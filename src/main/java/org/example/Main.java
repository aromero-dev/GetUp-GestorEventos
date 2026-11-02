package org.example;

import dao.*;
import modelo.*;
import modelo.*;
import servicio.*;
import servicio.CotizadorService;
import servicio.DisponibilidadService;
import modelo.ValidacionException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Scanner in = new Scanner(System.in);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MM/yyyy");

    private static final ClienteDAO clienteDAO = new ClienteDAO();
    private static final EventoDAO eventoDAO = new EventoDAO();
    private static final OpcionCateringDAO cateringDAO = new OpcionCateringDAO();
    private static final ConfiguracionDAO configDAO = new ConfiguracionDAO();
    private static final CotizadorService cotizador = new CotizadorService(configDAO);
    private static final DisponibilidadService disponibilidad = new DisponibilidadService(eventoDAO);
    private static final Recaudacionservice recaudacion = new Recaudacionservice(eventoDAO);

    // ---------------------------------------------------------------- MENÚ PRINCIPAL

    public static void main(String[] args) {
        try (var conexion = ConexionBD.obtenerConexion()) {
            System.out.println("Conexión a la base OK");
        } catch (Exception e) {
            System.err.println("No se pudo conectar: " + e.getMessage());
            return;
        }
        int op;
        do {
            System.out.println("\n=== GET UP EVENTOS ===");
            System.out.println("1. Nuevo evento");
            System.out.println("2. Ver eventos");
            System.out.println("3. Ver historial de eventos");
            System.out.println("4. Recaudación");
            System.out.println("5. Opciones");
            System.out.println("0. Salir");
            op = leerInt("Opción: ");
            try {
                switch (op) {
                    case 1 -> nuevoEvento();
                    case 2 -> verEventos();
                    case 3 -> verHistorial();
                    case 4 -> verRecaudacion();
                    case 5 -> opciones();
                    case 0 -> System.out.println("Chau");
                    default -> System.out.println("Opción inválida");
                }
            } catch (ValidacionException | AccesoDatosException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (op != 0);
    }

    // ---------------------------------------------------------------- 1. NUEVO EVENTO

    private static void nuevoEvento() throws ValidacionException, AccesoDatosException, modelo.ValidacionException {
        // Paso 1: fecha, hora y feriado, con la duración base del día
        LocalDate fecha = leerFecha("Fecha (dd/mm/aaaa): ");
        LocalTime hora = leerHora("Hora de inicio (hh:mm): ");
        boolean feriado = leerSiNo("¿Es feriado?");
        if (!disponibilidad.verificarDisponibilidad(fecha, hora, duracionBase(fecha, feriado), Optional.empty())) {
            System.out.println("Ese horario ya está ocupado por otro evento");
            return;
        }
        System.out.println("Horario libre. Seguimos con los datos.");

        // Paso 2: datos, extras, catering
        String nombre = leerTexto("Nombre del cliente: ");
        String telefono = leerTexto("Teléfono (solo dígitos): ");
        String cumple = leerTexto("Nombre del cumpleañero: ");
        int ninios = leerInt("Niños: ");
        int adultos = leerInt("Adultos: ");
        int[] extras = leerExtras();
        OpcionCatering catering = elegirCatering();


        // Cliente recurrente: se reutiliza el existente (pendiente de confirmar en el doc, sección 12)
        Optional<Cliente> existente = clienteDAO.buscarPorTelefono(telefono);
        Cliente cliente = existente.orElse(new Cliente(nombre, telefono));

        Evento evento = armarEvento(cliente, cumple, fecha, hora, feriado, ninios, adultos, catering, extras);
        cotizador.calcularTotal(evento);

        // Revalidación con la duración final
        if (!disponibilidad.verificarDisponibilidad(fecha, hora, evento.getDuracion(), Optional.empty())) {
            System.out.println("Con los extras elegidos el horario se superpone con otro evento. No se guardó nada.");
            return;
        }
        mostrarDetalle(evento);
        if (!leerSiNo("¿Confirmar y guardar?")) {
            System.out.println("Descartado");
            return;
        }
        if (existente.isEmpty()) {
            cliente = clienteDAO.guardar(cliente);
            // se arma de nuevo para que el evento tenga el cliente con id
            evento = armarEvento(cliente, cumple, fecha, hora, feriado, ninios, adultos, catering, extras);
            cotizador.calcularTotal(evento);
        }
        eventoDAO.guardar(evento);
        System.out.println("Evento guardado con éxito");
    }

    private static Evento armarEvento(Cliente cliente, String cumple, LocalDate fecha, LocalTime hora,
                                      boolean feriado, int ninios, int adultos,
                                      OpcionCatering catering, int[] extras) throws ValidacionException {
        Evento e = new Evento(cliente, cumple, fecha, hora, feriado, ninios, adultos, catering);
        e.setMediaHoraExtra(extras[0]);
        e.setHoraExtra(extras[1]);
        e.setAnimadoraAdicional(extras[2]);
        e.setCumpleaneroExtra(extras[3]);
        return e;
    }

    private static int[] leerExtras() {
        return new int[]{
                leerInt("Medias horas extra (0 si no): "),
                leerInt("Horas extra (0 si no): "),
                leerInt("Animadoras adicionales (0 si no): "),
                leerInt("Cumpleañeros extra (0 si no): ")
        };
    }

    private static int duracionBase(LocalDate fecha, boolean feriado) {
        boolean semana = !feriado && fecha.getDayOfWeek().getValue() <= DayOfWeek.THURSDAY.getValue();
        return semana ? 150 : 120;
    }

    // ---------------------------------------------------------------- 2. VER EVENTOS

    private static void verEventos() throws ValidacionException, AccesoDatosException {
        int op;
        do {
            List<Evento> lista = eventoDAO.buscarProximos();
            System.out.println("\n--- VER EVENTOS ---");
            listar(lista);
            System.out.println("1. Abrir un evento (por id)");
            System.out.println("2. Buscar por cumpleañero");
            System.out.println("3. Buscar por teléfono");
            System.out.println("4. Buscar por fecha");
            System.out.println("0. Volver");
            op = leerInt("Opción: ");
            switch (op) {
                case 1 -> gestionarEvento(leerInt("Id del evento: "));
                case 2 -> listar(eventoDAO.buscarPorCumpleanero(leerTexto("Nombre: ")));
                case 3 -> listar(eventoDAO.buscarPorTelefono(leerTexto("Teléfono: ")));
                case 4 -> listar(eventoDAO.buscarPorFecha(leerFecha("Fecha (dd/mm/aaaa): ")));
                case 0 -> { }
                default -> System.out.println("Opción inválida");
            }
        } while (op != 0);
    }

    private static void gestionarEvento(int id) throws ValidacionException, AccesoDatosException {
        int op;
        do {
            Optional<Evento> opt = eventoDAO.buscarPorId(id);
            if (opt.isEmpty()) {
                System.out.println("No existe un evento con ese id");
                return;
            }
            Evento ev = opt.get();
            mostrarDetalle(ev);
            System.out.println("1. Editar");
            System.out.println("2. Registrar pago y marcar realizado");
            System.out.println("3. Cancelar evento");
            System.out.println("0. Volver");
            op = leerInt("Opción: ");
            switch (op) {
                case 1 -> editar(ev);
                case 2 -> {
                    if (leerSiNo("¿Registrar el pago completo y marcar realizado?")) {
                        ev.registrarPagoCompleto();
                        ev.marcarRealizado();
                        eventoDAO.modificar(ev);
                        System.out.println("Evento realizado. Pasó al historial.");
                        return;
                    }
                }
                case 3 -> {
                    if (leerSiNo("¿Estás seguro de cancelar?")) {
                        ev.cancelar();
                        eventoDAO.modificar(ev);
                        System.out.println("Evento cancelado. Pasó al historial.");
                        return;
                    }
                }
                case 0 -> { }
                default -> System.out.println("Opción inválida");
            }
        } while (op != 0);
    }

    private static void editar(Evento ev) throws ValidacionException, AccesoDatosException {
        double seniaCobrada = ev.getMontoSenia(); // la seña ya cobrada se mantiene
        int op;
        do {
            System.out.println("\nEditar: 1 Fecha  2 Hora  3 Feriado  4 Cumpleañero  5 Cliente");
            System.out.println("        6 Niños  7 Adultos  8 Extras  9 Catering  0 Terminar");
            op = leerInt("Opción: ");
            try {
                switch (op) {
                    case 1 -> ev.setFecha(leerFecha("Nueva fecha: "));
                    case 2 -> ev.setHoraInicio(leerHora("Nueva hora: "));
                    case 3 -> ev.setEsFeriado(leerSiNo("¿Es feriado?"));
                    case 4 -> ev.setNombreCumpleanero(leerTexto("Cumpleañero: "));
                    case 5 -> {
                        ev.getCliente().setNombre(leerTexto("Nombre del cliente: "));
                        ev.getCliente().setTelefono(leerTexto("Teléfono: "));
                    }
                    case 6 -> ev.setCantidadNinios(leerInt("Niños: "));
                    case 7 -> ev.setCantidadAdultos(leerInt("Adultos: "));
                    case 8 -> {
                        int[] x = leerExtras();
                        ev.setMediaHoraExtra(x[0]);
                        ev.setHoraExtra(x[1]);
                        ev.setAnimadoraAdicional(x[2]);
                        ev.setCumpleaneroExtra(x[3]);
                    }
                    case 9 -> ev.setOpcionCatering(elegirCatering());
                    case 0 -> { }
                    default -> System.out.println("Opción inválida");
                }
            } catch (ValidacionException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (op != 0);

        cotizador.calcularTotal(ev);
        ev.setMontoSenia(seniaCobrada);
        if (!disponibilidad.verificarDisponibilidad(ev.getFecha(), ev.getHoraInicio(),
                ev.getDuracion(), Optional.of(ev.getId()))) {
            System.out.println("Ese horario ya está ocupado por otro evento. Cambios descartados.");
            return;
        }
        mostrarDetalle(ev);
        if (leerSiNo("¿Guardar cambios?")) {
            clienteDAO.modificar(ev.getCliente());
            eventoDAO.modificar(ev);
            System.out.println("Cambios guardados");
        } else {
            System.out.println("Edición cancelada");
        }
    }

    // ---------------------------------------------------------------- 3. HISTORIAL

    private static void verHistorial() throws AccesoDatosException {
        int op;
        do {
            System.out.println("\n--- HISTORIAL ---");
            listar(eventoDAO.buscarHistorial());
            System.out.println("1. Ver detalle (por id)");
            System.out.println("0. Volver");
            op = leerInt("Opción: ");
            if (op == 1) {
                Optional<Evento> ev = eventoDAO.buscarPorId(leerInt("Id del evento: "));
                if (ev.isPresent()) mostrarDetalle(ev.get());
                else System.out.println("No existe un evento con ese id");
            }
        } while (op != 0);
    }

    // ---------------------------------------------------------------- 4. RECAUDACIÓN

    private static void verRecaudacion() throws ValidacionException, AccesoDatosException {
        YearMonth mes = leerMes("Mes (mm/aaaa): ");
        System.out.println("\n--- RECAUDACIÓN " + mes.format(MES) + " ---");
        System.out.println("Total del mes: " + recaudacion.calcularTotalMes(mes));
        System.out.println("Comparación con el mes anterior: " + recaudacion.compararConMesAnterior(mes));
        int rango = leerInt("Promedio de los últimos meses (3, 6 o 12): ");
        System.out.println("Promedio: " + recaudacion.calcularPromedio(rango));
    }

    // ---------------------------------------------------------------- 5. OPCIONES

    private static void opciones() throws ValidacionException, AccesoDatosException {
        int op;
        do {
            System.out.println("\n--- OPCIONES ---");
            System.out.println("1. Ver tarifas, precios y paquete base");
            System.out.println("2. Editar tarifas, precios y paquete base");
            System.out.println("3. Menús de catering");
            System.out.println("0. Volver");
            op = leerInt("Opción: ");
            switch (op) {
                case 1 -> mostrarConfig(configDAO.leer());
                case 2 -> editarConfig();
                case 3 -> menusCatering();
                case 0 -> { }
                default -> System.out.println("Opción inválida");
            }
        } while (op != 0);
    }

    private static void mostrarConfig(Configuracion c) {
        System.out.println("1. Tarifa base lunes a jueves: " + c.getTarifaBaseSemana());
        System.out.println("2. Tarifa base fin de semana/feriados: " + c.getTarifaBaseFinde());
        System.out.println("3. Niño extra: " + c.getPrecioNinioExtra());
        System.out.println("4. Adulto extra: " + c.getPrecioAdultoExtra());
        System.out.println("5. Media hora extra: " + c.getPrecioMediaHoraExtra());
        System.out.println("6. Hora extra: " + c.getPrecioHoraExtra());
        System.out.println("7. Animadora adicional: " + c.getPrecioAnimadoraAdicional());
        System.out.println("8. Cumpleañero extra: " + c.getPrecioCumpleaneroExtra());
        System.out.println("9. Niños incluidos: " + c.getNiniosIncluidos());
        System.out.println("10. Adultos incluidos: " + c.getAdultosIncluidos());
    }

    private static void editarConfig() throws ValidacionException, AccesoDatosException {
        Configuracion c = configDAO.leer();
        mostrarConfig(c);
        int n = leerInt("¿Cuál querés editar? (0 para volver): ");
        if (n == 0) return;
        switch (n) {
            case 1 -> c.setTarifaBaseSemana(leerDouble("Nuevo valor: "));
            case 2 -> c.setTarifaBaseFinde(leerDouble("Nuevo valor: "));
            case 3 -> c.setPrecioNinioExtra(leerDouble("Nuevo valor: "));
            case 4 -> c.setPrecioAdultoExtra(leerDouble("Nuevo valor: "));
            case 5 -> c.setPrecioMediaHoraExtra(leerDouble("Nuevo valor: "));
            case 6 -> c.setPrecioHoraExtra(leerDouble("Nuevo valor: "));
            case 7 -> c.setPrecioAnimadoraAdicional(leerDouble("Nuevo valor: "));
            case 8 -> c.setPrecioCumpleaneroExtra(leerDouble("Nuevo valor: "));
            case 9 -> c.setNiniosIncluidos(leerInt("Nuevo valor: "));
            case 10 -> c.setAdultosIncluidos(leerInt("Nuevo valor: "));
            default -> { System.out.println("Opción inválida"); return; }
        }
        configDAO.actualizar(c);
        System.out.println("Guardado");
    }

    private static void menusCatering() throws ValidacionException, AccesoDatosException {
        int op;
        do {
            System.out.println("\n--- MENÚS DE CATERING ---");
            for (OpcionCatering o : cateringDAO.buscarActivas()) {
                System.out.println("- " + o.getNombre() + ": " + o.getPrecioPorPersona() + " por persona");
            }
            System.out.println("1. Agregar menú");
            System.out.println("2. Cambiar precio");
            System.out.println("3. Borrar (ocultar) menú");
            System.out.println("0. Volver");
            op = leerInt("Opción: ");
            switch (op) {
                case 1 -> {
                    String nombre = leerTexto("Nombre: ");
                    cateringDAO.agregar(new OpcionCatering(nombre, leerDouble("Precio por persona: ")));
                    System.out.println("Menú agregado");
                }
                case 2 -> {
                    OpcionCatering o = elegirCatering();
                    o.setPrecioPorPersona(leerDouble("Nuevo precio por persona: "));
                    cateringDAO.modificarPrecio(o);
                    System.out.println("Precio actualizado");
                }
                case 3 -> {
                    OpcionCatering o = elegirCatering();
                    if (leerSiNo("¿Ocultar " + o.getNombre() + "?")) {
                        cateringDAO.ocultar(o.getNombre());
                        System.out.println("Menú oculto");
                    }
                }
                case 0 -> { }
                default -> System.out.println("Opción inválida");
            }
        } while (op != 0);
    }

    // ---------------------------------------------------------------- PANTALLAS AUXILIARES

    private static OpcionCatering elegirCatering() throws ValidacionException, AccesoDatosException {
        List<OpcionCatering> lista = cateringDAO.buscarActivas();
        if (lista.isEmpty()) throw new ValidacionException("No hay menús de catering activos");
        for (int i = 0; i < lista.size(); i++) {
            OpcionCatering o = lista.get(i);
            System.out.println((i + 1) + ". " + o.getNombre() + " (" + o.getPrecioPorPersona() + " por persona)");
        }
        int n;
        do {
            n = leerInt("Catering: ");
        } while (n < 1 || n > lista.size());
        return lista.get(n - 1);
    }

    private static void listar(List<Evento> lista) {
        if (lista.isEmpty()) {
            System.out.println("(sin eventos)");
            return;
        }
        for (Evento e : lista) {
            String marca = "";
            if (e.getEstado() == EstadoEvento.ACTIVO && e.getFecha().isBefore(LocalDate.now())) {
                marca = "  [PENDIENTE DE COBRO]";
            } else if (e.getEstado() != EstadoEvento.ACTIVO) {
                marca = "  [" + e.getEstado() + "]";
            }
            System.out.println("#" + e.getId() + "  " + e.getNombreCumpleanero() + "  "
                    + e.getFecha().format(FECHA) + " " + e.getHoraInicio() + marca);
        }
    }

    private static void mostrarDetalle(Evento e) {
        double saldo = e.getEstadoPago() == EstadoPago.SENIADO ? e.getMontoTotal() - e.getMontoSenia() : 0;
        System.out.println("\n--- DETALLE ---");
        System.out.println("Cliente: " + e.getCliente().getNombre() + " (" + e.getCliente().getTelefono() + ")");
        System.out.println("Cumpleañero: " + e.getNombreCumpleanero());
        System.out.println("Fecha y hora: " + e.getFecha().format(FECHA) + " " + e.getHoraInicio()
                + " (" + e.getDuracion() + " min)");
        System.out.println("Niños: " + e.getCantidadNinios() + " | Adultos: " + e.getCantidadAdultos());
        System.out.println("Extras: media hora " + e.getMediaHoraExtra() + ", hora " + e.getHoraExtra()
                + ", animadora " + e.getAnimadoraAdicional() + ", cumpleañero " + e.getCumpleaneroExtra());
        System.out.println("Catering: " + e.getOpcionCatering().getNombre());
        System.out.println("Total: " + e.getMontoTotal() + " | Seña: " + e.getMontoSenia()
                + " | Saldo pendiente: " + saldo);
        System.out.println("Estado de pago: " + e.getEstadoPago() + " | Estado: " + e.getEstado());
    }

    // ---------------------------------------------------------------- LECTURA DE DATOS

    private static String leerTexto(String msg) {
        System.out.print(msg);
        return in.nextLine().trim();
    }

    private static int leerInt(String msg) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(msg));
            } catch (NumberFormatException e) {
                System.out.println("Ingresá un número entero");
            }
        }
    }

    private static double leerDouble(String msg) {
        while (true) {
            try {
                return Double.parseDouble(leerTexto(msg).replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Ingresá un número");
            }
        }
    }

    private static boolean leerSiNo(String msg) {
        return leerTexto(msg + " (s/n): ").equalsIgnoreCase("s");
    }

    private static LocalDate leerFecha(String msg) {
        while (true) {
            try {
                return LocalDate.parse(leerTexto(msg), FECHA);
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido, usá dd/mm/aaaa");
            }
        }
    }

    private static LocalTime leerHora(String msg) {
        while (true) {
            try {
                return LocalTime.parse(leerTexto(msg));
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido, usá hh:mm");
            }
        }
    }

    private static YearMonth leerMes(String msg) {
        while (true) {
            try {
                return YearMonth.parse(leerTexto(msg), MES);
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido, usá mm/aaaa");
            }
        }
    }
}
