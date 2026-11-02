package modelo;

public class OpcionCatering {
    private final String nombre;
    private double precioPorPersona;
    private boolean activo;

    // Para datos nuevos: activo queda en true
    public OpcionCatering(String nombre, double precioPorPersona) throws ValidacionException {
        this(nombre, precioPorPersona, true);
    }

    // Para el dao
    public OpcionCatering(String nombre, double precioPorPersona, boolean activo) throws ValidacionException {
        this.nombre = validarNombre(nombre);
        this.precioPorPersona = validarPrecio(precioPorPersona);
        this.activo = activo;
    }

    private static String validarNombre(String nombre) throws ValidacionException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionException("El nombre del menú es obligatorio");
        }
        String limpio = nombre.trim();
        if (limpio.length() > 50) {
            throw new ValidacionException("El nombre no puede superar los 50 caracteres");
        }
        return limpio;
    }

    private static double validarPrecio(double precio) throws ValidacionException {
        if (precio <= 0) {
            throw new ValidacionException("El precio por persona debe ser mayor a 0");
        }
        return precio;
    }

    public void setPrecioPorPersona(double precio) throws ValidacionException {
        this.precioPorPersona = validarPrecio(precio);
    }

    public void ocultar() { this.activo = false; }

    public void reactivar(double nuevoPrecio) throws ValidacionException {
        this.precioPorPersona = validarPrecio(nuevoPrecio);
        this.activo = true;
    }

    public String getNombre() { return nombre; }
    public double getPrecioPorPersona() { return precioPorPersona; }
    public boolean isActivo() { return activo; }
}