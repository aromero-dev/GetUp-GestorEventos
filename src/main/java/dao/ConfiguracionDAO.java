package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Configuracion;
import modelo.ValidacionException;

public class ConfiguracionDAO {

    private static final int ID_CONFIGURACION = 1;

    public Configuracion leer() throws AccesoDatosException {
        String sql = "SELECT tarifa_base_semana, tarifa_base_finde, precio_ninio_extra, "
                + "precio_adulto_extra, precio_media_hora_extra, precio_hora_extra, "
                + "precio_animadora_adicional, precio_cumpleanero_extra, "
                + "ninios_incluidos, adultos_incluidos "
                + "FROM Configuracion WHERE id_configuracion = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ID_CONFIGURACION);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new AccesoDatosException(
                            "No se encontró la configuración de precios en la base de datos", null);
                }
                return new Configuracion(
                        rs.getDouble("tarifa_base_semana"),
                        rs.getDouble("tarifa_base_finde"),
                        rs.getDouble("precio_ninio_extra"),
                        rs.getDouble("precio_adulto_extra"),
                        rs.getDouble("precio_media_hora_extra"),
                        rs.getDouble("precio_hora_extra"),
                        rs.getDouble("precio_animadora_adicional"),
                        rs.getDouble("precio_cumpleanero_extra"),
                        rs.getInt("ninios_incluidos"),
                        rs.getInt("adultos_incluidos"));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo leer la configuración", e);
        } catch (ValidacionException e) {
            throw new AccesoDatosException(
                    "Los datos de configuración guardados en la base no son válidos", null);
        }
    }

    public void actualizar(Configuracion c) throws AccesoDatosException {
        String sql = "UPDATE Configuracion SET tarifa_base_semana = ?, tarifa_base_finde = ?, "
                + "precio_ninio_extra = ?, precio_adulto_extra = ?, precio_media_hora_extra = ?, "
                + "precio_hora_extra = ?, precio_animadora_adicional = ?, "
                + "precio_cumpleanero_extra = ?, ninios_incluidos = ?, adultos_incluidos = ? "
                + "WHERE id_configuracion = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, c.getTarifaBaseSemana());
            ps.setDouble(2, c.getTarifaBaseFinde());
            ps.setDouble(3, c.getPrecioNinioExtra());
            ps.setDouble(4, c.getPrecioAdultoExtra());
            ps.setDouble(5, c.getPrecioMediaHoraExtra());
            ps.setDouble(6, c.getPrecioHoraExtra());
            ps.setDouble(7, c.getPrecioAnimadoraAdicional());
            ps.setDouble(8, c.getPrecioCumpleaneroExtra());
            ps.setInt(9, c.getNiniosIncluidos());
            ps.setInt(10, c.getAdultosIncluidos());
            ps.setInt(11, ID_CONFIGURACION);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudieron guardar los cambios de configuración", e);
        }
    }
}