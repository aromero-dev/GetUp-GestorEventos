package dao;

public class AccesoDatosException extends Exception {
    public AccesoDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa); // guarda la SQLException original
    }
    public AccesoDatosException(String mensaje) {
        super(mensaje);
    }
}
