/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package javafxappescolar.controlador;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafxappescolar.dominio.AlumnoDM;
import javafxappescolar.interfaz.INotificacion;
import javafxappescolar.modelo.dao.AlumnoDAO;
import javafxappescolar.modelo.dao.CatalogoDAO;
import javafxappescolar.modelo.pojo.Alumno;
import javafxappescolar.modelo.pojo.Carrera;
import javafxappescolar.modelo.pojo.Facultad;
import javafxappescolar.modelo.pojo.ResultadoOperacion;
import javafxappescolar.utilidades.Utilidad;
import javax.imageio.ImageIO;


/**
 * FXML Controller class
 *
 * @author marti
 */
public class FXMLFormularioAlumnoController implements Initializable {

    @FXML
    private ImageView ivFoto;
    @FXML
    private TextField tfNombre;
    @FXML
    private TextField tfApellidoPaterno;
    @FXML
    private TextField tfApellidoMaterno;
    @FXML
    private TextField tfMatricula;
    @FXML
    private TextField tfEmail;
    @FXML
    private DatePicker dpFechaNacimiento;
    @FXML
    private ComboBox<Facultad> cbFacultad;
    @FXML
    private ComboBox<Carrera> cbCarrera;
    
    ObservableList<Facultad> facultades;
    ObservableList<Carrera> carreras;
    File archivoFoto;
    INotificacion observador;
    Alumno alumnoEdicion;
    boolean esEdicion;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarFacultades();
        seleccionarFacultad();
    }
    
    public void inicializarInformacion(INotificacion observador, Alumno alumnoEdicion, boolean esEdicion){
        this.observador = observador;
        this.alumnoEdicion = alumnoEdicion;
        this.esEdicion = esEdicion;
        if(esEdicion){
            cargarInformacionEdicion();
        }
    }
    
    public void cargarInformacionEdicion(){
        tfMatricula.setText(alumnoEdicion.getMatricula());
        tfApellidoPaterno.setText(alumnoEdicion.getApellidoPaterno());
        tfApellidoMaterno.setText(alumnoEdicion.getApellidoMaterno());
        tfEmail.setText(alumnoEdicion.getEmail());
        tfNombre.setText(alumnoEdicion.getNombre());
        if(alumnoEdicion.getFechaNacimiento() != null){
            dpFechaNacimiento.setValue(LocalDate.parse(alumnoEdicion.getFechaNacimiento()));
        }
        //tfMatricula.setEditable(false);
        tfMatricula.setDisable(true);
        
        int indice = obtenerPosicionFacultad(alumnoEdicion.getIdFacultad());
        cbFacultad.getSelectionModel().select(indice);
        int indiceCarrera = obtenerPosicionCarrera(alumnoEdicion.getIdCarrera());
        cbCarrera.getSelectionModel().select(indiceCarrera);
        
        try {
            byte[] foto = AlumnoDAO.obtenerFotoAlumno(alumnoEdicion.getIdAlumno());
            alumnoEdicion.setFoto(foto);
            ByteArrayInputStream input = new ByteArrayInputStream(foto);
            Image image = new Image(input);
            ivFoto.setImage(image);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void cargarFacultades(){
        try {
            facultades = FXCollections.observableArrayList();
            //facultades = new ObservableList<Facultad>(){}
            List<Facultad> facultadesDAO = CatalogoDAO.obtenerFacultades();
            facultades.addAll(facultadesDAO);
            cbFacultad.setItems(facultades);
        } catch (SQLException ex) {
            //TODO
        }
    }
    
    private void seleccionarFacultad(){
        cbFacultad.valueProperty().addListener(new ChangeListener<Facultad>(){
            @Override
            public void changed(ObservableValue<? extends Facultad> observable, Facultad oldValue, Facultad newValue){
                if(newValue != null){
                    cargarCarreras(newValue.getIdFacultad());
                }
            }
        });
    }
    
    private void cargarCarreras(int idFacultad){
        try {
            carreras = FXCollections.observableArrayList();
            List<Carrera> carrerasDAO = CatalogoDAO.obtenerCarrerasPorFacultad(idFacultad);
            carreras.addAll(carrerasDAO);
            cbCarrera.setItems(carreras);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void clicSeleccionarFoto(ActionEvent event) {
    }

    @FXML
    private void clicGuardar(ActionEvent event){
        if(validarCampos() == true){
            try {
                if(!esEdicion){
                    ResultadoOperacion resultado = AlumnoDM.verificarEstadoMatricula(tfMatricula.getText());
                    if(!resultado.isError()){
                        Alumno alumno = obtenerAlumnoNuevo();
                        guardarAlumno(alumno);
                    }else{
                        Utilidad.mostrarAlertaSimple(Alert.AlertType.WARNING, "Verificar datos", resultado.getMensaje());
                    }
                }else{
                    Alumno alumno = obtenerAlumnoEdicion();
                    modificarAlumno(alumno);
                }
                Alumno alumno = obtenerAlumnoNuevo();
                guardarAlumno(alumno);
            } catch (IOException ex) {
                Utilidad.mostrarAlertaSimple(Alert.AlertType.ERROR, "Error en foto", "Lo sentimos, la foto seleccionada no puede ser guardada.");
            }
        }
    }

    @FXML
    private void clicCancelar(ActionEvent event) {
        Utilidad.getEscenarioComponente(tfNombre).close();
    }
    
    private void mostrarDialogoSeleccionFoto(){
        FileChooser dialogoSeleccion = new FileChooser();
        dialogoSeleccion.setTitle("Seleccione una foto");
        FileChooser.ExtensionFilter filtroImg = new FileChooser.ExtensionFilter("Archivos JPG (.jpg)", "*.jpg");
        dialogoSeleccion.getExtensionFilters().add(filtroImg);
        archivoFoto = dialogoSeleccion.showOpenDialog(Utilidad.getEscenarioComponente(tfNombre));
        if(archivoFoto != null){
            mostrarFotoPerfil(archivoFoto);
        }
    }
    
    private void mostrarFotoPerfil(File archivoFoto){
        try {
            BufferedImage bufferImg = ImageIO.read(archivoFoto);
            Image imagen = SwingFXUtils.toFXImage(bufferImg, null);
            ivFoto.setImage(imagen);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private boolean validarCampos(){
        //FIX
        return true;
    }
    
    private Alumno obtenerAlumnoNuevo()throws IOException{
        Alumno alumno = new Alumno();
        //String nombre = tfNombre.getText();
        alumno.setNombre(tfNombre.getText());
        alumno.setApellidoPaterno(tfApellidoPaterno.getText());
        alumno.setApellidoMaterno(tfApellidoMaterno.getText());
        alumno.setEmail(tfEmail.getText());
        alumno.setMatricula(tfMatricula.getText());
        alumno.setFechaNacimiento(dpFechaNacimiento.getValue().toString());
        Carrera carrera = cbCarrera.getSelectionModel().getSelectedItem();
        alumno.setIdCarrera(carrera.getIdCarrera());
        byte[] foto = Files.readAllBytes(archivoFoto.toPath());
        alumno.setFoto(foto);
        return alumno;
    }
    
    private Alumno obtenerAlumnoEdicion() throws IOException{
        Alumno alumno = new Alumno();
        alumno.setIdAlumno(alumnoEdicion.getIdAlumno());
        alumno.setNombre(tfNombre.getText());
        alumno.setApellidoPaterno(tfApellidoPaterno.getText());
        alumno.setApellidoMaterno(tfApellidoMaterno.getText());
        alumno.setEmail(tfEmail.getText());
        alumno.setMatricula(tfMatricula.getText());
        alumno.setFechaNacimiento(dpFechaNacimiento.getValue().toString());
        Carrera carrera = cbCarrera.getSelectionModel().getSelectedItem();
        alumno.setIdCarrera(carrera.getIdCarrera());
        if(archivoFoto != null){
            byte[] foto = Files.readAllBytes(archivoFoto.toPath());
            alumno.setFoto(foto);
        }else{
            alumno.setFoto(alumnoEdicion.getFoto());
        }
        return alumno;
    }
    
    private void guardarAlumno(Alumno alumno){
        try {
            ResultadoOperacion resultadoInsertar = AlumnoDAO.registrarAlumno(alumno);
            if(!resultadoInsertar.isError()){
                Utilidad.mostrarAlertaSimple(Alert.AlertType.INFORMATION, "Alumno registrado", "El alumno(a) " + alumno.getNombre() + ", fue registrado coon exito.");
                Utilidad.getEscenarioComponente(tfNombre).close();
                observador.operacionExitosa("Insertar", alumno.getNombre());
            }else{
                Utilidad.mostrarAlertaSimple(Alert.AlertType.ERROR, "Error al registrar", resultadoInsertar.getMensaje());
            }
        } catch (SQLException ex) {
            Utilidad.mostrarAlertaSimple(Alert.AlertType.ERROR, "Error de conexion", "Por el momento no hay conexion.");
        }
    }
    
    private void modificarAlumno(Alumno alumno){
        try {
            ResultadoOperacion resultadoModificar = AlumnoDAO.registrarAlumno(alumno);
            if(!resultadoModificar.isError()){
                Utilidad.mostrarAlertaSimple(Alert.AlertType.INFORMATION, "Alumno modificado", "El alumno(a) " + alumno.getNombre() + ", fue modificado con exito.");
                Utilidad.getEscenarioComponente(tfNombre).close();
                observador.operacionExitosa("Modificar", alumno.getNombre());
            }else{
                Utilidad.mostrarAlertaSimple(Alert.AlertType.ERROR, "Error al modificar", resultadoModificar.getMensaje());
            }
        } catch (SQLException ex) {
            Utilidad.mostrarAlertaSimple(Alert.AlertType.ERROR, "Error de conexion", "Por el momento no hay conexion.");
        }
    }
    
    private int obtenerPosicionFacultad(int idFacultad){
        for(int i=0; i < facultades.size(); i++){
            if(facultades.get(i).getIdFacultad() == idFacultad)
                return i;
        }
        return 0;
    }
    
    private int obtenerPosicionCarrera(int idCarrera){
        for(int i=0; i < carreras.size(); i++){
            if(carreras.get(i).getIdCarrera()== idCarrera)
                return i;
        }
        return 0;
    }
    
}
