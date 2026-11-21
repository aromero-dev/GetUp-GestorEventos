package interfaz;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage ventana) {
        Scene escena = new Scene(PantallaMenu.crear(), 800, 600);
        escena.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());
        Navegador.iniciar(escena);

        ventana.setTitle("Get Up Eventos");
        ventana.setScene(escena);
        ventana.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}