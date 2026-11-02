package servicio;

import dao.AccesoDatosException;
import dao.EventoDAO;
import modelo.EstadoEvento;
import modelo.EstadoPago;
import modelo.Evento;

import java.time.YearMonth;
import java.util.List;

public class Recaudacionservice {

    // Resultado de un mes: tres montos y su suma
    public record TotalMes(double cobradoCompleto, double cobradoEnSenias, double seniasCanceladas) {
        public double total() {
            return cobradoCompleto + cobradoEnSenias + seniasCanceladas;
        }

        @Override
        public String toString() {
            return String.format("%.2f (completos %.2f + señas por cobrar %.2f + señas de cancelados %.2f)",
                    total(), cobradoCompleto, cobradoEnSenias, seniasCanceladas);
        }
    }

    // Resultado de la comparación; variacionPorcentual es null si el mes anterior fue 0
    public record Comparacion(double diferencia, Double variacionPorcentual) {
        @Override
        public String toString() {
            if (variacionPorcentual == null) {
                return "el mes anterior fue 0, no hay base de comparación (diferencia " + diferencia + ")";
            }
            return String.format("diferencia %.2f (%.1f%%)", diferencia, variacionPorcentual);
        }
    }

    private final EventoDAO eventoDAO;

    public Recaudacionservice(EventoDAO eventoDAO) {
        this.eventoDAO = eventoDAO;
    }

    public TotalMes calcularTotalMes(YearMonth mes) throws AccesoDatosException {
        List<Evento> eventos = eventoDAO.buscarPorMes(mes.getYear(), mes.getMonthValue());
        double completo = 0;
        double senias = 0;
        double seniasCanceladas = 0;
        for (Evento e : eventos) {
            if (e.getEstado() == EstadoEvento.CANCELADO) {
                seniasCanceladas += e.getMontoSenia();       // la seña no se devuelve
            } else if (e.getEstadoPago() == EstadoPago.PAGADO) {
                completo += e.getMontoTotal();
            } else {
                senias += e.getMontoSenia();                  // señado y activo
            }
        }
        return new TotalMes(completo, senias, seniasCanceladas);
    }

    // Promedio de los últimos "rangoDeMeses" meses, incluido el actual. Un mes sin eventos suma 0.
    public double calcularPromedio(int rangoDeMeses) throws AccesoDatosException {
        YearMonth actual = YearMonth.now();
        double suma = 0;
        for (int i = 0; i < rangoDeMeses; i++) {
            suma += calcularTotalMes(actual.minusMonths(i)).total();
        }
        return suma / rangoDeMeses;
    }

    public Comparacion compararConMesAnterior(YearMonth mes) throws AccesoDatosException {
        double actual = calcularTotalMes(mes).total();
        double anterior = calcularTotalMes(mes.minusMonths(1)).total();
        double diferencia = actual - anterior;
        Double variacion = anterior == 0 ? null : diferencia / anterior * 100;
        return new Comparacion(diferencia, variacion);
    }
}