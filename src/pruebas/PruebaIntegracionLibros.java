package pruebas;

import biblioteca.Biblioteca;
import modelo.Libro;
import servicios.ServicioLibros;
import servicios.SistemaBiblioteca;
import vista.LibroView;
import vista.VentanaPrincipal;
import javax.swing.*;
import java.awt.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/** Prueba de integración Swing; requiere un escritorio gráfico y usa datos temporales. */
public class PruebaIntegracionLibros {
    private static List<Component> componentes(Component raiz) {
        List<Component> resultado = new ArrayList<>();
        resultado.add(raiz);
        if (raiz instanceof Container) {
            for (Component hijo : ((Container) raiz).getComponents()) resultado.addAll(componentes(hijo));
        }
        return resultado;
    }
    private static JTextField campo(LibroView vista, String etiqueta) {
        for (Component componente : componentes(vista)) {
            if (componente instanceof JLabel && ((JLabel) componente).getText().equals(etiqueta)) {
                return (JTextField) ((JLabel) componente).getLabelFor();
            }
        }
        throw new AssertionError("No existe el campo " + etiqueta);
    }
    private static void pulsar(LibroView vista, String texto) {
        for (Component componente : componentes(vista)) {
            if (componente instanceof JButton && ((JButton) componente).getText().equals(texto)) {
                ((JButton) componente).doClick();
                return;
            }
        }
        throw new AssertionError("No existe el botón " + texto);
    }
    private static LibroView catalogoAbierto() {
        LibroView resultado = null;
        for (Window ventana : Window.getWindows()) {
            if (ventana instanceof LibroView && ventana.isDisplayable()) {
                if (resultado != null) throw new AssertionError("Se abrieron dos catálogos");
                resultado = (LibroView) ventana;
            }
        }
        if (resultado == null) throw new AssertionError("No abrió el catálogo");
        return resultado;
    }
    private static JTable tabla(LibroView vista) {
        return (JTable) componentes(vista).stream().filter(c -> c instanceof JTable).findFirst().orElseThrow();
    }
    public static void main(String[] args) throws Exception {
        Path carpeta = Files.createTempDirectory("integracion-biblioteca-");
        ServicioLibros servicio = new ServicioLibros(carpeta.resolve("libros.txt"));
        SistemaBiblioteca sistema = new SistemaBiblioteca(servicio);
        if (sistema.getServicioLibros() != servicio) throw new AssertionError("No se comparte el servicio");
        SwingUtilities.invokeAndWait(() -> {
            VentanaPrincipal principal = new VentanaPrincipal(sistema);
            try {
                principal.getJMenuBar().getMenu(0).getItem(0).doClick();
                LibroView vista = catalogoAbierto();
                principal.getJMenuBar().getMenu(0).getItem(0).doClick();
                if (catalogoAbierto() != vista) throw new AssertionError("No se reutiliza el catálogo");
                campo(vista, "Título").setText("Biblioteca integrada");
                campo(vista, "Autor").setText("Autor de prueba");
                campo(vista, "Ejemplares totales").setText("4");
                pulsar(vista, "Registrar");
                if (tabla(vista).getRowCount() != 1 || tabla(vista).isCellEditable(0, 0)) throw new AssertionError("Tabla incorrecta");
                servicio.prestarEjemplares("L00001", 2);
                pulsar(vista, "Ver todos");
                if (!tabla(vista).getValueAt(0, 4).equals(2)) throw new AssertionError("Stock desactualizado");
                tabla(vista).setRowSelectionInterval(0, 0);
                campo(vista, "Ejemplares totales").setText("3");
                campo(vista, "Título").setText("Biblioteca actualizada");
                pulsar(vista, "Modificar");
                Libro libro = servicio.buscarPorId("L00001");
                if (libro.getEjemplaresDisponibles() != 1 || !libro.getTitulo().equals("Biblioteca actualizada")) throw new AssertionError("Inventario inconsistente");
                servicio.devolverEjemplares("L00001", 2);
                vista.dispose();
                principal.getJMenuBar().getMenu(0).getItem(0).doClick();
                if (!tabla(catalogoAbierto()).getValueAt(0, 4).equals(3)) throw new AssertionError("Reapertura incorrecta");
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                principal.dispose();
            }
        });
        Biblioteca.main(new String[0]);
        SwingUtilities.invokeAndWait(() -> {
            boolean encontrada = false;
            for (Window ventana : Window.getWindows()) {
                if (ventana instanceof VentanaPrincipal && ventana.isShowing()) {
                    encontrada = true;
                    ventana.dispose();
                }
            }
            if (!encontrada) throw new AssertionError("Biblioteca.main no abrió la ventana principal");
        });
        System.out.println("PruebaIntegracionLibros: OK. Datos temporales: " + carpeta);
    }
}
