package vista;

import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Usuario;
import servicios.ServicioUsuarios;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/** Pantalla para registrar, consultar, modificar y eliminar usuarios. */
public class UsuarioView extends JFrame {

    private static final Color FONDO = new Color(249, 245, 240);
    private static final Color CREMA = new Color(246, 232, 224);
    private static final Color TERRACOTA = new Color(183, 76, 64);
    private static final Color VINO = new Color(130, 49, 40);
    private static final Color LINEA = new Color(222, 210, 201);
    private static final Color TEXTO = new Color(48, 43, 40);

    private final ServicioUsuarios servicio;
    private final JTextField campoId = new JTextField();
    private final JTextField campoNombre = new JTextField();
    private final JTextField campoDni = new JTextField();
    private final JTextField campoCorreo = new JTextField();
    private final JTextField campoBusqueda = new JTextField(22);
    private String idSeleccionado;
    private boolean actualizandoTabla;

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "DNI", "Correo", "Préstamos activos", "Acciones"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return columna == 5;
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
        setTitle("Biblioteca · Usuarios");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 700));
        setSize(1500, 820);
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FONDO);
        setContentPane(raiz);
        raiz.add(crearBarraLateral(), BorderLayout.WEST);

        JPanel contenido = new JPanel(new BorderLayout(0, 22));
        contenido.setBackground(FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        contenido.add(crearCabecera(), BorderLayout.NORTH);
        contenido.add(crearZonaTrabajo(), BorderLayout.CENTER);
        raiz.add(contenido, BorderLayout.CENTER);
    }

    private JPanel crearBarraLateral() {
        JPanel barra = new JPanel();
        barra.setLayout(new javax.swing.BoxLayout(barra, javax.swing.BoxLayout.Y_AXIS));
        barra.setBackground(CREMA);
        barra.setBorder(BorderFactory.createEmptyBorder(28, 12, 20, 12));
        barra.setPreferredSize(new Dimension(245, 0));

        JLabel marca = new JLabel("▤  Biblioteca");
        marca.setFont(new Font("Serif", Font.BOLD, 27));
        marca.setForeground(VINO);
        marca.setBorder(BorderFactory.createEmptyBorder(4, 10, 25, 4));
        barra.add(marca);

        JPanel separador = new JPanel();
        separador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separador.setBackground(LINEA);
        barra.add(separador);
        barra.add(javax.swing.Box.createVerticalStrut(24));

        agregarOpcionMenu(barra, "⌂", "Inicio", false);
        agregarOpcionMenu(barra, "▤", "Libros", false);
        agregarOpcionMenu(barra, "♙", "Usuarios", true);
        agregarOpcionMenu(barra, "▦", "Préstamos", false);
        return barra;
    }

    private void agregarOpcionMenu(JPanel barra, String icono, String texto, boolean activa) {
        JPanel opcion = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        opcion.setAlignmentX(Component.LEFT_ALIGNMENT);
        opcion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        opcion.setBackground(activa ? TERRACOTA : CREMA);
        opcion.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        JLabel iconoLabel = new JLabel(icono);
        iconoLabel.setFont(new Font("SansSerif", Font.PLAIN, 22));
        iconoLabel.setForeground(activa ? Color.WHITE : VINO);
        JLabel textoLabel = new JLabel(texto);
        textoLabel.setFont(new Font("Serif", activa ? Font.BOLD : Font.PLAIN, 19));
        textoLabel.setForeground(activa ? Color.WHITE : TEXTO);
        opcion.add(iconoLabel);
        opcion.add(textoLabel);
        barra.add(opcion);
        barra.add(javax.swing.Box.createVerticalStrut(8));
    }

    private JPanel crearCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout(0, 14));
        cabecera.setOpaque(false);

        JPanel titulos = new JPanel(new GridLayout(2, 1, 0, 0));
        titulos.setOpaque(false);
        JLabel titulo = new JLabel("Usuarios");
        titulo.setFont(new Font("Serif", Font.BOLD, 42));
        titulo.setForeground(VINO);
        JLabel subtitulo = new JLabel("Administra a las personas registradas");
        subtitulo.setFont(new Font("Serif", Font.PLAIN, 20));
        subtitulo.setForeground(TEXTO);
        titulos.add(titulo);
        titulos.add(subtitulo);
        cabecera.add(titulos, BorderLayout.NORTH);

        JPanel herramientas = new JPanel(new BorderLayout(16, 0));
        herramientas.setOpaque(false);
        JPanel busqueda = new JPanel(new BorderLayout(10, 0));
        busqueda.setBackground(Color.WHITE);
        busqueda.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(198, 183, 173), 1, true),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        JLabel lupa = new JLabel("⌕");
        lupa.setFont(new Font("SansSerif", Font.PLAIN, 27));
        lupa.setForeground(new Color(125, 117, 111));
        campoBusqueda.setBorder(BorderFactory.createEmptyBorder(5, 3, 5, 3));
        campoBusqueda.setFont(new Font("SansSerif", Font.PLAIN, 16));
        campoBusqueda.setToolTipText("Buscar por nombre o DNI");
        busqueda.add(lupa, BorderLayout.WEST);
        busqueda.add(campoBusqueda, BorderLayout.CENTER);
        herramientas.add(busqueda, BorderLayout.CENTER);

        JButton nuevo = botonPrincipal("＋  Nuevo usuario");
        nuevo.setPreferredSize(new Dimension(230, 52));
        nuevo.addActionListener(evento -> limpiarFormulario());
        herramientas.add(nuevo, BorderLayout.EAST);
        cabecera.add(herramientas, BorderLayout.CENTER);

        campoBusqueda.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent evento) { buscarUsuarios(); }
            @Override public void removeUpdate(DocumentEvent evento) { buscarUsuarios(); }
            @Override public void changedUpdate(DocumentEvent evento) { buscarUsuarios(); }
        });
        return cabecera;
    }

    private JSplitPane crearZonaTrabajo() {
        JPanel panelLista = new JPanel(new BorderLayout());
        panelLista.setBackground(Color.WHITE);
        panelLista.setBorder(BorderFactory.createLineBorder(LINEA, 1, true));
        configurarTabla();
        JScrollPane desplazamiento = new JScrollPane(tabla);
        desplazamiento.setBorder(BorderFactory.createEmptyBorder());
        desplazamiento.getViewport().setBackground(Color.WHITE);
        panelLista.add(desplazamiento, BorderLayout.CENTER);

        JPanel formulario = crearFormulario();
        JSplitPane zona = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelLista, formulario);
        zona.setBorder(BorderFactory.createEmptyBorder());
        zona.setOpaque(false);
        zona.setDividerSize(14);
        zona.setResizeWeight(0.72);
        zona.setDividerLocation(0.72);
        return zona;
    }

    private void configurarTabla() {
        tabla.setRowHeight(76);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tabla.setForeground(TEXTO);
        tabla.setGridColor(LINEA);
        tabla.setShowVerticalLines(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setSelectionBackground(new Color(250, 237, 231));
        tabla.setSelectionForeground(TEXTO);
        tabla.setFillsViewportHeight(true);
        tabla.setIntercellSpacing(new Dimension(0, 1));

        javax.swing.table.JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setPreferredSize(new Dimension(0, 68));
        encabezado.setBackground(CREMA);
        encabezado.setForeground(VINO);
        encabezado.setFont(new Font("Serif", Font.BOLD, 16));
        encabezado.setReorderingAllowed(false);

        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        centrado.setBackground(Color.WHITE);
        tabla.getColumnModel().getColumn(2).setCellRenderer(centrado);
        tabla.getColumnModel().getColumn(4).setCellRenderer(centrado);
        tabla.getColumnModel().getColumn(1).setCellRenderer(new NombreCellRenderer());
        tabla.getColumnModel().getColumn(5).setCellRenderer(new AccionesCellRenderer());
        tabla.getColumnModel().getColumn(5).setCellEditor(new AccionesCellEditor());

        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(0);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(210);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(240);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(120);

        tabla.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting() && !actualizandoTabla) {
                cargarSeleccionado();
            }
        });
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                int fila = tabla.rowAtPoint(evento.getPoint());
                if (fila >= 0) {
                    tabla.setRowSelectionInterval(fila, fila);
                }
            }
        });
    }

    private JPanel crearFormulario() {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 18));
        tarjeta.setBackground(new Color(253, 250, 247));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINEA, 1, true),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));

        JLabel titulo = new JLabel("Registrar usuario");
        titulo.setFont(new Font("Serif", Font.BOLD, 28));
        titulo.setForeground(VINO);
        tarjeta.add(titulo, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(0, 1, 0, 6));
        campos.setOpaque(false);
        campoId.setEditable(false);
        campoId.setText("Se asigna al guardar");
        estiloCampo(campoId, true);
        estiloCampo(campoNombre, false);
        estiloCampo(campoDni, false);
        estiloCampo(campoCorreo, false);
        agregarCampo(campos, "ID automático", campoId, false);
        agregarCampo(campos, "Nombre completo", campoNombre, true);
        agregarCampo(campos, "DNI", campoDni, true);
        agregarCampo(campos, "Correo electrónico", campoCorreo, true);
        tarjeta.add(campos, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new GridLayout(1, 2, 10, 0));
        acciones.setOpaque(false);
        JButton cancelar = botonSecundario("Cancelar");
        cancelar.addActionListener(evento -> limpiarFormulario());
        JButton guardar = botonPrincipal("Guardar usuario");
        guardar.addActionListener(evento -> guardarUsuario());
        acciones.add(cancelar);
        acciones.add(guardar);
        tarjeta.add(acciones, BorderLayout.SOUTH);
        return tarjeta;
    }

    private void agregarCampo(JPanel panel, String etiqueta, JTextField campo, boolean obligatorio) {
        JLabel label = new JLabel(etiqueta + (obligatorio ? "  *" : ""));
        label.setFont(new Font("Serif", Font.BOLD, 16));
        label.setForeground(TEXTO);
        label.setBorder(BorderFactory.createEmptyBorder(8, 0, 1, 0));
        panel.add(label);
        panel.add(campo);
    }

    private void estiloCampo(JTextField campo, boolean deshabilitado) {
        campo.setFont(new Font("SansSerif", Font.PLAIN, 15));
        campo.setPreferredSize(new Dimension(180, 42));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196, 185, 178), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        if (deshabilitado) {
            campo.setBackground(new Color(238, 233, 230));
            campo.setForeground(new Color(105, 99, 95));
        } else {
            campo.setBackground(Color.WHITE);
        }
    }

    private JButton botonPrincipal(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Serif", Font.BOLD, 16));
        boton.setForeground(Color.WHITE);
        boton.setBackground(TERRACOTA);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        return boton;
    }

    private JButton botonSecundario(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Serif", Font.BOLD, 16));
        boton.setForeground(new Color(95, 68, 55));
        boton.setBackground(new Color(253, 250, 247));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 171, 158), 1, true),
                BorderFactory.createEmptyBorder(11, 12, 11, 12)));
        return boton;
    }

    private void guardarUsuario() {
        try {
            if (idSeleccionado == null) {
                Usuario nuevo = servicio.registrar(campoNombre.getText(), campoDni.getText(), campoCorreo.getText());
                JOptionPane.showMessageDialog(this, "Usuario registrado con ID " + nuevo.getId());
            } else {
                Usuario actualizado = new Usuario(idSeleccionado, campoNombre.getText(),
                        campoDni.getText(), campoCorreo.getText());
                servicio.actualizar(actualizado);
                JOptionPane.showMessageDialog(this, "Usuario actualizado.");
            }
            limpiarFormulario();
            cargarUsuarios();
        } catch (ValidacionException | PersistenciaException | IllegalArgumentException error) {
            mostrarError(error);
        }
    }

    private void eliminarUsuario(String id) {
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
        actualizandoTabla = true;
        try {
            modeloTabla.setRowCount(0);
            for (Usuario usuario : usuarios) {
                modeloTabla.addRow(new Object[]{usuario.getId(), usuario.getNombre(),
                        usuario.getDni(), usuario.getCorreo(), "—", ""});
            }
        } finally {
            actualizandoTabla = false;
        }
    }

    private void cargarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        idSeleccionado = modeloTabla.getValueAt(filaModelo, 0).toString();
        campoId.setText(idSeleccionado);
        campoNombre.setText(modeloTabla.getValueAt(filaModelo, 1).toString());
        campoDni.setText(modeloTabla.getValueAt(filaModelo, 2).toString());
        campoCorreo.setText(modeloTabla.getValueAt(filaModelo, 3).toString());
    }

    private void seleccionarUsuario(int filaVista) {
        if (filaVista < 0 || filaVista >= tabla.getRowCount()) {
            return;
        }
        tabla.setRowSelectionInterval(filaVista, filaVista);
        cargarSeleccionado();
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        idSeleccionado = null;
        campoId.setText("Se asigna al guardar");
        campoNombre.setText("");
        campoDni.setText("");
        campoCorreo.setText("");
        campoNombre.requestFocusInWindow();
    }

    private void mostrarError(Exception error) {
        JOptionPane.showMessageDialog(this, error.getMessage(),
                "No se pudo completar la operación", JOptionPane.ERROR_MESSAGE);
    }

    private final class NombreCellRenderer extends JPanel implements TableCellRenderer {
        private final JLabel iniciales = new JLabel("", SwingConstants.CENTER);
        private final JLabel nombre = new JLabel();

        private NombreCellRenderer() {
            super(new FlowLayout(FlowLayout.LEFT, 12, 12));
            setOpaque(true);
            iniciales.setPreferredSize(new Dimension(48, 48));
            iniciales.setOpaque(true);
            iniciales.setBackground(new Color(244, 216, 207));
            iniciales.setForeground(VINO);
            iniciales.setFont(new Font("Serif", Font.BOLD, 16));
            nombre.setFont(new Font("Serif", Font.BOLD, 15));
            nombre.setForeground(TEXTO);
            add(iniciales);
            add(nombre);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
                boolean foco, int fila, int columna) {
            String texto = valor == null ? "" : valor.toString();
            nombre.setText("<html><div style='width:135px'>" + escaparHtml(texto) + "</div></html>");
            iniciales.setText(obtenerIniciales(texto));
            setBackground(seleccionado ? tabla.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    private static String obtenerIniciales(String nombre) {
        String[] partes = nombre.strip().split("\\s+");
        if (partes.length == 0 || partes[0].isBlank()) {
            return "?";
        }
        return partes.length == 1
                ? partes[0].substring(0, 1).toUpperCase()
                : (partes[0].substring(0, 1) + partes[partes.length - 1].substring(0, 1)).toUpperCase();
    }

    private static String escaparHtml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private final class AccionesCellRenderer extends JPanel implements TableCellRenderer {
        private final JLabel editar = etiquetaAccion("✎");
        private final JLabel eliminar = etiquetaAccion("▤");

        private AccionesCellRenderer() {
            super(new FlowLayout(FlowLayout.CENTER, 6, 20));
            setOpaque(true);
            add(editar);
            add(eliminar);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
                boolean foco, int fila, int columna) {
            setBackground(seleccionado ? tabla.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    private JLabel etiquetaAccion(String texto) {
        JLabel etiqueta = new JLabel(texto, SwingConstants.CENTER);
        etiqueta.setPreferredSize(new Dimension(38, 34));
        etiqueta.setBorder(BorderFactory.createLineBorder(new Color(190, 171, 158), 1, true));
        etiqueta.setForeground(VINO);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 18));
        return etiqueta;
    }

    private final class AccionesCellEditor extends javax.swing.AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 16));
        private final JButton editar = botonAccion("✎");
        private final JButton eliminar = botonAccion("▤");
        private int filaVista;

        private AccionesCellEditor() {
            panel.setBackground(Color.WHITE);
            panel.add(editar);
            panel.add(eliminar);
            editar.addActionListener(evento -> {
                fireEditingStopped();
                seleccionarUsuario(filaVista);
            });
            eliminar.addActionListener(evento -> {
                int fila = filaVista;
                fireEditingStopped();
                int filaModelo = tabla.convertRowIndexToModel(fila);
                eliminarUsuario(modeloTabla.getValueAt(filaModelo, 0).toString());
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable tabla, Object valor,
                boolean seleccionado, int fila, int columna) {
            filaVista = fila;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        private JButton botonAccion(String texto) {
            JButton boton = new JButton(texto);
            boton.setMargin(new Insets(0, 0, 0, 0));
            boton.setPreferredSize(new Dimension(38, 34));
            boton.setFocusPainted(false);
            boton.setBackground(new Color(253, 250, 247));
            boton.setForeground(VINO);
            boton.setBorder(BorderFactory.createLineBorder(new Color(190, 171, 158), 1, true));
            boton.setFont(new Font("SansSerif", Font.BOLD, 18));
            return boton;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UsuarioView().setVisible(true));
    }
}
