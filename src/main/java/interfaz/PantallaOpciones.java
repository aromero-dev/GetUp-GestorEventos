package interfaz;

import dao.AccesoDatosException;
import dao.ConfiguracionDAO;
import dao.OpcionCateringDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import modelo.Configuracion;
import modelo.OpcionCatering;
import modelo.ValidacionException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PantallaOpciones {

    public static Parent crear() {
        Button volver = new Button("← Volver");
        Label titulo = new Label("Opciones");
        Label aviso = new Label();

        ConfiguracionDAO dao = new ConfiguracionDAO();
        Configuracion config;
        try {
            config = dao.leer();
        } catch (AccesoDatosException ex) {
            aviso.setText(ex.getMessage());
            volver.setOnAction(e -> Navegador.ir(PantallaMenu.crear()));
            VBox error = new VBox(20, volver, titulo, aviso);
            error.setPadding(new Insets(20));
            return error;
        }

        // Bloque 1: tarifas base
        TextField semana = new TextField(texto(config.getTarifaBaseSemana()));
        TextField finde = new TextField(texto(config.getTarifaBaseFinde()));
        VBox bloqueTarifas = crearBloque(
                new Label("Tarifas base"),
                new Label("Lunes a jueves"), semana,
                new Label("Viernes a domingo y feriados"), finde);

        // Bloque 2: precios de extras
        TextField ninioExtra = new TextField(texto(config.getPrecioNinioExtra()));
        TextField adultoExtra = new TextField(texto(config.getPrecioAdultoExtra()));
        TextField mediaHora = new TextField(texto(config.getPrecioMediaHoraExtra()));
        TextField horaExtra = new TextField(texto(config.getPrecioHoraExtra()));
        TextField animadora = new TextField(texto(config.getPrecioAnimadoraAdicional()));
        TextField cumpleExtra = new TextField(texto(config.getPrecioCumpleaneroExtra()));
        VBox bloqueExtras = crearBloque(
                new Label("Precios de extras"),
                new Label("Niño extra"), ninioExtra,
                new Label("Adulto extra"), adultoExtra,
                new Label("Media hora extra"), mediaHora,
                new Label("Hora extra"), horaExtra,
                new Label("Animadora adicional"), animadora,
                new Label("Cumpleañero extra"), cumpleExtra);

        // Bloque 3: paquete base
        TextField niniosInc = new TextField(String.valueOf(config.getNiniosIncluidos()));
        TextField adultosInc = new TextField(String.valueOf(config.getAdultosIncluidos()));
        VBox bloquePaquete = crearBloque(
                new Label("Paquete base"),
                new Label("Niños incluidos"), niniosInc,
                new Label("Adultos incluidos"), adultosInc);

        // Guardar tarifas, extras y paquete base
        boolean[] cambiosConfig = new boolean[1];

        Button guardar = new Button("Guardar cambios");
        guardar.getStyleClass().addAll("tarjeta", "nuevo");
        guardar.setPrefSize(260, 60);

        guardar.setOnAction(e -> {
            aviso.setText("");
            try {
                // el constructor valida todo junto: si algo falla, no se guarda nada
                Configuracion nueva = new Configuracion(
                        leerPrecio(semana), leerPrecio(finde),
                        leerPrecio(ninioExtra), leerPrecio(adultoExtra),
                        leerPrecio(mediaHora), leerPrecio(horaExtra),
                        leerPrecio(animadora), leerPrecio(cumpleExtra),
                        leerEntero(niniosInc), leerEntero(adultosInc));
                dao.actualizar(nueva);
                cambiosConfig[0] = false;
                aviso.setText("Cambios guardados");
            } catch (ValidacionException | AccesoDatosException ex) {
                aviso.setText(ex.getMessage());
            }
        });

        List<TextField> campos = List.of(semana, finde, ninioExtra, adultoExtra,
                mediaHora, horaExtra, animadora, cumpleExtra, niniosInc, adultosInc);
        for (TextField campo : campos) {
            campo.textProperty().addListener((o, a, b) -> {
                cambiosConfig[0] = true;
                aviso.setText("");
            });
        }

        // Bloque 4: menús de catering (cada uno se guarda con sus propios botones)
        OpcionCateringDAO cateringDAO = new OpcionCateringDAO();
        Set<String> preciosPendientes = new HashSet<>();
        Label avisoMenus = new Label();
        VBox listaMenus = new VBox(10);

        try {
            for (OpcionCatering op : cateringDAO.buscarActivas()) {
                listaMenus.getChildren().add(
                        crearFilaMenu(op, cateringDAO, listaMenus, avisoMenus, preciosPendientes));
            }
        } catch (AccesoDatosException ex) {
            avisoMenus.setText(ex.getMessage());
        }

        TextField nuevoNombre = new TextField();
        nuevoNombre.setPromptText("Nombre del menú");
        TextField nuevoPrecio = new TextField();
        nuevoPrecio.setPromptText("Precio por persona");
        Button agregarMenu = new Button("Agregar menú");

        agregarMenu.setOnAction(e -> {
            avisoMenus.setText("");
            try {
                OpcionCatering nueva = new OpcionCatering(nuevoNombre.getText(), leerPrecio(nuevoPrecio));
                cateringDAO.agregar(nueva);
                listaMenus.getChildren().add(
                        crearFilaMenu(nueva, cateringDAO, listaMenus, avisoMenus, preciosPendientes));
                nuevoNombre.clear();
                nuevoPrecio.clear();
                avisoMenus.setText("Menú agregado");
            } catch (ValidacionException | AccesoDatosException ex) {
                avisoMenus.setText(ex.getMessage());
            }
        });

        VBox bloqueCatering = crearBloque(
                new Label("Menús de catering (nombre y precio por persona)"),
                listaMenus,
                new Label("Nuevo menú"), nuevoNombre, nuevoPrecio, agregarMenu,
                avisoMenus);
        bloqueCatering.setMaxWidth(560);

        // Volver: con algo sin guardar pregunta antes
        volver.setOnAction(e -> {
            boolean pendiente = cambiosConfig[0]
                    || !preciosPendientes.isEmpty()
                    || !nuevoNombre.getText().isBlank()
                    || !nuevoPrecio.getText().isBlank();
            if (!pendiente) {
                Navegador.ir(PantallaMenu.crear());
                return;
            }
            if (confirmar("¿Salir sin guardar?", "Hay cambios sin guardar.",
                    "Salir", "Seguir editando")) {
                Navegador.ir(PantallaMenu.crear());
            }
        });

        VBox contenido = new VBox(20, volver, titulo, bloqueTarifas, bloqueExtras,
                bloquePaquete, guardar, aviso, bloqueCatering);
        contenido.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private static HBox crearFilaMenu(OpcionCatering op, OpcionCateringDAO dao, VBox listaMenus,
                                      Label avisoMenus, Set<String> pendientes) {
        Label nombre = new Label(op.getNombre());
        nombre.setPrefWidth(150);
        TextField precio = new TextField(texto(op.getPrecioPorPersona()));
        precio.setPrefWidth(90);
        Button guardarPrecio = new Button("Guardar precio");
        Button borrar = new Button("Borrar");

        HBox fila = new HBox(10, nombre, precio, guardarPrecio, borrar);
        fila.setAlignment(Pos.CENTER_LEFT);

        precio.textProperty().addListener((o, a, b) -> {
            pendientes.add(op.getNombre());
            avisoMenus.setText("");
        });

        guardarPrecio.setOnAction(e -> {
            try {
                dao.modificarPrecio(new OpcionCatering(op.getNombre(), leerPrecio(precio)));
                pendientes.remove(op.getNombre());
                avisoMenus.setText("Precio de " + op.getNombre() + " actualizado");
            } catch (ValidacionException | AccesoDatosException ex) {
                avisoMenus.setText(ex.getMessage());
            }
        });

        borrar.setOnAction(e -> {
            if (!confirmar("¿Borrar el menú " + op.getNombre() + "?",
                    "Deja de aparecer para nuevos eventos. Los eventos que ya lo eligieron lo siguen mostrando.",
                    "Borrar", "No")) {
                return;
            }
            try {
                dao.ocultar(op.getNombre());
                pendientes.remove(op.getNombre());
                listaMenus.getChildren().remove(fila);
                avisoMenus.setText("Menú borrado");
            } catch (AccesoDatosException ex) {
                avisoMenus.setText(ex.getMessage());
            }
        });

        return fila;
    }

    private static boolean confirmar(String encabezado, String mensaje, String textoSi, String textoNo) {
        ButtonType si = new ButtonType(textoSi, ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType(textoNo, ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, si, no);
        alerta.setHeaderText(encabezado);
        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == si;
    }

    private static double leerPrecio(TextField campo) throws ValidacionException {
        try {
            return Double.parseDouble(campo.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new ValidacionException("Los precios deben ser números");
        }
    }

    private static int leerEntero(TextField campo) throws ValidacionException {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException ex) {
            throw new ValidacionException("Las cantidades deben ser números enteros");
        }
    }

    // 15000.0 se muestra como 15000
    private static String texto(double valor) {
        return valor == Math.rint(valor) ? String.valueOf((long) valor) : String.valueOf(valor);
    }

    private static VBox crearBloque(Node... hijos) {
        VBox bloque = new VBox(8, hijos);
        bloque.setPadding(new Insets(15));
        bloque.setMaxWidth(400);
        bloque.getStyleClass().add("bloque");
        return bloque;
    }
}