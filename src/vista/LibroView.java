package vista;

import modelo.Libro;
import servicios.ServicioLibros;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class LibroView extends JFrame {
    private JTextField txtId, txtTitulo, txtAutor, txtTotales, txtDisponibles, txtBuscar;
    private JTable tablaLibros;
    private DefaultTableModel modeloTabla;
    private ServicioLibros servicio;

    public LibroView() {
        servicio = new ServicioLibros();
        setTitle("Gestión de Libros - Biblioteca");
        setSize(750, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel Superior: Formulario de Registro/Edición
        JPanel panelFormulario = new JPanel(new GridLayout(6, 2, 5, 5));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Libro"));

        panelFormulario.add(new JLabel("ID (Automático):"));
        txtId = new JTextField();
        txtId.setEditable(false);
        panelFormulario.add(txtId);

        panelFormulario.add(new JLabel("Título:"));
        txtTitulo = new JTextField();
        panelFormulario.add(txtTitulo);

        panelFormulario.add(new JLabel("Autor:"));
        txtAutor = new JTextField();
        panelFormulario.add(txtAutor);

        panelFormulario.add(new JLabel("Ejemplares Totales:"));
        txtTotales = new JTextField();
        panelFormulario.add(txtTotales);

        panelFormulario.add(new JLabel("Ejemplares Disponibles:"));
        txtDisponibles = new JTextField();
        panelFormulario.add(txtDisponibles);

        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnGuardar = new JButton("Guardar");
        JButton btnModificar = new JButton("Modificar");
        JButton btnLimpiar = new JButton("Limpiar");
        panelBotones.add(btnGuardar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnLimpiar);
        
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.add(panelFormulario, BorderLayout.CENTER);
        panelNorte.add(panelBotones, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        // Panel Central: Búsqueda y Tabla
        JPanel panelCentro = new JPanel(new BorderLayout(5, 5));
        
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.add(new JLabel("Buscar por Título:"));
        txtBuscar = new JTextField(20);
        JButton btnBuscar = new JButton("Buscar");
        JButton btnMostrarTodos = new JButton("Ver Todos");
        panelBusqueda.add(txtBuscar);
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnMostrarTodos);
        
        panelCentro.add(panelBusqueda, BorderLayout.NORTH);

        // Configuración de la JTable
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Título", "Autor", "Totales", "Disponibles"}, 0);
        tablaLibros = new JTable(modeloTabla);
        panelCentro.add(new JScrollPane(tablaLibros), BorderLayout.CENTER);

        add(panelCentro, BorderLayout.CENTER);

        // Eventos
        cargarTabla(servicio.listar());
        txtId.setText(servicio.generarId());

        btnGuardar.addActionListener(e -> guardarLibro());
        btnModificar.addActionListener(e -> modificarLibro());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnBuscar.addActionListener(e -> buscarLibros());
        btnMostrarTodos.addActionListener(e -> {
            cargarTabla(servicio.listar());
            txtId.setText(servicio.generarId());
        });

        // Evento al hacer clic en la tabla para pasar datos a los campos
        tablaLibros.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tablaLibros.getSelectedRow();
                if (fila != -1) {
                    txtId.setText(modeloTabla.getValueAt(fila, 0).toString());
                    txtTitulo.setText(modeloTabla.getValueAt(fila, 1).toString());
                    txtAutor.setText(modeloTabla.getValueAt(fila, 2).toString());
                    txtTotales.setText(modeloTabla.getValueAt(fila, 3).toString());
                    txtDisponibles.setText(modeloTabla.getValueAt(fila, 4).toString());
                }
            }
        });
    }

    private void cargarTabla(List<Libro> lista) {
        modeloTabla.setRowCount(0);
        for (Libro l : lista) {
            modeloTabla.addRow(new Object[]{
                l.getId(), l.getTitulo(), l.getAutor(), l.getEjemplaresTotales(), l.getEjemplaresDisponibles()
            });
        }
    }

    private void guardarLibro() {
        try {
            String titulo = txtTitulo.getText().trim();
            String autor = txtAutor.getText().trim();
            if (titulo.isEmpty() || autor.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor complete el título y el autor.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int totales = Integer.parseInt(txtTotales.getText().trim());
            int disponibles = Integer.parseInt(txtDisponibles.getText().trim());

            Libro libro = new Libro(servicio.generarId(), titulo, autor, totales, disponibles);
            servicio.guardar(libro);
            cargarTabla(servicio.listar());
            limpiarCampos();
            JOptionPane.showMessageDialog(this, "Libro guardado con éxito.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Los campos de ejemplares deben ser numéricos.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de validación", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarLibro() {
        try {
            String id = txtId.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seleccione un libro de la tabla para modificar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String titulo = txtTitulo.getText().trim();
            String autor = txtAutor.getText().trim();
            int totales = Integer.parseInt(txtTotales.getText().trim());
            int disponibles = Integer.parseInt(txtDisponibles.getText().trim());

            Libro libro = new Libro(id, titulo, autor, totales, disponibles);
            servicio.modificar(libro);
            cargarTabla(servicio.listar());
            limpiarCampos();
            JOptionPane.showMessageDialog(this, "Libro modificado con éxito.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Los campos de ejemplares deben ser numéricos.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de validación", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarLibros() {
        String filtro = txtBuscar.getText().trim();
        List<Libro> resultados = servicio.buscar(filtro);
        cargarTabla(resultados);
    }

    private void limpiarCampos() {
        txtId.setText(servicio.generarId());
        txtTitulo.setText("");
        txtAutor.setText("");
        txtTotales.setText("");
        txtDisponibles.setText("");
        txtBuscar.setText("");
        tablaLibros.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibroView().setVisible(true));
    }
}
