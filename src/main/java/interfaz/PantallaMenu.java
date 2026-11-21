package interfaz;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class PantallaMenu {

    public static Parent crear() {
        Button nuevo = new Button("Nuevo evento");
        Button ver = new Button("Ver eventos");
        Button historial = new Button("Historial");
        Button recaudacion = new Button("Recaudación");
        Button opciones = new Button("Opciones");

        nuevo.getStyleClass().addAll("tarjeta", "nuevo");
        ver.getStyleClass().addAll("tarjeta", "ver");
        historial.getStyleClass().addAll("tarjeta", "historial");
        recaudacion.getStyleClass().addAll("tarjeta", "recaudacion");
        opciones.getStyleClass().addAll("tarjeta", "opciones");

        nuevo.setPrefSize(200, 120);
        ver.setPrefSize(200, 120);
        historial.setPrefSize(200, 120);
        recaudacion.setPrefSize(200, 120);
        opciones.setPrefSize(420, 70);

        nuevo.setOnAction(e -> Navegador.ir(PantallaNuevoEvento.crear()));
        ver.setOnAction(e -> Navegador.ir(PantallaVerEventos.crear()));
        historial.setOnAction(e -> Navegador.ir(PantallaHistorial.crear()));
        recaudacion.setOnAction(e -> Navegador.ir(PantallaRecaudacion.crear()));
        opciones.setOnAction(e -> Navegador.ir(PantallaOpciones.crear()));

        GridPane grilla = new GridPane();
        grilla.setHgap(20);
        grilla.setVgap(20);
        grilla.setAlignment(Pos.CENTER);
        grilla.add(nuevo, 0, 0);
        grilla.add(ver, 1, 0);
        grilla.add(historial, 0, 1);
        grilla.add(recaudacion, 1, 1);

        VBox raiz = new VBox(20, Cabecera.crear(), grilla, opciones);
        raiz.setAlignment(Pos.CENTER);
        return raiz;
    }
}