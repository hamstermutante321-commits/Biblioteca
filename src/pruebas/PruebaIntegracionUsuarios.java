package pruebas;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import servicios.ServicioUsuarios;
import vista.UsuarioView;
import vista.VentanaPrincipal;

/** Prueba la pantalla de usuarios con archivos temporales. */
public class PruebaIntegracionUsuarios {
    private static <T extends Component> T buscar(Component raiz, Class<T> tipo) {
        if (tipo.isInstance(raiz)) return tipo.cast(raiz);
        if (raiz instanceof Container) {
            for (Component hijo : ((Container) raiz).getComponents()) {
                T encontrado = buscar(hijo, tipo);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }

    private static JTextField campo(Component raiz, String nombre) {
        JTextField encontrado = buscarCampo(raiz, nombre);
        if (encontrado == null) throw new AssertionError("Falta el campo " + nombre);
        return encontrado;
    }

    private static JTextField buscarCampo(Component raiz, String nombre) {
        if (raiz instanceof JLabel && nombre.equals(((JLabel) raiz).getText())) {
            return (JTextField) ((JLabel) raiz).getLabelFor();
        }
        if (raiz instanceof Container) {
            for (Component hijo : ((Container) raiz).getComponents()) {
                JTextField encontrado = buscarCampo(hijo, nombre);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }

    private static void pulsar(Component raiz, String nombre) {
        JButton boton = buscarBoton(raiz, nombre);
        if (boton == null) throw new AssertionError("Falta el botón " + nombre);
        boton.doClick();
    }

    private static JButton buscarBoton(Component raiz, String nombre) {
        if (raiz instanceof JButton && nombre.equals(((JButton) raiz).getText())) {
            return (JButton) raiz;
        }
        if (raiz instanceof Container) {
            for (Component hijo : ((Container) raiz).getComponents()) {
                JButton encontrado = buscarBoton(hijo, nombre);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }

    private static void confirmarEliminacion(UsuarioView vista, int respuesta) {
        Timer responder = new Timer(100, evento -> {
            for (Window ventana : Window.getWindows()) {
                if (ventana instanceof JDialog && ventana.isShowing()) {
                    JOptionPane panel = buscar(ventana, JOptionPane.class);
                    if (panel != null) panel.setValue(respuesta);
                }
            }
        });
        responder.start();
        try {
            pulsar(vista, "Eliminar");
        } finally {
            responder.stop();
        }
    }

    public static void main(String[] args) throws Exception {
        Path carpeta = Files.createTempDirectory("integracion-usuarios-");
        ServicioUsuarios servicio = new ServicioUsuarios(carpeta.resolve("usuarios.txt"));
        SwingUtilities.invokeAndWait(() -> {
            UsuarioView vista = new UsuarioView(servicio);
            vista.setVisible(true);
            try {
                JTable tabla = buscar(vista, JTable.class);
                JTextField nombre = campo(vista, "Nombre completo");
                JTextField dni = campo(vista, "DNI");
                JTextField correo = campo(vista, "Correo electrónico");
                nombre.setText("Ana Pérez");
                dni.setText("12345678");
                correo.setText("ana@correo.com");
                pulsar(vista, "Guardar usuario");
                if (tabla.getRowCount() != 1) throw new AssertionError("No se registró");

                // El campo de búsqueda es el único con 22 columnas.
                JTextField busqueda = null;
                for (Component componente : vista.getContentPane().getComponents()) {
                    JTextField encontrado = buscarConColumnas(componente, 22);
                    if (encontrado != null) busqueda = encontrado;
                }
                if (busqueda == null) throw new AssertionError("Falta la búsqueda");
                busqueda.setText("sin coincidencias");
                pulsar(vista, "Buscar");
                if (tabla.getRowCount() != 0) throw new AssertionError("No filtró");
                busqueda.setText("12345678");
                pulsar(vista, "Buscar");
                if (tabla.getRowCount() != 1) throw new AssertionError("No buscó por DNI");

                tabla.setRowSelectionInterval(0, 0);
                nombre.setText("Ana Actualizada");
                pulsar(vista, "Guardar cambios");
                if (!servicio.buscarPorId("U00001").getNombre().equals("Ana Actualizada")) {
                    throw new AssertionError("No se actualizó");
                }
                pulsar(vista, "Ver todos");
                tabla.setRowSelectionInterval(0, 0);
                confirmarEliminacion(vista, JOptionPane.NO_OPTION);
                if (servicio.listar().size() != 1) throw new AssertionError("No respetó cancelar");
                confirmarEliminacion(vista, JOptionPane.YES_OPTION);
                if (!servicio.listar().isEmpty()) throw new AssertionError("No se eliminó");
                pulsar(vista, "Volver");
                if (vista.isDisplayable()) throw new AssertionError("No cerró la vista");
                boolean principalAbierta = false;
                for (Window ventana : Window.getWindows()) {
                    if (ventana instanceof VentanaPrincipal && ventana.isVisible()) {
                        principalAbierta = true;
                    }
                }
                if (!principalAbierta) throw new AssertionError("No abrió la ventana principal");
            } catch (Exception error) {
                throw new RuntimeException(error);
            } finally {
                for (Window ventana : Window.getWindows()) ventana.dispose();
            }
        });
        System.out.println("PruebaIntegracionUsuarios: OK. Datos temporales: " + carpeta);
    }

    private static JTextField buscarConColumnas(Component raiz, int columnas) {
        if (raiz instanceof JTextField && ((JTextField) raiz).getColumns() == columnas) {
            return (JTextField) raiz;
        }
        if (raiz instanceof Container) {
            for (Component hijo : ((Container) raiz).getComponents()) {
                JTextField encontrado = buscarConColumnas(hijo, columnas);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }
}
