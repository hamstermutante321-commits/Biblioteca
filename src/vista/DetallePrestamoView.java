package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import modelo.DetallePrestamo;
import modelo.Libro;
import servicios.ServicioDetallePrestamo;

public class DetallePrestamoView extends JFrame {

    private final ServicioDetallePrestamo servicio
            = new ServicioDetallePrestamo();

    private final JTextField campoPrestamo = new JTextField();
    private final JTextField campoLibro = new JTextField();
    private final JTextField campoTitulo = new JTextField();
    private final JTextField campoCantidad = new JTextField("1");

    /*
     * Guarda el ID del préstamo que se está mostrando en la tabla.
     * Así evitamos quitar un libro usando la tabla de otro préstamo.
     */
    private String prestamoMostrado;

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID libro", "Título", "Cantidad"},
            0
    ) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);

    public DetallePrestamoView() {
        setTitle("Biblioteca - Detalle del préstamo");
        setSize(850, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBackground(new Color(249, 245, 240));
        principal.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        setContentPane(principal);

        JLabel titulo = new JLabel("Detalle del préstamo");
        titulo.setFont(new Font("Serif", Font.BOLD, 26));
        titulo.setForeground(new Color(130, 49, 40));

        JPanel campos = new JPanel(new GridLayout(4, 2, 8, 8));
        campos.setBorder(
                BorderFactory.createTitledBorder("Datos del detalle")
        );

        campos.add(new JLabel("ID del préstamo:"));
        campos.add(campoPrestamo);

        campos.add(new JLabel("ID del libro:"));
        campos.add(campoLibro);

        campos.add(new JLabel("Título del libro:"));
        campos.add(campoTitulo);

        campos.add(new JLabel("Cantidad:"));
        campos.add(campoCantidad);

        campoTitulo.setEditable(false);

        JPanel botones = new JPanel();

        JButton buscar = new JButton("Buscar libro");
        JButton agregar = new JButton("Agregar libro");
        JButton quitar = new JButton("Quitar libro");
        JButton ver = new JButton("Ver detalles");
        JButton limpiar = new JButton("Limpiar");

        buscar.addActionListener(e -> buscarLibro());
        agregar.addActionListener(e -> agregar());
        quitar.addActionListener(e -> quitar());
        ver.addActionListener(e -> cargar());
        limpiar.addActionListener(e -> limpiar());

        botones.add(buscar);
        botones.add(agregar);
        botones.add(quitar);
        botones.add(ver);
        botones.add(limpiar);

        JPanel superior = new JPanel(new BorderLayout(10, 10));
        superior.setOpaque(false);
        superior.add(titulo, BorderLayout.NORTH);
        superior.add(campos, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        principal.add(superior, BorderLayout.NORTH);

        tabla.setRowHeight(28);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        principal.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton volver = new JButton("Volver al menú principal");
        volver.addActionListener(e -> Navegacion.volverAlMenu(this));

        JPanel inferior = new JPanel();
        inferior.setOpaque(false);
        inferior.add(volver);

        principal.add(inferior, BorderLayout.SOUTH);
    }

    private void buscarLibro() {
        campoTitulo.setText("");

        try {
            String idLibro = campoLibro.getText().strip();
            Libro libro = servicio.buscarLibro(idLibro);

            campoTitulo.setText(libro.getTitulo());

        } catch (Exception error) {
            mostrarError(error);
        }
    }

    private void agregar() {
        try {
            String idPrestamo = campoPrestamo.getText().strip();
            String idLibro = campoLibro.getText().strip();

            int cantidad = Integer.parseInt(
                    campoCantidad.getText().strip()
            );

            servicio.agregar(idPrestamo, idLibro, cantidad);

            cargar();

            JOptionPane.showMessageDialog(
                    this,
                    "Libro agregado al préstamo."
            );

        } catch (NumberFormatException error) {
            mostrarError(
                    new IllegalArgumentException(
                            "Escribe una cantidad entera."
                    )
            );

        } catch (Exception error) {
            mostrarError(error);
        }
    }

    private void quitar() {
        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona un libro de la tabla."
            );
            return;
        }

        String idEscrito = campoPrestamo.getText().strip();

        if (!idEscrito.equals(prestamoMostrado)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Pulsa Ver detalles para el préstamo indicado."
            );
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(fila);

        String idLibro = modelo.getValueAt(
                filaModelo,
                0
        ).toString();

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Quitar el libro " + idLibro + "?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            servicio.quitar(idEscrito, idLibro);

            cargar();

            JOptionPane.showMessageDialog(
                    this,
                    "Libro retirado del préstamo."
            );

        } catch (Exception error) {
            mostrarError(error);
        }
    }

    private void cargar() {
        try {
            String idPrestamo = campoPrestamo.getText().strip();

            List<DetallePrestamo> detalles
                    = servicio.listarPorPrestamo(idPrestamo);

            modelo.setRowCount(0);

            for (DetallePrestamo detalle : detalles) {
                Libro libro = servicio.buscarLibro(
                        detalle.getIdLibro()
                );

                modelo.addRow(new Object[]{
                    detalle.getIdLibro(),
                    libro.getTitulo(),
                    detalle.getCantidad()
                });
            }

            prestamoMostrado = idPrestamo;

        } catch (Exception error) {
            mostrarError(error);
        }
    }

    private void limpiar() {
        campoPrestamo.setText("");
        campoLibro.setText("");
        campoTitulo.setText("");
        campoCantidad.setText("1");

        modelo.setRowCount(0);
        prestamoMostrado = null;
    }

    private void mostrarError(Exception error) {
        JOptionPane.showMessageDialog(
                this,
                error.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(
                () -> new DetallePrestamoView().setVisible(true)
        );
    }
}
