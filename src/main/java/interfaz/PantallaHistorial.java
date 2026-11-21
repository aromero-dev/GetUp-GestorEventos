package interfaz;

import dao.AccesoDatosException;
import dao.EventoDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import modelo.EstadoEvento;
import modelo.Evento;

import java.util.List;

public class PantallaHistorial {

    public static Parent crear() {
        Button volver = new Button("← Volver");
        volver.setOnAction(e -> Navegador.ir(PantallaMenu.crear()));

        Label titulo = new Label("Historial de eventos");
        Label aviso = new Label();
        VBox lista = new VBox(10);

        try {
            List<Evento> eventos = new EventoDAO().buscarHistorial();
            if (eventos.isEmpty()) {
                aviso.setText("Todavía no hay eventos en el historial");
            }
            for (Evento ev : eventos) {
                lista.getChildren().add(crearFila(ev));
            }
        } catch (AccesoDatosException ex) {
            aviso.setText(ex.getMessage());
        }

        VBox contenido = new VBox(20, volver, titulo, aviso, lista);
        contenido.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private static HBox crearFila(Evento ev) {
        boolean realizado = ev.getEstado() == EstadoEvento.REALIZADO;

        Label nombre = new Label(ev.getNombreCumpleanero());
        Label cuando = new Label(ev.getFecha() + " " + ev.getHoraInicio());
        Label estado = new Label(realizado ? "Realizado" : "Cancelado");
        VBox datos = new VBox(3, nombre, cuando, estado);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Button ver = new Button("Ver");
        ver.setOnAction(e -> Navegador.ir(
                PantallaDetalleEvento.crear(ev, () -> PantallaHistorial.crear())));

        HBox fila = new HBox(10, datos, espacio, ver);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(12));
        fila.getStyleClass().add(realizado ? "fila-realizado" : "fila-cancelado");
        return fila;
    }
}