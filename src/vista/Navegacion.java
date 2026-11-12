package vista;

import java.awt.Frame;
import java.awt.Window;
import javax.swing.JFrame;

final class Navegacion {
    private Navegacion() {
    }

    static void volverAlMenu(JFrame ventanaActual) {
        for (Window ventana : Window.getWindows()) {
            if (ventana instanceof VentanaPrincipal && ventana.isDisplayable()) {
                VentanaPrincipal menu = (VentanaPrincipal) ventana;
                menu.setExtendedState(menu.getExtendedState() & ~Frame.ICONIFIED);
                menu.setVisible(true);
                menu.toFront();
                ventanaActual.dispose();
                return;
            }
        }
        new VentanaPrincipal().setVisible(true);
        ventanaActual.dispose();
    }
}
