package dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {

    public static Connection obtenerConexion() throws AccesoDatosException {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            p.load(in);
            return DriverManager.getConnection(
                    p.getProperty("url"),
                    p.getProperty("usuario"),
                    p.getProperty("password"));
        } catch (IOException e) {
            throw new AccesoDatosException("No se pudo leer la configuración", e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo conectar con la base de datos", e);
        }
    }
}