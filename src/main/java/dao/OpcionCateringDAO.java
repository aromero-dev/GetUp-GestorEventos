package dao;

import modelo.OpcionCatering;
import modelo.ValidacionException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OpcionCateringDAO {

    public List<OpcionCatering> buscarActivas() throws AccesoDatosException {
        String sql = "SELECT nombre_catering, precio_por_persona, activo "
                + "FROM OpcionCatering WHERE activo = TRUE ORDER BY nombre_catering";
        List<OpcionCatering> opciones = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                opciones.add(armarOpcion(rs));
            }
            return opciones;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudieron cargar los menús de catering", e);
        }
    }

    public Optional<OpcionCatering> buscarPorNombre(String nombre) throws AccesoDatosException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            return buscarPorNombre(con, nombre);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo buscar el menú de catering", e);
        }
    }

    public void agregar(OpcionCatering opcion) throws ValidacionException, AccesoDatosException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            Optional<OpcionCatering> existente = buscarPorNombre(con, opcion.getNombre());

            if (existente.isPresent()) {
                if (existente.get().isActivo()) {
                    throw new ValidacionException("Ya existe un menú con ese nombre");
                }
                String sql = "UPDATE OpcionCatering SET precio_por_persona = ?, activo = TRUE "
                        + "WHERE nombre_catering = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setDouble(1, opcion.getPrecioPorPersona());
                    ps.setString(2, opcion.getNombre());
                    ps.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO OpcionCatering (nombre_catering, precio_por_persona, activo) "
                        + "VALUES (?, ?, TRUE)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, opcion.getNombre());
                    ps.setDouble(2, opcion.getPrecioPorPersona());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo agregar el menú de catering", e);
        }
    }

    public void modificarPrecio(OpcionCatering opcion) throws AccesoDatosException {
        String sql = "UPDATE OpcionCatering SET precio_por_persona = ? WHERE nombre_catering = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, opcion.getPrecioPorPersona());
            ps.setString(2, opcion.getNombre());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo modificar el precio del menú", e);
        }
    }

    public void ocultar(String nombre) throws AccesoDatosException {
        String sql = "UPDATE OpcionCatering SET activo = FALSE WHERE nombre_catering = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo borrar el menú de catering", e);
        }
    }

    private Optional<OpcionCatering> buscarPorNombre(Connection con, String nombre)
            throws SQLException, AccesoDatosException {
        String sql = "SELECT nombre_catering, precio_por_persona, activo "
                + "FROM OpcionCatering WHERE nombre_catering = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(armarOpcion(rs));
                }
                return Optional.empty();
            }
        }
    }

    // Si una fila no pasa la validación es un problema de datos en la base, no del usuario.
    private OpcionCatering armarOpcion(ResultSet rs) throws SQLException, AccesoDatosException {
        try {
            return new OpcionCatering(
                    rs.getString("nombre_catering"),
                    rs.getDouble("precio_por_persona"),
                    rs.getBoolean("activo"));
        } catch (ValidacionException e) {
            throw new AccesoDatosException("Hay un menú con datos inválidos en la base", e);
        }
    }
}