package dao;

import modelo.Cliente;
import modelo.EstadoEvento;
import modelo.EstadoPago;
import modelo.Evento;
import modelo.OpcionCatering;
import modelo.ValidacionException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventoDAO {

    private static final String SELECT_BASE =
            "SELECT e.id_evento, e.nombre_cumpleanero, e.fecha, e.hora_inicio, e.duracion, "
                    + "e.es_feriado, e.cantidad_ninios, e.cantidad_adultos, e.media_hora_extra, "
                    + "e.hora_extra, e.animadora_adicional, e.cumpleanero_extra, e.estado_pago, "
                    + "e.estado, e.monto_senia, e.monto_total, "
                    + "c.id_cliente, c.nombre AS nombre_cliente, c.telefono, "
                    + "o.nombre_catering, o.precio_por_persona, o.activo "
                    + "FROM Evento e "
                    + "JOIN Cliente c ON e.id_cliente = c.id_cliente "
                    + "JOIN OpcionCatering o ON e.nombre_catering = o.nombre_catering ";

    private static final String MSG_ERROR = "No se pudo acceder a los eventos en la base de datos";

    public Evento guardar(Evento evento) throws AccesoDatosException {
        String sql = "INSERT INTO Evento (id_cliente, nombre_cumpleanero, nombre_catering, fecha, "
                + "hora_inicio, duracion, es_feriado, cantidad_ninios, cantidad_adultos, "
                + "media_hora_extra, hora_extra, animadora_adicional, cumpleanero_extra, "
                + "estado_pago, estado, monto_senia, monto_total) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            cargarParametros(ps, evento);
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new AccesoDatosException("No se pudo guardar el evento", null);
                }
                int id = claves.getInt(1);
                return new Evento(id, evento.getCliente(), evento.getNombreCumpleanero(),
                        evento.getFecha(), evento.getHoraInicio(), evento.getDuracion(),
                        evento.isEsFeriado(), evento.getCantidadNinios(), evento.getCantidadAdultos(),
                        evento.getMediaHoraExtra(), evento.getHoraExtra(), evento.getAnimadoraAdicional(),
                        evento.getCumpleaneroExtra(), evento.getOpcionCatering(), evento.getEstadoPago(),
                        evento.getEstado(), evento.getMontoSenia(), evento.getMontoTotal());
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el evento", e);
        }
    }

    public void modificar(Evento evento) throws AccesoDatosException {
        String sql = "UPDATE Evento SET id_cliente = ?, nombre_cumpleanero = ?, nombre_catering = ?, "
                + "fecha = ?, hora_inicio = ?, duracion = ?, es_feriado = ?, cantidad_ninios = ?, "
                + "cantidad_adultos = ?, media_hora_extra = ?, hora_extra = ?, "
                + "animadora_adicional = ?, cumpleanero_extra = ?, estado_pago = ?, estado = ?, "
                + "monto_senia = ?, monto_total = ? WHERE id_evento = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            cargarParametros(ps, evento);
            ps.setInt(18, evento.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo modificar el evento", e);
        }
    }

    public Optional<Evento> buscarPorId(int id) throws AccesoDatosException {
        String sql = SELECT_BASE + "WHERE e.id_evento = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(armarEvento(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new AccesoDatosException(MSG_ERROR, e);
        }
    }

    public List<Evento> buscarPorFecha(LocalDate fecha) throws AccesoDatosException {
        String sql = SELECT_BASE + "WHERE e.fecha = ? AND e.estado = 'activo' ORDER BY e.hora_inicio";
        return listar(sql, ps -> ps.setDate(1, Date.valueOf(fecha)));
    }

    public List<Evento> buscarProximos() throws AccesoDatosException {
        String sql = SELECT_BASE + "WHERE e.estado = 'activo' ORDER BY e.fecha, e.hora_inicio";
        return listar(sql, ps -> { });
    }

    public List<Evento> buscarHistorial() throws AccesoDatosException {
        String sql = SELECT_BASE + "WHERE e.estado IN ('realizado', 'cancelado') "
                + "ORDER BY e.fecha DESC, e.hora_inicio DESC";
        return listar(sql, ps -> { });
    }

    public List<Evento> buscarPorCumpleanero(String texto) throws AccesoDatosException {
        String sql = SELECT_BASE + "WHERE e.estado = 'activo' AND e.nombre_cumpleanero LIKE ? "
                + "ORDER BY e.fecha, e.hora_inicio";
        return listar(sql, ps -> ps.setString(1, "%" + texto + "%"));
    }

    public List<Evento> buscarPorTelefono(String telefono) throws AccesoDatosException {
        String sql = SELECT_BASE + "WHERE e.estado = 'activo' AND c.telefono = ? "
                + "ORDER BY e.fecha, e.hora_inicio";
        return listar(sql, ps -> ps.setString(1, telefono));
    }

    public List<Evento> buscarPorCliente(int idCliente) throws AccesoDatosException {
        String sql = SELECT_BASE + "WHERE e.id_cliente = ? ORDER BY e.fecha DESC, e.hora_inicio DESC";
        return listar(sql, ps -> ps.setInt(1, idCliente));
    }

    public List<Evento> buscarPorMes(int anio, int mes) throws AccesoDatosException {
        LocalDate desde = LocalDate.of(anio, mes, 1);
        LocalDate hasta = desde.plusMonths(1);
        String sql = SELECT_BASE + "WHERE e.fecha >= ? AND e.fecha < ? ORDER BY e.fecha, e.hora_inicio";
        return listar(sql, ps -> {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
        });
    }

    private void cargarParametros(PreparedStatement ps, Evento e) throws SQLException {
        ps.setInt(1, e.getCliente().getIdCliente());
        ps.setString(2, e.getNombreCumpleanero());
        ps.setString(3, e.getOpcionCatering().getNombre());
        ps.setDate(4, Date.valueOf(e.getFecha()));
        ps.setTime(5, Time.valueOf(e.getHoraInicio()));
        ps.setInt(6, e.getDuracion());
        ps.setBoolean(7, e.isEsFeriado());
        ps.setInt(8, e.getCantidadNinios());
        ps.setInt(9, e.getCantidadAdultos());
        ps.setInt(10, e.getMediaHoraExtra());
        ps.setInt(11, e.getHoraExtra());
        ps.setInt(12, e.getAnimadoraAdicional());
        ps.setInt(13, e.getCumpleaneroExtra());
        ps.setString(14, estadoPagoABd(e.getEstadoPago()));
        ps.setString(15, e.getEstado().name().toLowerCase());
        ps.setDouble(16, e.getMontoSenia());
        ps.setDouble(17, e.getMontoTotal());
    }

    private interface CargadorParametros {
        void cargar(PreparedStatement ps) throws SQLException;
    }

    private List<Evento> listar(String sql, CargadorParametros cargador) throws AccesoDatosException {
        List<Evento> eventos = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            cargador.cargar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    eventos.add(armarEvento(rs));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException(MSG_ERROR, e);
        }
        return eventos;
    }

    private Evento armarEvento(ResultSet rs) throws SQLException, AccesoDatosException {
        try {
            Cliente cliente = new Cliente(
                    rs.getInt("id_cliente"),
                    rs.getString("nombre_cliente"),
                    rs.getString("telefono"));

            OpcionCatering opcion = new OpcionCatering(
                    rs.getString("nombre_catering"),
                    rs.getDouble("precio_por_persona"),
                    rs.getBoolean("activo"));

            return new Evento(
                    rs.getInt("id_evento"),
                    cliente,
                    rs.getString("nombre_cumpleanero"),
                    rs.getDate("fecha").toLocalDate(),
                    rs.getTime("hora_inicio").toLocalTime(),
                    rs.getInt("duracion"),
                    rs.getBoolean("es_feriado"),
                    rs.getInt("cantidad_ninios"),
                    rs.getInt("cantidad_adultos"),
                    rs.getInt("media_hora_extra"),
                    rs.getInt("hora_extra"),
                    rs.getInt("animadora_adicional"),
                    rs.getInt("cumpleanero_extra"),
                    opcion,
                    estadoPagoDeBd(rs.getString("estado_pago")),
                    EstadoEvento.valueOf(rs.getString("estado").toUpperCase()),
                    rs.getDouble("monto_senia"),
                    rs.getDouble("monto_total"));
        } catch (ValidacionException e) {
            throw new AccesoDatosException("Un evento guardado tiene datos inválidos", e);
        }
    }

    private String estadoPagoABd(EstadoPago estado) {
        return estado == EstadoPago.SENIADO ? "señado" : "pagado";
    }

    private EstadoPago estadoPagoDeBd(String valor) {
        return "señado".equals(valor) ? EstadoPago.SENIADO : EstadoPago.PAGADO;
    }
}