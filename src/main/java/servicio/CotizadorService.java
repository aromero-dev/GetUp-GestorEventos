package servicio;

import dao.AccesoDatosException;
import dao.ConfiguracionDAO;
import modelo.Configuracion;
import modelo.Evento;

import java.time.LocalDate;

public class CotizadorService {

    // Resultado de cotizar: cada concepto, el total y la seña
    public record Desglose(double salon, double extras, double catering, double total, double senia) {
    }

    private final ConfiguracionDAO configuracionDAO;

    public CotizadorService(ConfiguracionDAO dao) {
        this.configuracionDAO = dao;
    }

    public Desglose calcularTotal(Evento e) throws AccesoDatosException {
        Configuracion c = configuracionDAO.leer(); // una sola lectura
        double salon = elegirTarifaBase(e, c) + calcularExtraPersonas(e, c);
        double extras = calcularTotalExtras(e, c);
        double catering = calcularTotalCatering(e);
        double total = salon + extras + catering;
        double senia = total * 0.5;

        e.setDuracion(calcularDuracion(e));
        e.setMontoTotal(total);
        e.setMontoSenia(senia);
        return new Desglose(salon, extras, catering, total, senia);
    }

    public int calcularDuracionBase(LocalDate fecha, boolean esFeriado) {
        int d = fecha.getDayOfWeek().getValue(); // 1=lunes ... 7=domingo
        return (d <= 4 && !esFeriado) ? 150 : 120;
    }

    private boolean esTarifaSemana(Evento e) {
        int d = e.getFecha().getDayOfWeek().getValue();
        return d <= 4 && !e.isEsFeriado();
    }

    private double elegirTarifaBase(Evento e, Configuracion c) {
        return esTarifaSemana(e) ? c.getTarifaBaseSemana() : c.getTarifaBaseFinde();
    }

    private double calcularExtraPersonas(Evento e, Configuracion c) {
        int ninios = Math.max(0, e.getCantidadNinios() - c.getNiniosIncluidos());
        int adultos = Math.max(0, e.getCantidadAdultos() - c.getAdultosIncluidos());
        return ninios * c.getPrecioNinioExtra() + adultos * c.getPrecioAdultoExtra();
    }

    private double calcularTotalExtras(Evento e, Configuracion c) {
        return e.getMediaHoraExtra() * c.getPrecioMediaHoraExtra()
                + e.getHoraExtra() * c.getPrecioHoraExtra()
                + e.getAnimadoraAdicional() * c.getPrecioAnimadoraAdicional()
                + e.getCumpleaneroExtra() * c.getPrecioCumpleaneroExtra();
    }

    private double calcularTotalCatering(Evento e) {
        int personas = e.getCantidadNinios() + e.getCantidadAdultos();
        return e.getOpcionCatering().getPrecioPorPersona() * personas;
    }

    private int calcularDuracion(Evento e) {
        int base = calcularDuracionBase(e.getFecha(), e.isEsFeriado());
        return base + 30 * e.getMediaHoraExtra() + 60 * e.getHoraExtra();
    }
}