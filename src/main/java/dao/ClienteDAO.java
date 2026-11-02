package dao;

import modelo.Cliente;
import java.sql.*;
import java.util.Optional;

public class ClienteDAO {

    public Cliente guardar(Cliente cliente) throws AccesoDatosException {
        String sql = "INSERT INTO Cliente (nombre, telefono) VALUES (?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getTelefono());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return new Cliente(rs.getInt(1), cliente.getNombre(), cliente.getTelefono());
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el cliente", e);
        }
    }

    public void modificar(Cliente cliente) throws AccesoDatosException {
        String sql = "UPDATE Cliente SET nombre = ?, telefono = ? WHERE id_cliente = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getTelefono());
            ps.setInt(3, cliente.getIdCliente());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo modificar el cliente", e);
        }
    }

    public Optional<Cliente> buscarPorId(int id) throws AccesoDatosException {
        return buscarUno("SELECT * FROM Cliente WHERE id_cliente = ?", id);
    }

    public Optional<Cliente> buscarPorTelefono(String telefono) throws AccesoDatosException {
        return buscarUno("SELECT * FROM Cliente WHERE telefono = ?", telefono);
    }

    private Optional<Cliente> buscarUno(String sql, Object parametro) throws AccesoDatosException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Cliente(
                            rs.getInt("id_cliente"),
                            rs.getString("nombre"),
                            rs.getString("telefono")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo buscar el cliente", e);
        }
    }
}