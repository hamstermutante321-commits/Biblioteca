package vista;

import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Usuario;
import servicios.ServicioUsuarios;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
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

/** Pantalla para registrar y consultar usuarios de la biblioteca. */
public class UsuarioView extends JFrame {
    private final ServicioUsuarios servicio;
    private final JTextField campoBusqueda = new JTextField(22);
    private final JTextField campoId = new JTextField();
    private final JTextField campoNombre = new JTextField();
    private final JTextField campoDni = new JTextField();
    private final JTextField campoCorreo = new JTextField();
    private final JLabel tituloFormulario = new JLabel("Nuevo usuario");
    private final JLabel mensaje = new JLabel(" ");
    private final JButton guardar = new JButton("Guardar usuario");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Nombre", "DNI", "Correo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private String idSeleccionado;

    public UsuarioView() {
        this(new ServicioUsuarios());
    }

    public UsuarioView(ServicioUsuarios servicio) {
        this.servicio = servicio;
        setTitle("Biblioteca - Usuarios");
        setSize(1050, 650);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel principal = new JPanel(new BorderLayout(15, 15));
        principal.setBackground(new Color(249, 245, 240));
        principal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(principal);

        JLabel titulo = new JLabel("Usuarios");
        titulo.setFont(new Font("Serif", Font.BOLD, 30));
        titulo.setForeground(new Color(102, 26, 24));
        principal.add(titulo, BorderLayout.NORTH);
        principal.add(crearLista(), BorderLayout.CENTER);
        principal.add(crearFormulario(), BorderLayout.EAST);

        campoId.setEditable(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(28);
        tabla.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                cargarSeleccionado();
            }
        });
        campoBusqueda.addActionListener(evento -> actualizarTabla());
        limpiarFormulario();
        actualizarTabla();
    }

    private JPanel crearLista() {
        JPanel lista = new JPanel(new BorderLayout(0, 10));
        lista.setOpaque(false);

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        busqueda.setOpaque(false);
        busqueda.add(new JLabel("Buscar usuario:"));
        busqueda.add(campoBusqueda);
        JButton buscar = new JButton("Buscar");
        buscar.addActionListener(evento -> actualizarTabla());
        busqueda.add(buscar);
        JButton todos = new JButton("Ver todos");
        todos.addActionListener(evento -> {
            campoBusqueda.setText("");
            actualizarTabla();
        });
        busqueda.add(todos);
        lista.add(busqueda, BorderLayout.NORTH);
        lista.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.setOpaque(false);
        JButton nuevo = new JButton("Nuevo usuario");
        nuevo.addActionListener(evento -> limpiarFormulario());
        JButton editar = new JButton("Editar");
        editar.addActionListener(evento -> cargarSeleccionado());
        JButton eliminar = new JButton("Eliminar");
        eliminar.addActionListener(evento -> eliminarSeleccionado());
        JButton volver = new JButton("Volver");
        volver.addActionListener(evento -> volverAlInicio());
        acciones.add(nuevo);
        acciones.add(editar);
        acciones.add(eliminar);
        acciones.add(volver);
        acciones.add(mensaje);
        lista.add(acciones, BorderLayout.SOUTH);
        return lista;
    }

    private JPanel crearFormulario() {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 12));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        tarjeta.setPreferredSize(new Dimension(310, 0));
        tituloFormulario.setFont(new Font("Serif", Font.BOLD, 24));
        tarjeta.add(tituloFormulario, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(8, 1, 0, 5));
        campos.setBackground(Color.WHITE);
        agregarCampo(campos, "ID automático", campoId);
        agregarCampo(campos, "Nombre completo", campoNombre);
        agregarCampo(campos, "DNI", campoDni);
        agregarCampo(campos, "Correo electrónico", campoCorreo);
        tarjeta.add(new JScrollPane(campos), BorderLayout.CENTER);

        JPanel botones = new JPanel(new GridLayout(1, 2, 10, 0));
        botones.setBackground(Color.WHITE);
        JButton cancelar = new JButton("Cancelar");
        cancelar.addActionListener(evento -> limpiarFormulario());
        guardar.addActionListener(evento -> guardarUsuario());
        botones.add(cancelar);
        botones.add(guardar);
        tarjeta.add(botones, BorderLayout.SOUTH);
        return tarjeta;
    }

    private void agregarCampo(JPanel panel, String nombre, JTextField campo) {
        JLabel etiqueta = new JLabel(nombre);
        etiqueta.setLabelFor(campo);
        panel.add(etiqueta);
        panel.add(campo);
    }

    private void actualizarTabla() {
        try {
            var usuarios = servicio.buscar(campoBusqueda.getText());
            tabla.clearSelection();
            modelo.setRowCount(0);
            for (Usuario usuario : usuarios) {
                modelo.addRow(new Object[]{
                    usuario.getId(), usuario.getNombre(),
                    usuario.getDni(), usuario.getCorreo()
                });
            }
            mensaje.setText(modelo.getRowCount() + " usuarios");
        } catch (PersistenciaException error) {
            mostrarError(error.getMessage());
        }
    }

    private String idDeLaFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario de la tabla.");
            return null;
        }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        return modelo.getValueAt(filaModelo, 0).toString();
    }

    private void cargarSeleccionado() {
        String id = idDeLaFila();
        if (id == null) return;
        try {
            Usuario usuario = servicio.buscarPorId(id);
            idSeleccionado = usuario.getId();
            campoId.setText(usuario.getId());
            campoNombre.setText(usuario.getNombre());
            campoDni.setText(usuario.getDni());
            campoCorreo.setText(usuario.getCorreo());
            tituloFormulario.setText("Editar usuario");
            guardar.setText("Guardar cambios");
        } catch (PersistenciaException | ValidacionException error) {
            mostrarError(error.getMessage());
        }
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        idSeleccionado = null;
        campoId.setText("Se asignará al guardar");
        campoNombre.setText("");
        campoDni.setText("");
        campoCorreo.setText("");
        tituloFormulario.setText("Nuevo usuario");
        guardar.setText("Guardar usuario");
    }

    private void guardarUsuario() {
        try {
            if (idSeleccionado == null) {
                servicio.registrar(campoNombre.getText(), campoDni.getText(), campoCorreo.getText());
            } else {
                Usuario usuario = new Usuario(idSeleccionado, campoNombre.getText(),
                        campoDni.getText(), campoCorreo.getText());
                servicio.actualizar(usuario);
            }
            actualizarTabla();
            limpiarFormulario();
            mensaje.setText("Usuario guardado.");
        } catch (PersistenciaException | ValidacionException | IllegalArgumentException error) {
            mostrarError(error.getMessage());
        }
    }

    private void eliminarSeleccionado() {
        String id = idDeLaFila();
        if (id == null) return;
        int respuesta = JOptionPane.showConfirmDialog(
                this, "¿Eliminar al usuario " + id + "?", "Eliminar usuario", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) return;
        try {
            servicio.eliminar(id);
            actualizarTabla();
            limpiarFormulario();
            mensaje.setText("Usuario eliminado.");
        } catch (PersistenciaException | ValidacionException error) {
            mostrarError(error.getMessage());
        }
    }

    private void volverAlInicio() {
        for (Window ventana : Window.getWindows()) {
            if (ventana instanceof VentanaPrincipal && ventana.isDisplayable()) {
                ventana.setVisible(true);
                ventana.toFront();
                dispose();
                return;
            }
        }
        new VentanaPrincipal().setVisible(true);
        dispose();
    }

    private void mostrarError(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UsuarioView().setVisible(true));
    }
}
