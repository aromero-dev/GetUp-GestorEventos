package interfaz;

import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class Navegador {
    private static Scene escena;

    public static void iniciar(Scene e) {
        escena = e;
    }

    public static void ir(Parent pantalla) {
        centrar(pantalla);
        escena.setRoot(pantalla);
    }

    // Centra el contenido en horizontal, tanto en pantallas con scroll
    // como en las que son un VBox directo (por ejemplo, el paso 1 de Nuevo evento)
    private static void centrar(Parent pantalla) {
        if (pantalla instanceof ScrollPane scroll && scroll.getContent() instanceof VBox contenido) {
            centrarHorizontal(contenido);
        } else if (pantalla instanceof VBox caja) {
            centrarHorizontal(caja);
        }
    }

    // Cambia solo la alineación horizontal y respeta la vertical que ya tenía
    private static void centrarHorizontal(VBox caja) {
        VPos vertical = caja.getAlignment().getVpos();
        if (vertical == VPos.CENTER) {
            caja.setAlignment(Pos.CENTER);
        } else if (vertical == VPos.BOTTOM) {
            caja.setAlignment(Pos.BOTTOM_CENTER);
        } else {
            caja.setAlignment(Pos.TOP_CENTER);
        }
    }
}