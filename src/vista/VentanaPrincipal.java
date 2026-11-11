
package vista;



public class VentanaPrincipal extends javax.swing.JFrame {
    private final servicios.SistemaBiblioteca sistema;
    private LibroView vistaLibros;
    private PrestamoView vistaPrestamos;
    
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName());

    
    public VentanaPrincipal() {
        this(new servicios.SistemaBiblioteca());
    }

    public VentanaPrincipal(servicios.SistemaBiblioteca sistema) {
        this.sistema = java.util.Objects.requireNonNull(sistema);
        initComponents();
        configurarNavegacion();
        setTitle("Biblioteca - Menú principal");
        setLocationRelativeTo(null);
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

    @Override
    public void dispose() {
        if (vistaLibros != null) vistaLibros.dispose();
        if (vistaPrestamos != null) vistaPrestamos.dispose();
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
