
package vista;



public class VentanaPrincipal extends javax.swing.JFrame {
    private final servicios.SistemaBiblioteca sistema;
    private LibroView vistaLibros;
    private PrestamoView vistaPrestamos;
    private UsuarioView vistaUsuarios;
    private DetallePrestamoView vistaDetalles;
    
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName());

    
    public VentanaPrincipal() {
        this(new servicios.SistemaBiblioteca());
    }

    public VentanaPrincipal(servicios.SistemaBiblioteca sistema) {
        this.sistema = java.util.Objects.requireNonNull(sistema);
        initComponents();
        configurarNavegacion();
        crearPantallaPrincipal();
        setTitle("Biblioteca - Menú principal");
        setLocationRelativeTo(null);
    }

    private void crearPantallaPrincipal() {
        javax.swing.JPanel principal = new javax.swing.JPanel(new java.awt.BorderLayout(15, 15));
        principal.setBorder(javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25));

        javax.swing.JLabel titulo = new javax.swing.JLabel("Sistema de biblioteca", javax.swing.SwingConstants.CENTER);
        titulo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));
        principal.add(titulo, java.awt.BorderLayout.NORTH);

        javax.swing.JPanel botones = new javax.swing.JPanel(new java.awt.GridLayout(2, 2, 12, 12));
        javax.swing.JButton libros = new javax.swing.JButton("Libros");
        javax.swing.JButton usuarios = new javax.swing.JButton("Usuarios");
        javax.swing.JButton prestamos = new javax.swing.JButton("Préstamos");
        javax.swing.JButton detalles = new javax.swing.JButton("Detalle de préstamo");

        libros.addActionListener(evento -> abrirLibros());
        usuarios.addActionListener(evento -> abrirUsuarios());
        prestamos.addActionListener(evento -> abrirPrestamos());
        detalles.addActionListener(evento -> abrirDetalles());

        botones.add(libros);
        botones.add(usuarios);
        botones.add(prestamos);
        botones.add(detalles);
        principal.add(botones, java.awt.BorderLayout.CENTER);

        javax.swing.JLabel indicacion = new javax.swing.JLabel(
                "Selecciona un módulo para comenzar.", javax.swing.SwingConstants.CENTER);
        principal.add(indicacion, java.awt.BorderLayout.SOUTH);
        setContentPane(principal);
        setSize(600, 350);
    }

    private void configurarNavegacion() {
        javax.swing.JMenuBar barra = new javax.swing.JMenuBar();
        javax.swing.JMenu catalogo = new javax.swing.JMenu("Catálogo");
        javax.swing.JMenuItem libros = new javax.swing.JMenuItem("Libros");
        libros.addActionListener(evento -> abrirLibros());
        catalogo.add(libros);
        javax.swing.JMenuItem prestamos = new javax.swing.JMenuItem("Préstamos");
        prestamos.addActionListener(evento -> abrirPrestamos());
        catalogo.add(prestamos);
        javax.swing.JMenuItem usuarios = new javax.swing.JMenuItem("Usuarios");
        usuarios.addActionListener(evento -> abrirUsuarios());
        catalogo.add(usuarios);
        javax.swing.JMenuItem detalles = new javax.swing.JMenuItem("Detalle de préstamo");
        detalles.addActionListener(evento -> abrirDetalles());
        catalogo.add(detalles);
        barra.add(catalogo);
        setJMenuBar(barra);
        pack();
    }

    private void abrirLibros() {
        if (vistaLibros == null || !vistaLibros.isDisplayable()) {
            vistaLibros = new LibroView(sistema.getServicioLibros());
        }
        vistaLibros.setVisible(true);
        vistaLibros.toFront();
    }

    private void abrirPrestamos() {
        if (vistaPrestamos == null || !vistaPrestamos.isDisplayable()) {
            vistaPrestamos = new PrestamoView();
        }
        vistaPrestamos.setVisible(true);
        vistaPrestamos.toFront();
    }

    private void abrirUsuarios() {
        if (vistaUsuarios == null || !vistaUsuarios.isDisplayable()) {
            vistaUsuarios = new UsuarioView();
        }
        vistaUsuarios.setVisible(true);
        vistaUsuarios.toFront();
    }

    private void abrirDetalles() {
        if (vistaDetalles == null || !vistaDetalles.isDisplayable()) {
            vistaDetalles = new DetallePrestamoView();
        }
        vistaDetalles.setVisible(true);
        vistaDetalles.toFront();
    }

    @Override
    public void dispose() {
        if (vistaLibros != null) vistaLibros.dispose();
        if (vistaPrestamos != null) vistaPrestamos.dispose();
        if (vistaUsuarios != null) vistaUsuarios.dispose();
        if (vistaDetalles != null) vistaDetalles.dispose();
        super.dispose();
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Biblioteca");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(261, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 230, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  
    public static void main(String args[]) {
 
        java.awt.EventQueue.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
