package vista;

import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Libro;
import servicios.ServicioLibros;
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

/** Pantalla para consultar y administrar los libros de la biblioteca. */
public class LibroView extends JFrame {
    private final ServicioLibros servicio;
    private final JTextField txtBuscar = new JTextField(22);
    private final JTextField txtId = new JTextField();
    private final JTextField txtTitulo = new JTextField();
    private final JTextField txtAutor = new JTextField();
    private final JTextField txtTotales = new JTextField();
    private final JTextField txtDisponibles = new JTextField();
    private final JLabel tituloFormulario = new JLabel("Nuevo libro");
    private final JLabel mensaje = new JLabel(" ");
    private final JButton guardar = new JButton("Guardar libro");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Título", "Autor", "ID", "Ejemplares", "Disponibles", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private String idSeleccionado;

    public LibroView() {
        this(new ServicioLibros());
    }

    public LibroView(ServicioLibros servicio) {
        this.servicio = servicio;
        setTitle("Biblioteca - Catálogo de libros");
        setSize(1100, 650);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel principal = new JPanel(new BorderLayout(15, 15));
        principal.setBackground(new Color(249, 245, 240));
        principal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(principal);

        JLabel titulo = new JLabel("Catálogo de libros");
        titulo.setFont(new Font("Serif", Font.BOLD, 30));
        titulo.setForeground(new Color(102, 26, 24));
        principal.add(titulo, BorderLayout.NORTH);
        principal.add(crearCatalogo(), BorderLayout.CENTER);
        principal.add(crearFormulario(), BorderLayout.EAST);

        txtId.setEditable(false);
        txtDisponibles.setEditable(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(28);
        tabla.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                editarSeleccionado();
            }
        });
        txtBuscar.addActionListener(evento -> actualizarTabla());
        limpiarFormulario();
        actualizarTabla();
    }

    private JPanel crearCatalogo() {
        JPanel catalogo = new JPanel(new BorderLayout(0, 10));
        catalogo.setOpaque(false);

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        busqueda.setOpaque(false);
        busqueda.add(new JLabel("Buscar por título, autor o ID:"));
        busqueda.add(txtBuscar);
        JButton buscar = new JButton("Buscar");
        buscar.addActionListener(evento -> actualizarTabla());
        busqueda.add(buscar);
        JButton todos = new JButton("Ver todos");
        todos.addActionListener(evento -> {
            txtBuscar.setText("");
            actualizarTabla();
        });
        busqueda.add(todos);
        catalogo.add(busqueda, BorderLayout.NORTH);

        catalogo.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.setOpaque(false);
        JButton nuevo = new JButton("Nuevo libro");
        nuevo.addActionListener(evento -> limpiarFormulario());
        JButton editar = new JButton("Editar");
        editar.addActionListener(evento -> editarSeleccionado());
        JButton detalle = new JButton("Ver detalle");
        detalle.addActionListener(evento -> verSeleccionado());
        JButton eliminar = new JButton("Eliminar");
        eliminar.addActionListener(evento -> eliminarSeleccionado());
        JButton volver = new JButton("Volver");
        volver.addActionListener(evento -> volverAlInicio());
        acciones.add(nuevo);
        acciones.add(editar);
        acciones.add(detalle);
        acciones.add(eliminar);
        acciones.add(volver);
        acciones.add(mensaje);
        catalogo.add(acciones, BorderLayout.SOUTH);
        return catalogo;
    }

    private JPanel crearFormulario() {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 12));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        tarjeta.setPreferredSize(new Dimension(310, 0));
        tituloFormulario.setFont(new Font("Serif", Font.BOLD, 24));
        tarjeta.add(tituloFormulario, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(10, 1, 0, 4));
        campos.setBackground(Color.WHITE);
        agregarCampo(campos, "ID automático", txtId);
        agregarCampo(campos, "Título", txtTitulo);
        agregarCampo(campos, "Autor", txtAutor);
        agregarCampo(campos, "Ejemplares totales", txtTotales);
        agregarCampo(campos, "Disponibles (según préstamos)", txtDisponibles);
        tarjeta.add(new JScrollPane(campos), BorderLayout.CENTER);

        JPanel botones = new JPanel(new GridLayout(1, 2, 10, 0));
        botones.setBackground(Color.WHITE);
        JButton cancelar = new JButton("Cancelar");
        cancelar.addActionListener(evento -> limpiarFormulario());
        guardar.addActionListener(evento -> guardarLibro());
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
            tabla.clearSelection();
            modelo.setRowCount(0);
            for (Libro libro : servicio.buscar(txtBuscar.getText())) {
                String estado;
                if (libro.getEjemplaresDisponibles() == 0) {
                    estado = "No disponible";
                } else if (libro.getEjemplaresDisponibles() == 1) {
                    estado = "Pocos disponibles";
                } else {
                    estado = "Disponible";
                }
                modelo.addRow(new Object[]{
                    libro.getTitulo(), libro.getAutor(), libro.getId(),
                    libro.getEjemplaresTotales(), libro.getEjemplaresDisponibles(), estado
                });
            }
            mensaje.setText(modelo.getRowCount() + " libros");
        } catch (PersistenciaException error) {
            mostrarError(error.getMessage());
        }
    }

    private String idDeLaFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro de la tabla.");
            return null;
        }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        return modelo.getValueAt(filaModelo, 2).toString();
    }

    private void editarSeleccionado() {
        mostrarSeleccionado(false);
    }

    private void verSeleccionado() {
        mostrarSeleccionado(true);
    }

    private void mostrarSeleccionado(boolean soloLectura) {
        String id = idDeLaFila();
        if (id == null) return;
        try {
            Libro libro = servicio.buscarPorId(id);
            idSeleccionado = id;
            txtId.setText(id);
            txtTitulo.setText(libro.getTitulo());
            txtAutor.setText(libro.getAutor());
            txtTotales.setText(String.valueOf(libro.getEjemplaresTotales()));
            txtDisponibles.setText(String.valueOf(libro.getEjemplaresDisponibles()));
            tituloFormulario.setText(soloLectura ? "Detalle del libro" : "Editar libro");
            guardar.setText("Guardar cambios");
            guardar.setEnabled(!soloLectura);
            txtTitulo.setEditable(!soloLectura);
            txtAutor.setEditable(!soloLectura);
            txtTotales.setEditable(!soloLectura);
        } catch (PersistenciaException | ValidacionException error) {
            mostrarError(error.getMessage());
        }
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        idSeleccionado = null;
        txtId.setText("Se asignará al guardar");
        txtTitulo.setText("");
        txtAutor.setText("");
        txtTotales.setText("");
        txtDisponibles.setText("Se calcula al guardar");
        tituloFormulario.setText("Nuevo libro");
        guardar.setText("Guardar libro");
        guardar.setEnabled(true);
        txtTitulo.setEditable(true);
        txtAutor.setEditable(true);
        txtTotales.setEditable(true);
    }

    private void guardarLibro() {
        try {
            int total = Integer.parseInt(txtTotales.getText().strip());
            if (idSeleccionado == null) {
                servicio.registrar(txtTitulo.getText(), txtAutor.getText(), total);
            } else {
                servicio.modificar(idSeleccionado, txtTitulo.getText(), txtAutor.getText(), total);
            }
            actualizarTabla();
            limpiarFormulario();
            mensaje.setText("Libro guardado.");
        } catch (NumberFormatException error) {
            mostrarError("Escribe una cantidad entera de ejemplares.");
        } catch (PersistenciaException | ValidacionException | IllegalArgumentException error) {
            mostrarError(error.getMessage());
        }
    }

    private void eliminarSeleccionado() {
        String id = idDeLaFila();
        if (id == null) return;
        int respuesta = JOptionPane.showConfirmDialog(
                this, "¿Eliminar el libro " + id + "?", "Eliminar libro", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) return;
        try {
            servicio.eliminar(id);
            actualizarTabla();
            limpiarFormulario();
            mensaje.setText("Libro eliminado.");
        } catch (PersistenciaException | ValidacionException error) {
            mostrarError(error.getMessage());
        }
    }

    private void mostrarError(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Error", JOptionPane.ERROR_MESSAGE);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibroView().setVisible(true));
    }
}
