package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Libro;
import modelo.Prestamo;
import servicios.ServicioLibros;
import servicios.ServicioPrestamos;

public class PrestamoView extends JFrame {
    private static final Color TERRACOTA = new Color(183, 76, 64);
    private final ServicioPrestamos servicio = new ServicioPrestamos();
    private final ServicioLibros servicioLibros = new ServicioLibros();
    private final JTextField campoUsuario = new JTextField();
    private final JTextField campoIdLibro = new JTextField();
    private final JTextField campoTituloLibro = new JTextField();
    private final JTextField campoBusqueda = new JTextField(15);
    private final JComboBox<String> filtroEstado = new JComboBox<>(
            new String[]{"Todos", "Activos", "Vencidos", "Devueltos"});
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Usuario", "Préstamo", "Vencimiento", "Devolución", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public PrestamoView() {
        setTitle("Biblioteca - Préstamos");
        setSize(900, 600);
        setMinimumSize(new java.awt.Dimension(800, 500));
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

        JPanel formulario = new JPanel(new GridLayout(8, 1, 8, 8));
        formulario.setBackground(Color.WHITE);
        formulario.setBorder(BorderFactory.createTitledBorder("Nuevo préstamo"));
        formulario.add(new JLabel("ID del usuario:"));
        formulario.add(campoUsuario);
        formulario.add(new JLabel("ID del libro:"));
        formulario.add(campoIdLibro);
        campoIdLibro.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evento) { campoTituloLibro.setText(""); }
            public void removeUpdate(DocumentEvent evento) { campoTituloLibro.setText(""); }
            public void changedUpdate(DocumentEvent evento) { campoTituloLibro.setText(""); }
        });
        JButton buscarLibro = new JButton("Buscar libro");
        aplicarEstiloBoton(buscarLibro);
        buscarLibro.addActionListener(e -> buscarLibro());
        formulario.add(buscarLibro);
        campoTituloLibro.setEditable(false);
        formulario.add(campoTituloLibro);
        formulario.add(new JLabel("Se prestará un ejemplar del libro indicado."));
        formulario.add(new JLabel("El plazo de devolución es de 14 días."));

        JButton registrar = new JButton("Registrar préstamo");
        aplicarEstiloBoton(registrar);
        registrar.addActionListener(e -> registrarPrestamo());

        JPanel panelFormulario = new JPanel(new BorderLayout(0, 10));
        panelFormulario.setOpaque(false);
        JPanel tarjetaFormulario = new JPanel(new BorderLayout(0, 10));
        tarjetaFormulario.setBackground(Color.WHITE);
        tarjetaFormulario.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        tarjetaFormulario.add(formulario, BorderLayout.CENTER);
        tarjetaFormulario.add(registrar, BorderLayout.SOUTH);
        panelFormulario.add(tarjetaFormulario, BorderLayout.NORTH);
        contenido.add(panelFormulario, BorderLayout.WEST);

        JPanel panelListado = new JPanel(new BorderLayout(0, 10));
        panelListado.setOpaque(false);
        JPanel panelBusqueda = new JPanel();
        panelBusqueda.setOpaque(false);
        panelBusqueda.add(new JLabel("Buscar por ID:"));
        panelBusqueda.add(campoBusqueda);
        panelBusqueda.add(filtroEstado);
        JButton buscar = new JButton("Buscar");
        aplicarEstiloBoton(buscar);
        buscar.addActionListener(e -> cargarTabla());
        panelBusqueda.add(buscar);
        JButton verTodos = new JButton("Ver todos");
        aplicarEstiloBoton(verTodos);
        verTodos.addActionListener(e -> {
            campoBusqueda.setText("");
            filtroEstado.setSelectedItem("Todos");
            cargarTabla();
        });
        panelBusqueda.add(verTodos);
        panelListado.add(panelBusqueda, BorderLayout.NORTH);
        tabla.setRowHeight(28);
        tabla.setSelectionBackground(new Color(246, 226, 219));
        tabla.setSelectionForeground(Color.BLACK);
        tabla.getTableHeader().setBackground(new Color(246, 232, 224));
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panelListado.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JButton devolver = new JButton("Registrar devolución");
        aplicarEstiloBoton(devolver);
        devolver.addActionListener(e -> registrarDevolucion());
        JPanel panelAcciones = new JPanel();
        panelAcciones.setOpaque(false);
        panelAcciones.add(devolver);
        panelListado.add(panelAcciones, BorderLayout.SOUTH);
        contenido.add(panelListado, BorderLayout.CENTER);
        cargarTabla();
    }

    private void aplicarEstiloBoton(JButton boton) {
        boton.setBackground(TERRACOTA);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
    }

    private void buscarLibro() {
        String id = campoIdLibro.getText().trim();
        campoTituloLibro.setText("");
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe el ID de un libro.");
            return;
        }
        try {
            for (Libro libro : servicioLibros.listar()) {
                if (libro.getId().equalsIgnoreCase(id)) {
                    campoTituloLibro.setText(libro.getTitulo());
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "No se encontró un libro con el ID " + id + ".");
        } catch (IllegalArgumentException | PersistenciaException error) {
            JOptionPane.showMessageDialog(this, "No se pudieron leer los libros: " + error.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarPrestamo() {
        try {
            String idUsuario = campoUsuario.getText().trim();
            String idLibro = campoIdLibro.getText().trim();
            Prestamo prestamo = servicio.crear(idUsuario, idLibro, LocalDate.now());
            campoUsuario.setText("");
            campoIdLibro.setText("");
            campoTituloLibro.setText("");
            cargarTabla();
            JOptionPane.showMessageDialog(this, "Préstamo " + prestamo.getId() + " registrado.");
        } catch (IllegalArgumentException | IOException | PersistenciaException | ValidacionException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTabla() {
        try {
            List<Prestamo> prestamos = servicio.listar();
            String busqueda = campoBusqueda.getText().trim().toLowerCase(Locale.ROOT);
            String estadoElegido = (String) filtroEstado.getSelectedItem();
            modeloTabla.setRowCount(0);
            for (Prestamo prestamo : prestamos) {
                String estado = obtenerEstado(prestamo);
                String idPrestamo = prestamo.getId().toLowerCase(Locale.ROOT);
                String idUsuario = prestamo.getIdUsuario().toLowerCase(Locale.ROOT);
                if (!idPrestamo.contains(busqueda) && !idUsuario.contains(busqueda)) {
                    continue;
                }
                if (!estadoElegido.equals("Todos") && !estadoElegido.equals(estado + "s")) {
                    continue;
                }
                modeloTabla.addRow(new Object[]{
                    prestamo.getId(), prestamo.getIdUsuario(), prestamo.getFechaPrestamo(),
                    prestamo.getFechaVencimiento(), prestamo.getFechaDevolucion(), estado
                });
            }
        } catch (IOException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String obtenerEstado(Prestamo prestamo) {
        if (!prestamo.estaActivo()) {
            return "Devuelto";
        }
        if (prestamo.estaVencido(LocalDate.now())) {
            return "Vencido";
        }
        return "Activo";
    }

    private void registrarDevolucion() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un préstamo de la tabla.");
            return;
        }

        String id = modeloTabla.getValueAt(tabla.convertRowIndexToModel(fila), 0).toString();
        String textoFecha = JOptionPane.showInputDialog(this,
                "Fecha de devolución (AAAA-MM-DD):", LocalDate.now().toString());
        if (textoFecha == null) {
            return;
        }

        try {
            LocalDate fecha = LocalDate.parse(textoFecha.trim());
            servicio.registrarDevolucion(id, fecha);
            cargarTabla();
            JOptionPane.showMessageDialog(this, "Devolución del préstamo " + id + " registrada.");
        } catch (DateTimeParseException error) {
            JOptionPane.showMessageDialog(this, "Escribe una fecha válida con el formato AAAA-MM-DD.",
                    "Fecha inválida", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException | IllegalStateException | IOException | PersistenciaException | ValidacionException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrestamoView().setVisible(true));
    }
}
