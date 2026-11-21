package servicio;

import dao.AccesoDatosException;
import dao.ClienteDAO;
import dao.EventoDAO;
import modelo.Cliente;
import modelo.Evento;
import modelo.ValidacionException;

import java.util.Optional;

public class EventoService {

    private final ClienteDAO clienteDAO;
    private final EventoDAO eventoDAO;
    private final DisponibilidadService disponibilidad;

    public EventoService(ClienteDAO clienteDAO, EventoDAO eventoDAO,
                         DisponibilidadService disponibilidad) {
        this.clienteDAO = clienteDAO;
        this.eventoDAO = eventoDAO;
        this.disponibilidad = disponibilidad;
    }

    public Evento guardarNuevo(Evento evento) throws AccesoDatosException, ValidacionException {
        // última verificación: otra computadora pudo haber agendado mientras se cargaba
        boolean libre = disponibilidad.verificarDisponibilidad(
                evento.getFecha(), evento.getHoraInicio(), evento.getDuracion(), Optional.empty());
        if (!libre) {
            throw new ValidacionException("Ese horario ya está ocupado por otro evento");
        }

        Cliente tipeado = evento.getCliente();
        Optional<Cliente> existente = clienteDAO.buscarPorTelefono(tipeado.getTelefono());
        Cliente cliente = existente.isPresent() ? existente.get() : clienteDAO.guardar(tipeado);

        evento.setCliente(cliente);
        return eventoDAO.guardar(evento);
    }

    // El evento ya viene con los cambios aplicados en memoria (el modelo los validó).
    // Acá se recalcula, se verifica el horario ignorando al propio evento y se guarda.
    public void guardarEdicion(Evento evento, CotizadorService cotizador)
            throws AccesoDatosException, ValidacionException {
        // la seña ya cobrada se mantiene; el saldo absorbe la diferencia
        double seniaCobrada = evento.getMontoSenia();
        cotizador.calcularTotal(evento);
        evento.setMontoSenia(seniaCobrada);

        boolean libre = disponibilidad.verificarDisponibilidad(
                evento.getFecha(), evento.getHoraInicio(), evento.getDuracion(),
                Optional.of(evento.getId()));
        if (!libre) {
            throw new ValidacionException("Ese horario ya está ocupado por otro evento");
        }

        // el teléfono no puede pertenecer a otro cliente
        Cliente cliente = evento.getCliente();
        Optional<Cliente> conEseTelefono = clienteDAO.buscarPorTelefono(cliente.getTelefono());
        if (conEseTelefono.isPresent()
                && conEseTelefono.get().getIdCliente() != cliente.getIdCliente()) {
            throw new ValidacionException("Ya existe un cliente con ese teléfono");
        }

        clienteDAO.modificar(cliente);
        eventoDAO.modificar(evento);
    }

    public void registrarPagoYMarcarRealizado(Evento evento)
            throws AccesoDatosException, ValidacionException {
        evento.registrarPagoCompleto();
        evento.marcarRealizado();
        eventoDAO.modificar(evento);
    }

    public void cancelar(Evento evento)
            throws AccesoDatosException, ValidacionException {
        evento.cancelar();
        eventoDAO.modificar(evento);
    }
}