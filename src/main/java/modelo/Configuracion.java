package modelo;

public class Configuracion {
    private double tarifaBaseSemana;
    private double tarifaBaseFinde;
    private double precioNinioExtra;
    private double precioAdultoExtra;
    private double precioMediaHoraExtra;
    private double precioHoraExtra;
    private double precioAnimadoraAdicional;
    private double precioCumpleaneroExtra;
    private int niniosIncluidos;
    private int adultosIncluidos;

    public Configuracion(double tarifaBaseSemana, double tarifaBaseFinde,
                         double precioNinioExtra, double precioAdultoExtra,
                         double precioMediaHoraExtra, double precioHoraExtra,
                         double precioAnimadoraAdicional, double precioCumpleaneroExtra,
                         int niniosIncluidos, int adultosIncluidos) throws ValidacionException {
        setTarifaBaseSemana(tarifaBaseSemana);
        setTarifaBaseFinde(tarifaBaseFinde);
        setPrecioNinioExtra(precioNinioExtra);
        setPrecioAdultoExtra(precioAdultoExtra);
        setPrecioMediaHoraExtra(precioMediaHoraExtra);
        setPrecioHoraExtra(precioHoraExtra);
        setPrecioAnimadoraAdicional(precioAnimadoraAdicional);
        setPrecioCumpleaneroExtra(precioCumpleaneroExtra);
        setNiniosIncluidos(niniosIncluidos);
        setAdultosIncluidos(adultosIncluidos);
    }

    private static double validarPrecio(double precio) throws ValidacionException {
        if (!(precio >= 0)) {
            throw new ValidacionException("El precio no puede ser negativo");
        }
        return precio;
    }

    private static int validarCantidadIncluida(int cantidad) throws ValidacionException {
        if (cantidad < 1) {
            throw new ValidacionException("La cantidad incluida debe ser al menos 1");
        }
        return cantidad;
    }

    public double getTarifaBaseSemana() {
        return tarifaBaseSemana;
    }

    public double getTarifaBaseFinde() {
        return tarifaBaseFinde;
    }

    public double getPrecioNinioExtra() {
        return precioNinioExtra;
    }

    public double getPrecioAdultoExtra() {
        return precioAdultoExtra;
    }

    public double getPrecioMediaHoraExtra() {
        return precioMediaHoraExtra;
    }

    public double getPrecioHoraExtra() {
        return precioHoraExtra;
    }

    public double getPrecioAnimadoraAdicional() {
        return precioAnimadoraAdicional;
    }

    public double getPrecioCumpleaneroExtra() {
        return precioCumpleaneroExtra;
    }

    public int getNiniosIncluidos() {
        return niniosIncluidos;
    }

    public int getAdultosIncluidos() {
        return adultosIncluidos;
    }

    public void setTarifaBaseSemana(double tarifaBaseSemana) throws ValidacionException {
        this.tarifaBaseSemana = validarPrecio(tarifaBaseSemana);
    }

    public void setTarifaBaseFinde(double tarifaBaseFinde) throws ValidacionException {
        this.tarifaBaseFinde = validarPrecio(tarifaBaseFinde);
    }

    public void setPrecioNinioExtra(double precioNinioExtra) throws ValidacionException {
        this.precioNinioExtra = validarPrecio(precioNinioExtra);
    }

    public void setPrecioAdultoExtra(double precioAdultoExtra) throws ValidacionException {
        this.precioAdultoExtra = validarPrecio(precioAdultoExtra);
    }

    public void setPrecioMediaHoraExtra(double precioMediaHoraExtra) throws ValidacionException {
        this.precioMediaHoraExtra = validarPrecio(precioMediaHoraExtra);
    }

    public void setPrecioHoraExtra(double precioHoraExtra) throws ValidacionException {
        this.precioHoraExtra = validarPrecio(precioHoraExtra);
    }

    public void setPrecioAnimadoraAdicional(double precioAnimadoraAdicional) throws ValidacionException {
        this.precioAnimadoraAdicional = validarPrecio(precioAnimadoraAdicional);
    }

    public void setPrecioCumpleaneroExtra(double precioCumpleaneroExtra) throws ValidacionException {
        this.precioCumpleaneroExtra = validarPrecio(precioCumpleaneroExtra);
    }

    public void setNiniosIncluidos(int niniosIncluidos) throws ValidacionException {
        this.niniosIncluidos = validarCantidadIncluida(niniosIncluidos);
    }

    public void setAdultosIncluidos(int adultosIncluidos) throws ValidacionException {
        this.adultosIncluidos = validarCantidadIncluida(adultosIncluidos);
    }
}