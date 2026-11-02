package servicio;

import dao.AccesoDatosException;
import dao.ConfiguracionDAO;
import modelo.Configuracion;
import modelo.Evento;

public class CotizadorService {
    private final ConfiguracionDAO configuracionDAO;

    public CotizadorService(ConfiguracionDAO dao) {
        this.configuracionDAO = dao;
    }

    public void calcularTotal(Evento e) throws AccesoDatosException {
        Configuracion c = configuracionDAO.leer(); // una sola lectura
        double total = elegirTarifaBase(e, c)
                + calcularExtraPersonas(e, c)
                + calcularTotalExtras(e, c)
                + calcularTotalCatering(e);
        e.setDuracion(calcularDuracion(e));
        e.setMontoTotal(total);
        e.setMontoSenia(total * 0.5);
    }

    private boolean esTarifaSemana(Evento e) {
        int d = e.getFecha().getDayOfWeek().getValue(); // 1=lunes ... 7=domingo
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
        int base = esTarifaSemana(e) ? 150 : 120;
        return base + 30 * e.getMediaHoraExtra() + 60 * e.getHoraExtra();
    }
}
