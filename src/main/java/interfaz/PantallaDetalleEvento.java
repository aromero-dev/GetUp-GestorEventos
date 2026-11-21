package interfaz;

import dao.AccesoDatosException;
import dao.ClienteDAO;
import dao.EventoDAO;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import modelo.EstadoEvento;
import modelo.EstadoPago;
import modelo.Evento;
import modelo.ValidacionException;
import servicio.DisponibilidadService;
import servicio.EventoService;

import java.util.Optional;
import java.util.function.Supplier;

public class PantallaDetalleEvento {

    // origen: cómo reconstruir la pantalla a la que hay que volver
    public static Parent crear(Evento ev, Supplier<Parent> origen) {
        Button volver = new Button("← Volver");
        volver.setOnAction(e -> Navegador.ir(origen.get()));

        Label titulo = new Label("Detalle del evento");

        boolean pagado = ev.getEstadoPago() == EstadoPago.PAGADO;
        double saldo = pagado ? 0 : ev.getMontoTotal() - ev.getMontoSenia();

        VBox bloqueEvento = crearBloque(
                new Label("Evento"),
                new Label("Cumpleañero: " + ev.getNombreCumpleanero()),
                new Label("Fecha: " + ev.getFecha()),
                new Label("Hora de inicio: " + ev.getHoraInicio()),
                new Label("Duración: " + ev.getDuracion() + " minutos"),
                new Label("Feriado: " + (ev.isEsFeriado() ? "Sí" : "No")));

        VBox bloqueCliente = crearBloque(
                new Label("Cliente"),
                new Label("Nombre: " + ev.getCliente().getNombre()),
                new Label("Teléfono: " + ev.getCliente().getTelefono()));

        VBox bloquePersonas = crearBloque(
                new Label("Niños, adultos y extras"),
                new Label("Niños: " + ev.getCantidadNinios()),
                new Label("Adultos: " + ev.getCantidadAdultos()),
                new Label("Media hora extra: " + ev.getMediaHoraExtra()),
                new Label("Hora extra: " + ev.getHoraExtra()),
                new Label("Animadora adicional: " + ev.getAnimadoraAdicional()),
                new Label("Cumpleañero extra: " + ev.getCumpleaneroExtra()));

        VBox bloqueCatering = crearBloque(
                new Label("Catering"),
                new Label(ev.getOpcionCatering().getNombre()
                        + " ($" + ev.getOpcionCatering().getPrecioPorPersona() + " por persona)"));

        VBox bloquePago = crearBloque(
                new Label("Pago"),
                new Label("Estado de pago: " + (pagado ? "Pagado" : "Señado")),
                new Label("Total: $" + ev.getMontoTotal()),
                new Label("Seña: $" + ev.getMontoSenia()),
                new Label("Saldo pendiente: $" + saldo));

        VBox contenido = new VBox(20, volver, titulo, bloqueEvento, bloqueCliente,
                bloquePersonas, bloqueCatering, bloquePago);

        // acciones: solo para eventos activos
        if (ev.getEstado() == EstadoEvento.ACTIVO) {
            EventoService servicio = new EventoService(new ClienteDAO(), new EventoDAO(),
                    new DisponibilidadService(new EventoDAO()));
            Label avisoAccion = new Label();

            Button editar = new Button("Editar evento");
            editar.getStyleClass().addAll("tarjeta", "ver");
            editar.setPrefSize(320, 60);
            editar.setOnAction(e -> Navegador.ir(PantallaEditarEvento.crear(ev, origen)));

            Button registrarPago = new Button("Registrar pago y marcar realizado");
            registrarPago.getStyleClass().addAll("tarjeta", "nuevo");
            registrarPago.setPrefSize(320, 60);
            registrarPago.setOnAction(e -> {
                if (!confirmar("¿Registrar el pago completo?",
                        "El evento se da por realizado y pasa al historial.")) {
                    return;
                }
                try {
                    servicio.registrarPagoYMarcarRealizado(ev);
                    mostrarExito("Pago registrado. El evento quedó como realizado.");
                    Navegador.ir(PantallaVerEventos.crear());
                } catch (ValidacionException | AccesoDatosException ex) {
                    avisoAccion.setText(ex.getMessage());
                }
            });

            Button cancelar = new Button("Cancelar evento");
            cancelar.getStyleClass().addAll("tarjeta", "cancelar");
            cancelar.setPrefSize(320, 60);
            cancelar.setOnAction(e -> {
                if (!confirmar("¿Estás seguro?",
                        "El evento pasa al historial como cancelado. La seña no se devuelve.")) {
                    return;
                }
                try {
                    servicio.cancelar(ev);
                    mostrarExito("Evento cancelado.");
                    Navegador.ir(PantallaVerEventos.crear());
                } catch (ValidacionException | AccesoDatosException ex) {
                    avisoAccion.setText(ex.getMessage());
                }
            });

            contenido.getChildren().addAll(editar, registrarPago, cancelar, avisoAccion);
        }

        contenido.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private static boolean confirmar(String encabezado, String mensaje) {
        ButtonType si = new ButtonType("Sí", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, si, no);
        alerta.setHeaderText(encabezado);
        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == si;
    }

    private static void mostrarExito(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    private static VBox crearBloque(Node... hijos) {
        VBox bloque = new VBox(8, hijos);
        bloque.setPadding(new Insets(15));
        bloque.setMaxWidth(400);
        bloque.getStyleClass().add("bloque");
        return bloque;
    }
}