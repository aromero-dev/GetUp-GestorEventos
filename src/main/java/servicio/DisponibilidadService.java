package servicio;

import dao.AccesoDatosException;
import dao.EventoDAO;
import modelo.Evento;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class DisponibilidadService {

    private final EventoDAO eventoDAO;

    public DisponibilidadService(EventoDAO eventoDAO) {
        this.eventoDAO = eventoDAO;
    }


    public boolean verificarDisponibilidad(LocalDate fecha, LocalTime horaInicio,
                                           int duracion, Optional<Integer> idEventoAExcluir)
            throws AccesoDatosException {

        LocalTime fin = horaInicio.plusMinutes(duracion);
        List<Evento> delDia = eventoDAO.buscarPorFecha(fecha);

        for (Evento otro : delDia) {
            if (idEventoAExcluir.isPresent() && otro.getId() == idEventoAExcluir.get()) {
                continue; // al editar, ignora al propio evento
            }
            LocalTime otroInicio = otro.getHoraInicio();
            LocalTime otroFin = otroInicio.plusMinutes(otro.getDuracion());

            if (horaInicio.isBefore(otroFin) && otroInicio.isBefore(fin)) {
                return false;
            }
        }
        return true;
    }
}