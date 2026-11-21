package interfaz;

import dao.AccesoDatosException;
import dao.EventoDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import servicio.Recaudacionservice;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PantallaRecaudacion {

    private static final DateTimeFormatter FORMATO_MES =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.of("es", "AR"));

    public static Parent crear() {
        Button volver = new Button("← Volver");
        volver.setOnAction(e -> Navegador.ir(PantallaMenu.crear()));

        Label titulo = new Label("Recaudación");

        Recaudacionservice servicio = new Recaudacionservice(new EventoDAO());
        YearMonth[] mesElegido = { YearMonth.now() };

        // selector de mes con flechas
        Button anterior = new Button("◀");
        Button siguiente = new Button("▶");
        Label nombreMes = new Label();
        HBox selector = new HBox(15, anterior, nombreMes, siguiente);
        selector.setAlignment(Pos.CENTER_LEFT);

        // total y desglose
        Label total = new Label();
        total.getStyleClass().add("total-grande");
        Label desglose = new Label();
        VBox bloqueTotal = crearBloque(new Label("Total recaudado del mes"), total, desglose);

        // comparación con el mes anterior
        Label comparacion = new Label();
        VBox bloqueComparacion = crearBloque(new Label("Comparación con el mes anterior"), comparacion);

        // promedio de un rango de meses
        ComboBox<Integer> rango = new ComboBox<>(FXCollections.observableArrayList(3, 6, 12));
        rango.setValue(3);
        Label promedio = new Label();
        VBox bloquePromedio = crearBloque(
                new Label("Promedio de los últimos meses"),
                new HBox(10, new Label("Meses:"), rango),
                promedio);

        Label aviso = new Label();

        Runnable actualizar = () -> {
            aviso.setText("");
            String texto = FORMATO_MES.format(mesElegido[0]);
            nombreMes.setText(texto.substring(0, 1).toUpperCase() + texto.substring(1));
            try {
                Recaudacionservice.TotalMes totalMes = servicio.calcularTotalMes(mesElegido[0]);
                total.setText(dinero(totalMes.total()));
                desglose.setText("Pagos completos: " + dinero(totalMes.cobradoCompleto()) + "\n"
                        + "Señas por cobrar: " + dinero(totalMes.cobradoEnSenias()) + "\n"
                        + "Señas de eventos cancelados: " + dinero(totalMes.seniasCanceladas()));

                Recaudacionservice.Comparacion comp = servicio.compararConMesAnterior(mesElegido[0]);
                if (comp.variacionPorcentual() == null) {
                    comparacion.setText("El mes anterior no tuvo recaudación: no hay base de comparación.");
                } else {
                    comparacion.setText(String.format("%+.1f%% (diferencia de %s)",
                            comp.variacionPorcentual(), dinero(comp.diferencia())));
                }

                promedio.setText(dinero(servicio.calcularPromedio(rango.getValue())) + " por mes");
            } catch (AccesoDatosException ex) {
                aviso.setText(ex.getMessage());
            }
        };

        anterior.setOnAction(e -> {
            mesElegido[0] = mesElegido[0].minusMonths(1);
            actualizar.run();
        });
        siguiente.setOnAction(e -> {
            mesElegido[0] = mesElegido[0].plusMonths(1);
            actualizar.run();
        });
        rango.valueProperty().addListener((o, a, b) -> actualizar.run());

        actualizar.run();

        VBox contenido = new VBox(20, volver, titulo, selector, aviso,
                bloqueTotal, bloqueComparacion, bloquePromedio);
        contenido.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private static String dinero(double monto) {
        return String.format("$%.2f", monto);
    }

    private static VBox crearBloque(Node... hijos) {
        VBox bloque = new VBox(8, hijos);
        bloque.setPadding(new Insets(15));
        bloque.setMaxWidth(400);
        bloque.getStyleClass().add("bloque");
        return bloque;
    }
}