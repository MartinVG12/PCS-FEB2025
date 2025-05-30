/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javafxappescolar.modelo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javafxappescolar.modelo.ConexionBD;
import javafxappescolar.modelo.pojo.Alumno;
import javafxappescolar.modelo.pojo.ResultadoOperacion;

/**
 *
 * @author marti
 */
public class AlumnoDAO {
    
    public static ArrayList<Alumno> obtenerAlumnos()throws SQLException{
        ArrayList<Alumno> alumnos = new ArrayList<>();
        Connection  conexionBD = ConexionBD.abrirConexion();
        if(conexionBD != null){
            String consulta = "SELECT idAlumno, a.nombre, apellidoPaterno, apellidoMaterno, matricula, email, a.idCarrera, fechaNacimiento, c.nombre AS 'carrera', c.idFacultad, f.nombre AS 'Facultad' " +
                              "FROM alumno a " +
                              "INNER JOIN carrera c ON c.idCarrera = a.idCarrera " +
                              "INNER JOIN facultad f ON f.idFacultad = c.idFacultad";
            PreparedStatement sentencia = conexionBD.prepareStatement(consulta);
            ResultSet resultado = sentencia.executeQuery();
            while(resultado.next()){
                alumnos.add(convertirRegistroAlumno(resultado));
            }
            resultado.close();
            sentencia.close();
            conexionBD.close();
        }else{
            throw new SQLException("Sin conexión a la BD");
        }
        return alumnos;
    }
    
    public static byte[] obtenerFotoAlumno(int idAlumno) throws SQLException {
    byte[] foto = null;
    Connection conexionBD = ConexionBD.abrirConexion();
    if (conexionBD != null) {
        String consulta = "SELECT foto FROM alumno WHERE idAlumno = ?";
        PreparedStatement sentencia = conexionBD.prepareStatement(consulta);
        sentencia.setInt(1, idAlumno);
        ResultSet resultado = sentencia.executeQuery();
        if (resultado.next()) {
            foto = resultado.getBytes("foto");
        }
        resultado.close();
        sentencia.close();
        conexionBD.close();
    } else {
        throw new SQLException("Sin conexión a la BD");
    }
    return foto;
}
    
    public static boolean verificarExistenciaMatricula(String matricula) throws SQLException {
    boolean existe = false;
    Connection conexionBD = ConexionBD.abrirConexion();
    if (conexionBD != null) {
        String consulta = "SELECT COUNT(*) AS total FROM alumno WHERE matricula = ?";
        PreparedStatement sentencia = conexionBD.prepareStatement(consulta);
        sentencia.setString(1, matricula);
        ResultSet resultado = sentencia.executeQuery();
        if (resultado.next()) {
            existe = resultado.getInt("total") > 0;
        }
        resultado.close();
        sentencia.close();
        conexionBD.close();
    } else {
        throw new SQLException("Sin conexión a la BD");
    }
    return existe;
}

    public static ResultadoOperacion editarAlumno(Alumno alumno) throws SQLException {
    ResultadoOperacion resultado = new ResultadoOperacion();
    Connection conexionBD = ConexionBD.abrirConexion();
    if (conexionBD != null) {
        String consulta = "UPDATE alumno SET nombre = ?, apellidoPaterno = ?, apellidoMaterno = ?, email = ?, idCarrera = ?, fechaNacimiento = ?, foto = ? WHERE idAlumno = ?";
        PreparedStatement sentencia = conexionBD.prepareStatement(consulta);
        sentencia.setString(1, alumno.getNombre());
        sentencia.setString(2, alumno.getApellidoPaterno());
        sentencia.setString(3, alumno.getApellidoMaterno());
        sentencia.setString(4, alumno.getEmail());
        sentencia.setInt(5, alumno.getIdCarrera());
        sentencia.setString(6, alumno.getFechaNacimiento());
        sentencia.setBytes(7, alumno.getFoto());
        sentencia.setInt(8, alumno.getIdAlumno());

        int filasAfectadas = sentencia.executeUpdate();
        if (filasAfectadas == 1) {
            resultado.setError(false);
            resultado.setMensaje("Alumno(a) actualizado correctamente.");
        } else {
            resultado.setError(true);
            resultado.setMensaje("No fue posible actualizar los datos del alumno(a).");
        }
        sentencia.close();
        conexionBD.close();
    } else {
        throw new SQLException("Sin conexión a la BD");
    }
    return resultado;
}
    
    public static ResultadoOperacion eliminarAlumno(int idAlumno) throws SQLException {
    ResultadoOperacion resultado = new ResultadoOperacion();
    Connection conexionBD = ConexionBD.abrirConexion();
    if (conexionBD != null) {
        String consulta = "DELETE FROM alumno WHERE idAlumno = ?";
        PreparedStatement sentencia = conexionBD.prepareStatement(consulta);
        sentencia.setInt(1, idAlumno);
        int filasAfectadas = sentencia.executeUpdate();
        if (filasAfectadas == 1) {
            resultado.setError(false);
            resultado.setMensaje("Alumno(a) eliminado correctamente.");
        } else {
            resultado.setError(true);
            resultado.setMensaje("No se pudo eliminar al alumno(a).");
        }
        sentencia.close();
        conexionBD.close();
    } else {
        throw new SQLException("Sin conexión a la BD");
    }
    return resultado;
}
    
    public static ResultadoOperacion registrarAlumno(Alumno alumno)throws SQLException{
        
        ResultadoOperacion resultado = new ResultadoOperacion();
        Connection conexionBD = ConexionBD.abrirConexion();
        if(conexionBD != null){
            String sentencia = "INSERT INTO " +
                    "alumno (nombre, apellidoPaterno, apellidoMaterno, matricula, email, idCarrera, fechaNacimiento, foto) " +
                    "VALUES (?, ?, ?, ?, ?, 2, ?, ?)";
            PreparedStatement prepararSentencia = conexionBD.prepareStatement(sentencia);
            prepararSentencia.setString(1, alumno.getNombre());
            prepararSentencia.setString(2, alumno.getApellidoPaterno());
            prepararSentencia.setString(3, alumno.getApellidoMaterno());
            prepararSentencia.setString(4, alumno.getMatricula());
            prepararSentencia.setString(5, alumno.getEmail());
            prepararSentencia.setInt(6, alumno.getIdCarrera());
            prepararSentencia.setString(7, alumno.getFechaNacimiento());
            prepararSentencia.setBytes(8, alumno.getFoto());
            int filasAfectadas = prepararSentencia.executeUpdate();
            if(filasAfectadas == 1){
                resultado.setError(false);
                resultado.setMensaje("Alumno(a) registrado correctamente.");
            }else{
                resultado.setError(true);
                resultado.setMensaje("Lo sentimos :( por el momento no se puede registrar la informacion del alumno(a), por favor intentelo mas tarde.");
            }
            prepararSentencia.close();
            conexionBD.close();
        }else{
            throw new SQLException("Sin conexión a la BD");
        }
        return resultado;
    }
    
    private static Alumno convertirRegistroAlumno(ResultSet resultado) throws SQLException{
        Alumno alumno = new Alumno();
        alumno.setIdAlumno(resultado.getInt("IdAlumno"));
        alumno.setNombre(resultado.getString("nombre"));
        alumno.setApellidoPaterno(resultado.getString("apellidoPaterno"));
        alumno.setApellidoMaterno(resultado.getString("apellidoMaterno"));
        alumno.setMatricula(resultado.getString("matricula"));
        alumno.setEmail(resultado.getString("email"));
        alumno.setIdCarrera(resultado.getInt("idCarrera"));
        alumno.setIdFacultad(resultado.getInt("idFacultad"));
        alumno.setFacultad(resultado.getString("facultad"));
        alumno.setFechaNacimiento(resultado.getString("fechaNacimiento"));
        return alumno;
    }
}
