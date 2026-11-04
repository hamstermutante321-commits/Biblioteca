package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import modelo.Prestamo;
import servicios.ServicioPrestamos;

public class PrestamoView extends JFrame {
    private final ServicioPrestamos servicio = new ServicioPrestamos();
    private final JTextField campoUsuario = new JTextField();
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Usuario", "Préstamo", "Vencimiento", "Devolución"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public PrestamoView() {
        setTitle("Biblioteca - Préstamos");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel contenido = new JPanel(new BorderLayout(15, 15));
        contenido.setBackground(new Color(249, 245, 240));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        setContentPane(contenido);

        JPanel cabecera = new JPanel(new BorderLayout(15, 0));
        cabecera.setBackground(new Color(246, 232, 224));
        JLabel titulo = new JLabel("  Gestión de préstamos");
        titulo.setFont(new Font("Serif", Font.BOLD, 28));
        titulo.setForeground(new Color(130, 49, 40));
        cabecera.add(titulo, BorderLayout.CENTER);

        URL imagen = getClass().getResource("/vista/recursos/prestamos.png");
        if (imagen != null) {
            Image portada = new ImageIcon(imagen).getImage().getScaledInstance(200, 100, Image.SCALE_SMOOTH);
            cabecera.add(new JLabel(new ImageIcon(portada)), BorderLayout.EAST);
        }
        contenido.add(cabecera, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridLayout(3, 1, 8, 8));
        formulario.setBackground(Color.WHITE);
        formulario.setBorder(BorderFactory.createTitledBorder("Nuevo préstamo"));
        formulario.add(new JLabel("ID del usuario:"));
        formulario.add(campoUsuario);
        formulario.add(new JLabel("El plazo de devolución es de 14 días."));

        JButton registrar = new JButton("Registrar préstamo");
        registrar.setBackground(new Color(183, 76, 64));
        registrar.setForeground(Color.WHITE);
        registrar.addActionListener(e -> registrarPrestamo());

        JPanel panelFormulario = new JPanel(new BorderLayout(0, 10));
        panelFormulario.setOpaque(false);
        panelFormulario.add(formulario, BorderLayout.CENTER);
        panelFormulario.add(registrar, BorderLayout.SOUTH);
        contenido.add(panelFormulario, BorderLayout.WEST);

        tabla.setRowHeight(28);
        contenido.add(new JScrollPane(tabla), BorderLayout.CENTER);
        cargarTabla();
    }

    private void registrarPrestamo() {
        try {
            String idUsuario = campoUsuario.getText().trim();
            Prestamo prestamo = servicio.crear(idUsuario, LocalDate.now());
            campoUsuario.setText("");
            cargarTabla();
            JOptionPane.showMessageDialog(this, "Préstamo " + prestamo.getId() + " registrado.");
        } catch (IllegalArgumentException | IOException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTabla() {
        try {
            modeloTabla.setRowCount(0);
            for (Prestamo prestamo : servicio.listar()) {
                modeloTabla.addRow(new Object[]{
                    prestamo.getId(), prestamo.getIdUsuario(), prestamo.getFechaPrestamo(),
                    prestamo.getFechaVencimiento(), prestamo.getFechaDevolucion()
                });
            }
        } catch (IOException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrestamoView().setVisible(true));
    }
}
