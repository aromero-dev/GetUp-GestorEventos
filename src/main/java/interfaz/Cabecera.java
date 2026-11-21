package interfaz;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.net.URL;

public class Cabecera {

    public static Node crear() {
        URL recurso = Cabecera.class.getResource("/logo.png");
        Node contenido;
        if (recurso != null) {
            ImageView logo = new ImageView(new Image(recurso.toExternalForm()));
            logo.setFitHeight(90);
            logo.setPreserveRatio(true);
            contenido = logo;
        } else {
            Label nombre = new Label("Get Up Eventos");
            nombre.getStyleClass().add("titulo-cabecera");
            contenido = nombre;
        }
        HBox caja = new HBox(contenido);
        caja.setAlignment(Pos.CENTER);
        caja.setPadding(new Insets(10));
        caja.getStyleClass().add("cabecera");
        return caja;
    }
}