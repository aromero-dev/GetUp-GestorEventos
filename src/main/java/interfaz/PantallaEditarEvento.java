package interfaz;

import dao.AccesoDatosException;
import dao.ClienteDAO;
import dao.ConfiguracionDAO;
import dao.EventoDAO;
import dao.OpcionCateringDAO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import modelo.Cliente;
import modelo.Evento;
import modelo.OpcionCatering;
import modelo.ValidacionException;
import servicio.CotizadorService;
import servicio.DisponibilidadService;
import servicio.EventoService;

import java.time.LocalTime;
import java.util.Optional;
import java.util.function.Supplier;

public class PantallaEditarEvento {

    // origen: pantalla a la que se vuelve (la lista de la que se vino)
    public static Parent crear(Evento original, Supplier<Parent> origen) {
        Evento[] actual = { original };

        Button volver = new Button("← Volver");
        Label titulo = new Label("Editar evento");
        Label aviso = new Label();
        SimpleBooleanProperty hayCambios = new SimpleBooleanProperty(false);

        // Bloque 1: fecha y hora
        DatePicker fecha = new DatePicker(original.getFecha());
        Spinner<Integer> hora = new Spinner<>(0, 23, original.getHoraInicio().getHour());
        Spinner<Integer> minutos = new Spinner<>(0, 59, original.getHoraInicio().getMinute(), 5);
        hora.setPrefWidth(80);
        minutos.setPrefWidth(80);
        HBox horaInicio = new HBox(10, hora, new Label(":"), minutos);
        horaInicio.setAlignment(Pos.CENTER_LEFT);
        CheckBox feriado = new CheckBox("Es feriado");
        feriado.setSelected(original.isEsFeriado());

        VBox bloqueFecha = crearBloque(
                new Label("Fecha y hora"),
                new Label("Fecha"), fecha,
                new Label("Hora de inicio"), horaInicio,
                feriado);

        // Bloque 2: cliente y cumpleañero
        TextField nombre = new TextField(original.getCliente().getNombre());
        TextField telefono = new TextField(original.getCliente().getTelefono());
        TextField cumpleanero = new TextField(original.getNombreCumpleanero());
        VBox bloqueCliente = crearBloque(
                new Label("Cliente"),
                new Label("Nombre"), nombre,
                new Label("Teléfono"), telefono,
                new Label("Cumpleañero"), cumpleanero);

        // Bloque 3: niños, adultos y extras
        Spinner<Integer> ninios = new Spinner<>(1, 1000, original.getCantidadNinios());
        Spinner<Integer> adultos = new Spinner<>(1, 1000, original.getCantidadAdultos());
        Spinner<Integer> mediaHora = new Spinner<>(0, 20, original.getMediaHoraExtra());
        Spinner<Integer> horaExtra = new Spinner<>(0, 20, original.getHoraExtra());
        Spinner<Integer> animadora = new Spinner<>(0, 20, original.getAnimadoraAdicional());
        Spinner<Integer> cumpleExtra = new Spinner<>(0, 20, original.getCumpleaneroExtra());
        VBox bloquePersonas = crearBloque(
                new Label("Niños, adultos y extras"),
                new Label("Cantidad de niños"), ninios,
                new Label("Cantidad de adultos"), adultos,
                new Label("Media hora extra"), mediaHora,
                new Label("Hora extra"), horaExtra,
                new Label("Animadora adicional"), animadora,
                new Label("Cumpleañero extra"), cumpleExtra);

        // Bloque 4: catering (si el actual está oculto, igual aparece para no perderlo)
        ComboBox<OpcionCatering> catering = new ComboBox<>();
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
        try {
            catering.getItems().addAll(new OpcionCateringDAO().buscarActivas());
        } catch (AccesoDatosException ex) {
            aviso.setText(ex.getMessage());
        }
        OpcionCatering actualCatering = null;
        for (OpcionCatering op : catering.getItems()) {
            if (op.getNombre().equals(original.getOpcionCatering().getNombre())) {
                actualCatering = op;
            }
        }
        if (actualCatering == null) {
            actualCatering = original.getOpcionCatering();
            catering.getItems().add(actualCatering);
        }
        catering.setValue(actualCatering);
        VBox bloqueCatering = crearBloque(new Label("Catering"), catering);

        // Pago: solo lectura
        VBox bloquePago = crearBloque(
                new Label("Pago (solo lectura)"),
                new Label("Total actual: $" + original.getMontoTotal()),
                new Label("Seña cobrada: $" + original.getMontoSenia()),
                new Label("Al guardar se recalcula el total con los precios vigentes; "
                        + "la seña ya cobrada se mantiene."));

        // Botones de edición: solo aparecen con cambios pendientes
        EventoService servicio = new EventoService(new ClienteDAO(), new EventoDAO(),
                new DisponibilidadService(new EventoDAO()));
        CotizadorService cotizador = new CotizadorService(new ConfiguracionDAO());

        Button guardar = new Button("Guardar cambios");
        guardar.getStyleClass().addAll("tarjeta", "nuevo");
        guardar.setPrefSize(260, 60);
        Button cancelarEdicion = new Button("Cancelar edición");
        cancelarEdicion.getStyleClass().addAll("tarjeta", "cancelar");
        cancelarEdicion.setPrefSize(260, 60);
        for (Button b : new Button[] { guardar, cancelarEdicion }) {
            b.visibleProperty().bind(hayCambios);
            b.managedProperty().bind(hayCambios);
        }

        guardar.setOnAction(e -> {
            aviso.setText("");
            try {
                Evento ev = actual[0];
                if (fecha.getValue() == null) {
                    throw new ValidacionException("La fecha es obligatoria");
                }
                if (catering.getValue() == null) {
                    throw new ValidacionException("Elegí una opción de catering");
                }

                Cliente cliente = ev.getCliente();
                cliente.setNombre(nombre.getText());
                cliente.setTelefono(telefono.getText());

                ev.setNombreCumpleanero(cumpleanero.getText());
                // la fecha solo se revalida si el usuario la cambió (un evento vencido sigue siendo editable)
                if (!fecha.getValue().equals(ev.getFecha())) {
                    ev.setFecha(fecha.getValue());
                }
                ev.setHoraInicio(LocalTime.of(hora.getValue(), minutos.getValue()));
                ev.setEsFeriado(feriado.isSelected());
                ev.setCantidadNinios(ninios.getValue());
                ev.setCantidadAdultos(adultos.getValue());
                ev.setMediaHoraExtra(mediaHora.getValue());
                ev.setHoraExtra(horaExtra.getValue());
                ev.setAnimadoraAdicional(animadora.getValue());
                ev.setCumpleaneroExtra(cumpleExtra.getValue());
                if (!catering.getValue().getNombre().equals(ev.getOpcionCatering().getNombre())) {
                    ev.setOpcionCatering(catering.getValue());
                }

                servicio.guardarEdicion(ev, cotizador);

                Alert exito = new Alert(Alert.AlertType.INFORMATION, "Cambios guardados");
                exito.setHeaderText(null);
                exito.showAndWait();
                Navegador.ir(PantallaDetalleEvento.crear(recargar(ev), origen));
            } catch (ValidacionException | AccesoDatosException ex) {
                aviso.setText(ex.getMessage());
                // el evento pudo quedar a medio modificar en memoria: se vuelve a leer de la base
                actual[0] = recargar(actual[0]);
            }
        });

        cancelarEdicion.setOnAction(e ->
                Navegador.ir(PantallaDetalleEvento.crear(recargar(actual[0]), origen)));

        volver.setOnAction(e -> {
            if (!hayCambios.get()) {
                Navegador.ir(origen.get());
                return;
            }
            ButtonType salir = new ButtonType("Salir", ButtonBar.ButtonData.OK_DONE);
            ButtonType seguir = new ButtonType("Seguir editando", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert pregunta = new Alert(Alert.AlertType.CONFIRMATION,
                    "Se van a perder los cambios.", salir, seguir);
            pregunta.setHeaderText("¿Salir sin guardar los cambios?");
            Optional<ButtonType> respuesta = pregunta.showAndWait();
            if (respuesta.isPresent() && respuesta.get() == salir) {
                Navegador.ir(origen.get());
            }
        });

        // cualquier cambio en un campo muestra Guardar cambios y Cancelar edición
        Runnable marcar = () -> {
            hayCambios.set(true);
            aviso.setText("");
        };
        fecha.valueProperty().addListener((o, a, b) -> marcar.run());
        hora.valueProperty().addListener((o, a, b) -> marcar.run());
        minutos.valueProperty().addListener((o, a, b) -> marcar.run());
        feriado.selectedProperty().addListener((o, a, b) -> marcar.run());
        nombre.textProperty().addListener((o, a, b) -> marcar.run());
        telefono.textProperty().addListener((o, a, b) -> marcar.run());
        cumpleanero.textProperty().addListener((o, a, b) -> marcar.run());
        ninios.valueProperty().addListener((o, a, b) -> marcar.run());
        adultos.valueProperty().addListener((o, a, b) -> marcar.run());
        mediaHora.valueProperty().addListener((o, a, b) -> marcar.run());
        horaExtra.valueProperty().addListener((o, a, b) -> marcar.run());
        animadora.valueProperty().addListener((o, a, b) -> marcar.run());
        cumpleExtra.valueProperty().addListener((o, a, b) -> marcar.run());
        catering.valueProperty().addListener((o, a, b) -> marcar.run());

        VBox contenido = new VBox(20, volver, titulo, bloqueFecha, bloqueCliente,
                bloquePersonas, bloqueCatering, bloquePago, guardar, cancelarEdicion, aviso);
        contenido.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private static Evento recargar(Evento ev) {
        try {
            return new EventoDAO().buscarPorId(ev.getId()).orElse(ev);
        } catch (AccesoDatosException ex) {
            return ev;
        }
    }

    private static VBox crearBloque(Node... hijos) {
        VBox bloque = new VBox(8, hijos);
        bloque.setPadding(new Insets(15));
        bloque.setMaxWidth(400);
        bloque.getStyleClass().add("bloque");
        return bloque;
    }
}