package modelo;

import java.time.LocalDate;
import java.time.LocalTime;

public class Evento {
    private int id;
    private Cliente cliente;
    private String nombreCumpleanero;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private int duracion;
    private boolean esFeriado;
    private int cantidadNinios;
    private int cantidadAdultos;
    private int mediaHoraExtra;
    private int horaExtra;
    private int animadoraAdicional;
    private int cumpleaneroExtra;
    private OpcionCatering opcionCatering;
    private EstadoPago estadoPago;
    private EstadoEvento estado;
    private double montoSenia;
    private double montoTotal;

    // Constructor para un evento nuevo: valida todo a través de los setters
    public Evento(Cliente cliente, String nombreCumpleanero, LocalDate fecha, LocalTime horaInicio, boolean esFeriado, int cantidadNinios, int cantidadAdultos, OpcionCatering opcionCatering) throws ValidacionException {
        setCliente(cliente);
        setNombreCumpleanero(nombreCumpleanero);
        setFecha(fecha);
        setHoraInicio(horaInicio);
        setEsFeriado(esFeriado);
        setCantidadNinios(cantidadNinios);
        setCantidadAdultos(cantidadAdultos);
        setOpcionCatering(opcionCatering);

        this.mediaHoraExtra = 0;
        this.horaExtra = 0;
        this.animadoraAdicional = 0;
        this.cumpleaneroExtra = 0;
        this.estadoPago = EstadoPago.SENIADO;
        this.estado = EstadoEvento.ACTIVO;
    }

    // Constructor para el dao: reconstruye desde la base, sin revalidar
    public Evento(int id, Cliente cliente, String nombreCumpleanero, LocalDate fecha, LocalTime horaInicio, int duracion, boolean esFeriado, int cantidadNinios, int cantidadAdultos, int mediaHoraExtra, int horaExtra, int animadoraAdicional, int cumpleaneroExtra, OpcionCatering opcionCatering, EstadoPago estadoPago, EstadoEvento estado, double montoSenia, double montoTotal) {
        this.id = id;
        this.cliente = cliente;
        this.nombreCumpleanero = nombreCumpleanero;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.duracion = duracion;
        this.esFeriado = esFeriado;
        this.cantidadNinios = cantidadNinios;
        this.cantidadAdultos = cantidadAdultos;
        this.mediaHoraExtra = mediaHoraExtra;
        this.horaExtra = horaExtra;
        this.animadoraAdicional = animadoraAdicional;
        this.cumpleaneroExtra = cumpleaneroExtra;
        this.opcionCatering = opcionCatering;
        this.estadoPago = estadoPago;
        this.estado = estado;
        this.montoSenia = montoSenia;
        this.montoTotal = montoTotal;
    }

    // Transiciones de estado
    public void registrarPagoCompleto() throws ValidacionException {
        if (estado != EstadoEvento.ACTIVO) {
            throw new ValidacionException("Un evento cancelado o realizado no se puede volver a activar");
        }
        if (estadoPago == EstadoPago.PAGADO) {
            throw new ValidacionException("El evento ya tiene el pago completo");
        }
        this.estadoPago = EstadoPago.PAGADO;
    }

    public void marcarRealizado() throws ValidacionException {
        if (estado != EstadoEvento.ACTIVO) {
            throw new ValidacionException("Solo un evento activo se puede dar por realizado");
        }
        if (estadoPago != EstadoPago.PAGADO) {
            throw new ValidacionException("El evento no se puede dar por realizado sin el pago completo");
        }
        this.estado = EstadoEvento.REALIZADO;
    }

    public void cancelar() throws ValidacionException {
        if (estado != EstadoEvento.ACTIVO) {
            throw new ValidacionException("Solo un evento activo se puede cancelar");
        }
        this.estado = EstadoEvento.CANCELADO;
    }

    // Getters y setters con validación
    public int getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) throws ValidacionException {
        if (cliente == null) {
            throw new ValidacionException("El evento debe tener un cliente");
        }
        this.cliente = cliente;
    }

    public String getNombreCumpleanero() {
        return nombreCumpleanero;
    }

    public void setNombreCumpleanero(String nombreCumpleanero) throws ValidacionException {
        if (nombreCumpleanero == null || nombreCumpleanero.trim().isEmpty()) {
            throw new ValidacionException("El nombre del cumpleañero es obligatorio");
        }
        String nombreLimpio = nombreCumpleanero.trim();
        if (nombreLimpio.length() > 50) {
            throw new ValidacionException("El nombre del cumpleañero no puede superar los 50 caracteres");
        }
        this.nombreCumpleanero = nombreLimpio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) throws ValidacionException {
        if (fecha == null) {
            throw new ValidacionException("La fecha es obligatoria");
        }
        if (fecha.isBefore(LocalDate.now())) {
            throw new ValidacionException("No se puede agendar una fecha que ya pasó");
        }
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) throws ValidacionException {
        if (horaInicio == null) {
            throw new ValidacionException("La hora de inicio es obligatoria");
        }
        this.horaInicio = horaInicio;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public boolean isEsFeriado() {
        return esFeriado;
    }

    public void setEsFeriado(boolean esFeriado) {
        this.esFeriado = esFeriado;
    }

    public int getCantidadNinios() {
        return cantidadNinios;
    }

    public void setCantidadNinios(int cantidadNinios) throws ValidacionException {
        if (cantidadNinios < 1) {
            throw new ValidacionException("Tiene que haber al menos 1 niño");
        }
        this.cantidadNinios = cantidadNinios;
    }

    public int getCantidadAdultos() {
        return cantidadAdultos;
    }

    public void setCantidadAdultos(int cantidadAdultos) throws ValidacionException {
        if (cantidadAdultos < 1) {
            throw new ValidacionException("Tiene que haber al menos 1 adulto");
        }
        this.cantidadAdultos = cantidadAdultos;
    }

    public int getMediaHoraExtra() {
        return mediaHoraExtra;
    }

    public void setMediaHoraExtra(int mediaHoraExtra) throws ValidacionException {
        if (mediaHoraExtra < 0) {
            throw new ValidacionException("La cantidad no puede ser negativa");
        }
        this.mediaHoraExtra = mediaHoraExtra;
    }

    public int getHoraExtra() {
        return horaExtra;
    }

    public void setHoraExtra(int horaExtra) throws ValidacionException {
        if (horaExtra < 0) {
            throw new ValidacionException("La cantidad no puede ser negativa");
        }
        this.horaExtra = horaExtra;
    }

    public int getAnimadoraAdicional() {
        return animadoraAdicional;
    }

    public void setAnimadoraAdicional(int animadoraAdicional) throws ValidacionException {
        if (animadoraAdicional < 0) {
            throw new ValidacionException("La cantidad no puede ser negativa");
        }
        this.animadoraAdicional = animadoraAdicional;
    }

    public int getCumpleaneroExtra() {
        return cumpleaneroExtra;
    }

    public void setCumpleaneroExtra(int cumpleaneroExtra) throws ValidacionException {
        if (cumpleaneroExtra < 0) {
            throw new ValidacionException("La cantidad no puede ser negativa");
        }
        this.cumpleaneroExtra = cumpleaneroExtra;
    }

    public OpcionCatering getOpcionCatering() {
        return opcionCatering;
    }

    public void setOpcionCatering(OpcionCatering opcionCatering) throws ValidacionException {
        if (opcionCatering == null) {
            throw new ValidacionException("Elegí una opción de catering");
        }
        this.opcionCatering = opcionCatering;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public EstadoEvento getEstado() {
        return estado;
    }

    public double getMontoSenia() {
        return montoSenia;
    }

    public void setMontoSenia(double montoSenia) {
        this.montoSenia = montoSenia;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }
}