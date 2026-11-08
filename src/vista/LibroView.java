package vista;

import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Libro;
import servicios.ServicioLibros;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Pantalla independiente, preparada para compartir el servicio en la integración. */
public class LibroView extends JFrame {
    private final ServicioLibros servicio;
    private final JTextField txtId = new JTextField();
    private final JTextField txtTitulo = new JTextField();
    private final JTextField txtAutor = new JTextField();
    private final JTextField txtTotales = new JTextField();
    private final JTextField txtDisponibles = new JTextField();
    private final JTextField txtBuscar = new JTextField(20);
    private final JComboBox<String> tipoBusqueda = new JComboBox<>(new String[]{"Título", "ID"});
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Título", "Autor", "Totales", "Disponibles"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
        @Override public Class<?> getColumnClass(int columna) { return columna >= 3 ? Integer.class : String.class; }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JLabel estado = new JLabel(" ");
    private String idSeleccionado;
    private boolean actualizandoTabla;

    public LibroView() { this(new ServicioLibros()); }

    public LibroView(ServicioLibros servicio) {
        this.servicio = servicio;
        setTitle("Biblioteca · Catálogo de libros");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(760, 540));
        setSize(900, 600);
        setLocationRelativeTo(null);

        JPanel contenido = new JPanel(new BorderLayout(12, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setContentPane(contenido);
        JLabel encabezado = new JLabel("Catálogo de libros");
        encabezado.setFont(encabezado.getFont().deriveFont(Font.BOLD, 24f));
        JPanel formulario = new JPanel(new GridLayout(5, 2, 10, 8));
        txtId.setEditable(false);
        txtDisponibles.setEditable(false);
        agregarCampo(formulario, "ID automático", txtId);
        agregarCampo(formulario, "Título", txtTitulo);
        agregarCampo(formulario, "Autor", txtAutor);
        agregarCampo(formulario, "Ejemplares totales", txtTotales);
        agregarCampo(formulario, "Disponibles (según préstamos)", txtDisponibles);
        JPanel superior = new JPanel(new BorderLayout(10, 12));
        superior.add(encabezado, BorderLayout.NORTH);
        superior.add(formulario, BorderLayout.CENTER);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        boton(botones, "Registrar", this::registrar);
        boton(botones, "Modificar", this::modificar);
        boton(botones, "Eliminar", this::eliminar);
        boton(botones, "Nuevo / Limpiar", this::limpiar);
        superior.add(botones, BorderLayout.SOUTH);
        contenido.add(superior, BorderLayout.NORTH);

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        busqueda.add(new JLabel("Buscar por"));
        busqueda.add(tipoBusqueda);
        busqueda.add(txtBuscar);
        boton(busqueda, "Buscar", this::buscar);
        boton(busqueda, "Ver todos", () -> ejecutar(() -> mostrar(servicio.listar())));
        txtBuscar.addActionListener(e -> buscar());
        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.add(busqueda, BorderLayout.NORTH);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionar();
        });
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        contenido.add(centro, BorderLayout.CENTER);
        contenido.add(estado, BorderLayout.SOUTH);
        limpiar();
        ejecutar(() -> mostrar(servicio.listar()));
    }

    private void agregarCampo(JPanel panel, String etiqueta, JTextField campo) {
        JLabel label = new JLabel(etiqueta);
        label.setLabelFor(campo);
        panel.add(label);
        panel.add(campo);
    }

    private void boton(JPanel panel, String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(e -> accion.run());
        panel.add(boton);
    }

    private int leerTotal() {
        String texto = txtTotales.getText().strip();
        if (!texto.matches("[0-9]+")) throw new NumberFormatException();
        return Integer.parseInt(texto);
    }

    private void registrar() {
        if (idSeleccionado != null) {
            JOptionPane.showMessageDialog(this, "Pulsa Nuevo / Limpiar antes de registrar otro libro.");
            return;
        }
        ejecutar(() -> {
            Libro nuevo = servicio.registrar(txtTitulo.getText(), txtAutor.getText(), leerTotal());
            mostrar(servicio.listar());
            limpiar();
            estado.setText("Libro registrado: " + nuevo.getId());
        });
    }

    private void modificar() {
        ejecutar(() -> {
            exigirSeleccion();
            servicio.modificar(idSeleccionado, txtTitulo.getText(), txtAutor.getText(), leerTotal());
            mostrar(servicio.listar());
            limpiar();
            estado.setText("Libro modificado correctamente.");
        });
    }

    private void eliminar() {
        ejecutar(() -> {
            exigirSeleccion();
            if (JOptionPane.showConfirmDialog(this, "¿Eliminar el libro " + idSeleccionado + "?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            servicio.eliminar(idSeleccionado);
            mostrar(servicio.listar());
            limpiar();
            estado.setText("Libro eliminado correctamente.");
        });
    }

    private void buscar() {
        ejecutar(() -> {
            if (tipoBusqueda.getSelectedIndex() == 1) {
                mostrar(java.util.Collections.singletonList(servicio.buscarPorId(txtBuscar.getText())));
            } else mostrar(servicio.buscar(txtBuscar.getText()));
        });
    }

    private void mostrar(List<Libro> libros) {
        actualizandoTabla = true;
        try {
            limpiar();
            modeloTabla.setRowCount(0);
            for (Libro libro : libros) modeloTabla.addRow(new Object[]{libro.getId(), libro.getTitulo(),
                libro.getAutor(), libro.getEjemplaresTotales(), libro.getEjemplaresDisponibles()});
            estado.setText(libros.size() + " libro(s) en la lista.");
        } finally {
            actualizandoTabla = false;
        }
    }

    private void seleccionar() {
        if (actualizandoTabla) return;
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        int indice = tabla.convertRowIndexToModel(fila);
        idSeleccionado = modeloTabla.getValueAt(indice, 0).toString();
        txtId.setText(idSeleccionado);
        txtTitulo.setText(modeloTabla.getValueAt(indice, 1).toString());
        txtAutor.setText(modeloTabla.getValueAt(indice, 2).toString());
        txtTotales.setText(modeloTabla.getValueAt(indice, 3).toString());
        txtDisponibles.setText(modeloTabla.getValueAt(indice, 4).toString());
    }

    private void limpiar() {
        tabla.clearSelection();
        idSeleccionado = null;
        txtId.setText("Se asignará al registrar");
        txtTitulo.setText("");
        txtAutor.setText("");
        txtTotales.setText("");
        txtDisponibles.setText("");
    }

    private void exigirSeleccion() throws ValidacionException {
        if (idSeleccionado == null) throw new ValidacionException("Selecciona un libro de la tabla.");
    }

    private interface Accion {
        void ejecutar() throws PersistenciaException, ValidacionException;
    }

    private void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (NumberFormatException e) {
            error("Escribe un número entero entre 0 y " + Integer.MAX_VALUE + " para los ejemplares.");
        } catch (PersistenciaException | ValidacionException | IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "No se pudo completar la operación", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibroView().setVisible(true));
    }
}
