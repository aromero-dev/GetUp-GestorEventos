package interfaz;

import dao.AccesoDatosException;
import dao.ClienteDAO;
import dao.ConfiguracionDAO;
import dao.EventoDAO;
import dao.OpcionCateringDAO;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import modelo.Cliente;
import modelo.Evento;
import modelo.OpcionCatering;
import modelo.ValidacionException;
import servicio.CotizadorService;
import servicio.DisponibilidadService;
import servicio.EventoService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public class PantallaNuevoEventoPaso2 {

    public static Parent crear(LocalDate fecha, LocalTime horaInicio, boolean esFeriado) {
        Button volver = new Button("← Volver");

        Label titulo = new Label("Nuevo evento: datos y cotización");
        Label resumen = new Label("Fecha: " + fecha + " | Hora: " + horaInicio
                + (esFeriado ? " | Feriado" : ""));

        // Bloque 1: datos del cliente
        TextField nombre = new TextField();
        nombre.setPromptText("Nombre de quien contrata");
        TextField telefono = new TextField();
        telefono.setPromptText("Ej: 1155556666");
        TextField cumpleanero = new TextField();
        cumpleanero.setPromptText("Nombre del cumpleañero");

        VBox bloqueCliente = crearBloque(
                new Label("Datos del cliente"),
                new Label("Nombre"), nombre,
                new Label("Teléfono"), telefono,
                new Label("Cumpleañero"), cumpleanero);

        // Bloque 2: niños, adultos y extras
        Spinner<Integer> ninios = new Spinner<>(1, 1000, 30);
        Spinner<Integer> adultos = new Spinner<>(1, 1000, 15);
        Spinner<Integer> mediaHora = new Spinner<>(0, 20, 0);
        Spinner<Integer> horaExtra = new Spinner<>(0, 20, 0);
        Spinner<Integer> animadora = new Spinner<>(0, 20, 0);
        Spinner<Integer> cumpleExtra = new Spinner<>(0, 20, 0);

        VBox bloquePersonas = crearBloque(
                new Label("Niños, adultos y extras"),
                new Label("Cantidad de niños"), ninios,
                new Label("Cantidad de adultos"), adultos,
                new Label("Media hora extra"), mediaHora,
                new Label("Hora extra"), horaExtra,
                new Label("Animadora adicional"), animadora,
                new Label("Cumpleañero extra"), cumpleExtra);

        // Bloque 3: catering
        ComboBox<OpcionCatering> catering = new ComboBox<>();
        catering.setPromptText("Elegí una opción");
        catering.setConverter(new StringConverter<>() {
            @Override
            public String toString(OpcionCatering o) {
                return o == null ? "" : o.getNombre() + " ($" + o.getPrecioPorPersona() + " por persona)";
            }

            @Override
            public OpcionCatering fromString(String s) {
                return null;
            }
        });
        Label avisoCatering = new Label();

        try {
            catering.getItems().addAll(new OpcionCateringDAO().buscarActivas());
            if (catering.getItems().isEmpty()) {
                avisoCatering.setText("No hay menús de catering cargados");
            }
        } catch (AccesoDatosException ex) {
            avisoCatering.setText(ex.getMessage());
        }

        VBox bloqueCatering = crearBloque(
                new Label("Catering"), catering, avisoCatering);

        // Bloque 4: resumen de cotización
        CotizadorService cotizador = new CotizadorService(new ConfiguracionDAO());
        DisponibilidadService disponibilidad = new DisponibilidadService(new EventoDAO());
        Evento[] cotizado = new Evento[1]; // evento listo para guardar

        Button generar = new Button("Generar resumen");
        generar.getStyleClass().addAll("tarjeta", "ver");
        generar.setPrefSize(260, 60);
        Label resultado = new Label();

        generar.setOnAction(e -> {
            cotizado[0] = null;
            try {
                Cliente cliente = new Cliente(nombre.getText(), telefono.getText());
                Evento evento = new Evento(cliente, cumpleanero.getText(), fecha, horaInicio,
                        esFeriado, ninios.getValue(), adultos.getValue(), catering.getValue());
                evento.setMediaHoraExtra(mediaHora.getValue());
                evento.setHoraExtra(horaExtra.getValue());
                evento.setAnimadoraAdicional(animadora.getValue());
                evento.setCumpleaneroExtra(cumpleExtra.getValue());

                CotizadorService.Desglose d = cotizador.calcularTotal(evento);

                boolean libre = disponibilidad.verificarDisponibilidad(
                        fecha, horaInicio, evento.getDuracion(), Optional.empty());
                if (!libre) {
                    resultado.setText("Con esta duración el horario se superpone con otro evento. "
                            + "Ajustá los extras o volvé al paso 1.");
                    return;
                }
                cotizado[0] = evento;
                resultado.setText(
                        "Salón (tarifa base y personas extra): " + dinero(d.salon()) + "\n"
                                + "Extras: " + dinero(d.extras()) + "\n"
                                + "Catering: " + dinero(d.catering()) + "\n"
                                + "Total: " + dinero(d.total()) + "\n"
                                + "Seña (50%): " + dinero(d.senia()) + "\n"
                                + "Duración: " + evento.getDuracion() + " minutos");
            } catch (ValidacionException | AccesoDatosException ex) {
                resultado.setText(ex.getMessage());
            }
        });

        VBox bloqueResumen = crearBloque(
                new Label("Resumen de cotización"), generar, resultado);

        // Bloque 5: confirmar y guardar
        EventoService eventoService = new EventoService(
                new ClienteDAO(), new EventoDAO(), disponibilidad);

        Button guardar = new Button("Confirmar y guardar");
        guardar.getStyleClass().addAll("tarjeta", "nuevo");
        guardar.setPrefSize(260, 60);
        Label avisoGuardar = new Label();

        guardar.setOnAction(e -> {
            if (cotizado[0] == null) {
                avisoGuardar.setText("Primero generá el resumen de cotización");
                return;
            }
            try {
                eventoService.guardarNuevo(cotizado[0]);
                Alert exito = new Alert(Alert.AlertType.INFORMATION, "Evento guardado con éxito");
                exito.setHeaderText(null);
                exito.showAndWait();
                Navegador.ir(PantallaMenu.crear());
            } catch (ValidacionException | AccesoDatosException ex) {
                avisoGuardar.setText(ex.getMessage());
            }
        });

        VBox bloqueGuardar = crearBloque(guardar, avisoGuardar);

        // si se cambia cualquier dato, el resumen anterior ya no sirve
        // y se marca que hay datos cargados (para el cartel de salir)
        boolean[] hayDatos = new boolean[1];

        Runnable invalidar = () -> {
            hayDatos[0] = true;
            cotizado[0] = null;
            resultado.setText("");
            avisoGuardar.setText("");
        };
        nombre.textProperty().addListener((o, a, b) -> invalidar.run());
        telefono.textProperty().addListener((o, a, b) -> invalidar.run());
        cumpleanero.textProperty().addListener((o, a, b) -> invalidar.run());
        ninios.valueProperty().addListener((o, a, b) -> invalidar.run());
        adultos.valueProperty().addListener((o, a, b) -> invalidar.run());
        mediaHora.valueProperty().addListener((o, a, b) -> invalidar.run());
        horaExtra.valueProperty().addListener((o, a, b) -> invalidar.run());
        animadora.valueProperty().addListener((o, a, b) -> invalidar.run());
        cumpleExtra.valueProperty().addListener((o, a, b) -> invalidar.run());
        catering.valueProperty().addListener((o, a, b) -> invalidar.run());

        // Volver: sin datos va directo al paso 1; con datos pregunta antes
        volver.setOnAction(e -> {
            if (!hayDatos[0]) {
                Navegador.ir(PantallaNuevoEvento.crear());
                return;
            }
            ButtonType salir = new ButtonType("Salir", ButtonBar.ButtonData.OK_DONE);
            ButtonType seguir = new ButtonType("Seguir editando", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert pregunta = new Alert(Alert.AlertType.CONFIRMATION,
                    "Se van a perder los datos cargados.", salir, seguir);
            pregunta.setHeaderText("¿Salir sin guardar?");
            Optional<ButtonType> respuesta = pregunta.showAndWait();
            if (respuesta.isPresent() && respuesta.get() == salir) {
                Navegador.ir(PantallaNuevoEvento.crear());
            }
        });

        VBox contenido = new VBox(20, volver, titulo, resumen,
                bloqueCliente, bloquePersonas, bloqueCatering, bloqueResumen, bloqueGuardar);
        contenido.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private static String dinero(double monto) {
        return String.format("$%.2f", monto);
    }

    private static VBox crearBloque(Node... hijos) {
        VBox bloque = new VBox(10, hijos);
        bloque.setPadding(new Insets(15));
        bloque.setMaxWidth(400);
        bloque.getStyleClass().add("bloque");
        return bloque;
    }
}