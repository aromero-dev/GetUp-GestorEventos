package interfaz;

import dao.AccesoDatosException;
import dao.EventoDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import modelo.EstadoEvento;
import modelo.Evento;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PantallaVerEventos {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static Parent crear() {
        Button volver = new Button("← Volver");
        volver.setOnAction(e -> Navegador.ir(PantallaMenu.crear()));

        Label titulo = new Label("Ver eventos");

        TextField busqueda = new TextField();
        busqueda.setPromptText("Buscar por cumpleañero, fecha (dd/mm/aaaa) o teléfono");
        busqueda.setPrefWidth(380);
        Button buscar = new Button("Buscar");
        HBox barra = new HBox(10, busqueda, buscar);
        barra.setAlignment(Pos.CENTER_LEFT);

        Label aviso = new Label();
        VBox lista = new VBox(10);

        Runnable cargar = () -> cargarLista(lista, aviso, busqueda.getText().trim());
        buscar.setOnAction(e -> cargar.run());
        busqueda.setOnAction(e -> cargar.run()); // Enter también busca
        cargar.run();

        VBox contenido = new VBox(20, volver, titulo, barra, aviso, lista);
        contenido.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private static void cargarLista(VBox lista, Label aviso, String texto) {
        lista.getChildren().clear();
        aviso.setText("");
        try {
            List<Evento> eventos = buscarSegunTexto(texto);
            if (eventos.isEmpty()) {
                aviso.setText(texto.isEmpty() ? "No hay eventos activos" : "No se encontraron eventos");
            }
            for (Evento ev : eventos) {
                lista.getChildren().add(crearFila(ev));
            }
        } catch (AccesoDatosException ex) {
            aviso.setText(ex.getMessage());
        }
    }

    private static List<Evento> buscarSegunTexto(String texto) throws AccesoDatosException {
        EventoDAO dao = new EventoDAO();
        if (texto.isEmpty()) {
            return dao.buscarProximos();
        }
        List<Evento> encontrados;
        if (texto.contains("/")) {
            try {
                encontrados = dao.buscarPorFecha(LocalDate.parse(texto, FORMATO_FECHA));
            } catch (DateTimeParseException ex) {
                throw new AccesoDatosException("Fecha inválida. Usá el formato dd/mm/aaaa");
            }
        } else if (texto.matches("\\d+")) {
            encontrados = dao.buscarPorTelefono(texto);
        } else {
            encontrados = dao.buscarPorCumpleanero(texto);
        }
        // esta pantalla muestra solo eventos activos
        return encontrados.stream()
                .filter(ev -> ev.getEstado() == EstadoEvento.ACTIVO)
                .toList();
    }

    private static HBox crearFila(Evento ev) {
        boolean pendiente = ev.getFecha().isBefore(LocalDate.now());

        Label nombre = new Label(ev.getNombreCumpleanero());
        Label cuando = new Label(ev.getFecha() + " " + ev.getHoraInicio());
        VBox datos = new VBox(3, nombre, cuando);
        if (pendiente) {
            datos.getChildren().add(new Label("Pendiente de cobro"));
        }

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Button ver = new Button("Ver");
        ver.setOnAction(e -> Navegador.ir(
                PantallaDetalleEvento.crear(ev, () -> PantallaVerEventos.crear())));
        Button editar = new Button("Editar");
        editar.setOnAction(e -> Navegador.ir(
                PantallaEditarEvento.crear(ev, () -> PantallaVerEventos.crear())));

        HBox fila = new HBox(10, datos, espacio, ver, editar);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(12));
        fila.getStyleClass().add(pendiente ? "fila-pendiente" : "bloque");
        return fila;
    }
}