package interfaz;

import dao.AccesoDatosException;
import dao.ConfiguracionDAO;
import dao.EventoDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import servicio.CotizadorService;
import servicio.DisponibilidadService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public class PantallaNuevoEvento {

    public static Parent crear() {
        Button volver = new Button("← Volver");
        volver.setOnAction(e -> Navegador.ir(PantallaMenu.crear()));

        Label titulo = new Label("Nuevo evento: fecha y hora");

        DatePicker fecha = new DatePicker();

        Spinner<Integer> hora = new Spinner<>(0, 23, 17);
        Spinner<Integer> minutos = new Spinner<>(0, 59, 0, 5);
        hora.setPrefWidth(80);
        minutos.setPrefWidth(80);
        HBox horaInicio = new HBox(10, hora, new Label(":"), minutos);
        horaInicio.setAlignment(Pos.CENTER_LEFT);

        CheckBox feriado = new CheckBox("Es feriado");

        Button verificar = new Button("Verificar disponibilidad");
        verificar.getStyleClass().addAll("tarjeta", "nuevo");
        verificar.setPrefSize(260, 60);

        Label aviso = new Label();

        Button continuar = new Button("Continuar");
        continuar.getStyleClass().addAll("tarjeta", "ver");
        continuar.setPrefSize(260, 60);
        continuar.setVisible(false);
        continuar.setManaged(false);

        DisponibilidadService disponibilidad = new DisponibilidadService(new EventoDAO());
        CotizadorService cotizador = new CotizadorService(new ConfiguracionDAO());

        verificar.setOnAction(e -> {
            ocultar(continuar);
            if (fecha.getValue() == null) {
                aviso.setText("Elegí una fecha");
                return;
            }
            if (fecha.getValue().isBefore(LocalDate.now())) {
                aviso.setText("No se puede agendar una fecha que ya pasó");
                return;
            }
            try {
                LocalTime inicio = LocalTime.of(hora.getValue(), minutos.getValue());
                int duracion = cotizador.calcularDuracionBase(fecha.getValue(), feriado.isSelected());
                boolean libre = disponibilidad.verificarDisponibilidad(
                        fecha.getValue(), inicio, duracion, Optional.empty());
                if (libre) {
                    aviso.setText("Horario disponible");
                    continuar.setVisible(true);
                    continuar.setManaged(true);
                } else {
                    aviso.setText("Ese horario ya está ocupado por otro evento");
                }
            } catch (AccesoDatosException ex) {
                aviso.setText(ex.getMessage());
            }
        });

        continuar.setOnAction(e -> {
            LocalTime inicio = LocalTime.of(hora.getValue(), minutos.getValue());
            Navegador.ir(PantallaNuevoEventoPaso2.crear(fecha.getValue(), inicio, feriado.isSelected()));
        });

        // si cambia algún dato, hay que volver a verificar
        fecha.valueProperty().addListener((obs, a, b) -> reiniciar(continuar, aviso));
        hora.valueProperty().addListener((obs, a, b) -> reiniciar(continuar, aviso));
        minutos.valueProperty().addListener((obs, a, b) -> reiniciar(continuar, aviso));
        feriado.selectedProperty().addListener((obs, a, b) -> reiniciar(continuar, aviso));

        VBox contenido = new VBox(15,
                new Label("Fecha"), fecha,
                new Label("Hora de inicio"), horaInicio,
                feriado, verificar, aviso, continuar);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setMaxWidth(300);

        VBox raiz = new VBox(20, volver, titulo, contenido);
        raiz.setPadding(new Insets(20));
        return raiz;
    }

    private static void ocultar(Button continuar) {
        continuar.setVisible(false);
        continuar.setManaged(false);
    }

    private static void reiniciar(Button continuar, Label aviso) {
        ocultar(continuar);
        aviso.setText("");
    }
}