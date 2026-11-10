package vista;

import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Usuario;
import servicios.ServicioUsuarios;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/** Pantalla para registrar, consultar, modificar y eliminar usuarios. */
public class UsuarioView extends JFrame {

    private final ServicioUsuarios servicio;
    private final JTextField campoId = new JTextField();
    private final JTextField campoNombre = new JTextField();
    private final JTextField campoDni = new JTextField();
    private final JTextField campoCorreo = new JTextField();
    private final JTextField campoBusqueda = new JTextField(24);
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "DNI", "Correo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public UsuarioView() {
        this(new ServicioUsuarios());
    }

    public UsuarioView(ServicioUsuarios servicio) {
        this.servicio = servicio;
        construirInterfaz();
        cargarUsuarios();
    }

    private void construirInterfaz() {
        setTitle("Gestión de usuarios");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del usuario"));
        campoId.setEditable(false);
        formulario.add(new JLabel("ID (asignado automáticamente)"));
        formulario.add(campoId);
        formulario.add(new JLabel("Nombre"));
        formulario.add(campoNombre);
        formulario.add(new JLabel("DNI"));
        formulario.add(campoDni);
        formulario.add(new JLabel("Correo"));
        formulario.add(campoCorreo);

        JButton botonNuevo = new JButton("Nuevo");
        JButton botonGuardar = new JButton("Guardar");
        JButton botonEliminar = new JButton("Eliminar");
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.add(botonNuevo);
        acciones.add(botonGuardar);
        acciones.add(botonEliminar);

        JPanel superior = new JPanel(new BorderLayout(8, 8));
        superior.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        superior.add(formulario, BorderLayout.CENTER);
        superior.add(acciones, BorderLayout.SOUTH);
        add(superior, BorderLayout.NORTH);

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        busqueda.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        busqueda.add(new JLabel("Buscar por ID, nombre, DNI o correo:"));
        busqueda.add(campoBusqueda);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        JPanel centro = new JPanel(new BorderLayout(5, 5));
        centro.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        centro.add(busqueda, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        botonNuevo.addActionListener(evento -> limpiarFormulario());
        botonGuardar.addActionListener(evento -> guardarUsuario());
        botonEliminar.addActionListener(evento -> eliminarUsuario());
        tabla.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) {
                cargarSeleccionado();
            }
        });
        campoBusqueda.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent evento) { buscarUsuarios(); }
            @Override public void removeUpdate(DocumentEvent evento) { buscarUsuarios(); }
            @Override public void changedUpdate(DocumentEvent evento) { buscarUsuarios(); }
        });
    }

    private void guardarUsuario() {
        try {
            Usuario usuario;
            if (campoId.getText().isBlank()) {
                usuario = servicio.registrar(campoNombre.getText(), campoDni.getText(), campoCorreo.getText());
                JOptionPane.showMessageDialog(this, "Usuario registrado con ID " + usuario.getId());
            } else {
                usuario = new Usuario(campoId.getText(), campoNombre.getText(),
                        campoDni.getText(), campoCorreo.getText());
                servicio.actualizar(usuario);
                JOptionPane.showMessageDialog(this, "Usuario actualizado.");
            }
            limpiarFormulario();
            cargarUsuarios();
        } catch (ValidacionException | PersistenciaException | IllegalArgumentException error) {
            mostrarError(error);
        }
    }

    private void eliminarUsuario() {
        String id = campoId.getText();
        if (id.isBlank()) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario de la tabla.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Eliminar al usuario " + id + "?", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.eliminar(id);
            limpiarFormulario();
            cargarUsuarios();
        } catch (ValidacionException | PersistenciaException error) {
            mostrarError(error);
        }
    }

    private void buscarUsuarios() {
        try {
            mostrarUsuarios(servicio.buscar(campoBusqueda.getText()));
        } catch (PersistenciaException error) {
            mostrarError(error);
        }
    }

    private void cargarUsuarios() {
        try {
            if (campoBusqueda.getText().isBlank()) {
                mostrarUsuarios(servicio.listar());
            } else {
                mostrarUsuarios(servicio.buscar(campoBusqueda.getText()));
            }
        } catch (PersistenciaException error) {
            mostrarError(error);
        }
    }

    private void mostrarUsuarios(List<Usuario> usuarios) {
        modeloTabla.setRowCount(0);
        for (Usuario usuario : usuarios) {
            modeloTabla.addRow(new Object[]{usuario.getId(), usuario.getNombre(),
                    usuario.getDni(), usuario.getCorreo()});
        }
    }

    private void cargarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        campoId.setText(modeloTabla.getValueAt(fila, 0).toString());
        campoNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
        campoDni.setText(modeloTabla.getValueAt(fila, 2).toString());
        campoCorreo.setText(modeloTabla.getValueAt(fila, 3).toString());
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        campoId.setText("");
        campoNombre.setText("");
        campoDni.setText("");
        campoCorreo.setText("");
        campoNombre.requestFocusInWindow();
    }

    private void mostrarError(Exception error) {
        JOptionPane.showMessageDialog(this, error.getMessage(),
                "No se pudo completar la operación", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UsuarioView().setVisible(true));
    }
}
