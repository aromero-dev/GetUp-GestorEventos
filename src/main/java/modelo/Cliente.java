package modelo;

import java.util.ArrayList;
import java.util.List;

public class Cliente {
    private String nombre;
    private String telefono;
    private  int idCliente;
    private List<Evento> eventos;


    public Cliente(String telefono, String nombre) throws ValidacionException {
        setNombre(nombre);
        setTelefono(telefono);
        this.eventos = new ArrayList<>();
    }

    public Cliente(int idCliente,String telefono, String nombre) {
        this.idCliente = idCliente;
        this.telefono = telefono;
        this.nombre = nombre;

    }

    public void setNombre(String nombre) throws ValidacionException {
        if (nombre==null || nombre.trim().isEmpty()) {
            throw new ValidacionException("El nombre es obligatorio");
        }

        this.nombre = nombre.trim();
    }

    public void setTelefono(String telefono) throws ValidacionException {
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new ValidacionException("El teléfono es obligatorio");
        }
        String limpio = telefono.trim();
        if (!limpio.matches("\\d+")) {
            throw new ValidacionException("El teléfono debe contener solo números");
        }
        if (limpio.length() < 10 || limpio.length() > 13) {
            throw new ValidacionException("El teléfono debe tener entre 10 y 13 dígitos");
        }
        this.telefono = limpio;
    }
    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public void setEventos(List<Evento> eventos) {
        this.eventos = eventos;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public List<Evento> getEventos() {
        return eventos;
    }
}