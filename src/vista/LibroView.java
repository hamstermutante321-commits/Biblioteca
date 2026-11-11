package vista;

import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Libro;
import servicios.ServicioLibros;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.List;

/** Catálogo Swing con formulario lateral y acciones por libro. */
public class LibroView extends JFrame {
    private static final Color FONDO=new Color(250,246,240), PAPEL=new Color(255,252,248), BORDE=new Color(225,209,198);
    private static final Color TINTA=new Color(43,36,31), VINO=new Color(102,26,24), ACENTO=new Color(181,95,75), MUTED=new Color(115,104,94);
    private static final Font TEXTO=new Font("SansSerif",Font.PLAIN,15), TITULO=new Font("Serif",Font.BOLD,36);
    private final ServicioLibros servicio;
    private final JTextField txtId=campo(), txtTitulo=campo(), txtAutor=campo(), txtTotales=campo(), txtDisponibles=campo();
    private final JTextField txtBuscar=new JTextField(20);
    private final JLabel estado=new JLabel(" "), tituloFormulario=new JLabel("Nuevo libro");
    private final JButton guardar=boton("Guardar libro",true), cancelar=boton("Cancelar",false);
    private final JPanel tarjeta=new Redondo(PAPEL,16);
    private final DefaultTableModel modeloTabla=new DefaultTableModel(new String[]{"Libro","ID","Ejemplares","Disponibles","Estado","Acciones"},0) {
        @Override public boolean isCellEditable(int r,int c){return c==5;}
    };
    private final JTable tabla=new JTable(modeloTabla);
    private final Timer filtro;
    private String idSeleccionado;
    private boolean actualizandoTabla;

    public LibroView(){this(new ServicioLibros());}
    public LibroView(ServicioLibros servicio){
        this.servicio=java.util.Objects.requireNonNull(servicio);
        setTitle("Biblioteca · Catálogo de libros");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1120,680));
        Dimension pantalla=Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1560,pantalla.width-40),Math.min(900,pantalla.height-60));
        setLocationRelativeTo(null);
        JPanel raiz=new JPanel(new BorderLayout()); raiz.setBackground(FONDO); setContentPane(raiz);
        raiz.add(lateral(),BorderLayout.WEST);
        JPanel cuerpo=new JPanel(new BorderLayout(22,0)); cuerpo.setOpaque(false); cuerpo.setBorder(BorderFactory.createEmptyBorder(34,24,28,24));
        raiz.add(cuerpo,BorderLayout.CENTER);
        cuerpo.add(catalogo(),BorderLayout.CENTER);
        cuerpo.add(formulario(),BorderLayout.EAST);
        filtro=new Timer(250,e->buscar()); filtro.setRepeats(false);
        txtBuscar.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e){filtro.restart();}
            public void removeUpdate(DocumentEvent e){filtro.restart();}
            public void changedUpdate(DocumentEvent e){filtro.restart();}
        });
        txtBuscar.addActionListener(e->{filtro.stop();buscar();});
        txtTotales.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e){actualizarDisponibleNuevo();}
            public void removeUpdate(DocumentEvent e){actualizarDisponibleNuevo();}
            public void changedUpdate(DocumentEvent e){actualizarDisponibleNuevo();}
        });
        limpiar(); ejecutar(()->mostrar(servicio.listar()));
    }

    private JPanel lateral(){
        JPanel lado=new JPanel(); lado.setLayout(new BoxLayout(lado,BoxLayout.Y_AXIS)); lado.setBackground(FONDO);
        lado.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,0,1,BORDE),BorderFactory.createEmptyBorder(35,15,25,15)));
        lado.setPreferredSize(new Dimension(225,0));
        JLabel marca=new JLabel("Biblioteca",new Simbolo("book",38,ACENTO),SwingConstants.LEFT); marca.setFont(new Font("Serif",Font.BOLD,27)); marca.setForeground(VINO); marca.setIconTextGap(12);
        marca.setAlignmentX(LEFT_ALIGNMENT); lado.add(marca);
        JLabel subtitulo=new JLabel("Gestión de biblioteca"); subtitulo.setFont(new Font("SansSerif",Font.PLAIN,13)); subtitulo.setForeground(MUTED); subtitulo.setAlignmentX(LEFT_ALIGNMENT); subtitulo.setPreferredSize(new Dimension(195,24)); subtitulo.setMaximumSize(new Dimension(195,24));
        lado.add(Box.createVerticalStrut(9)); lado.add(subtitulo); lado.add(Box.createVerticalStrut(30));
        JSeparator linea=new JSeparator(); linea.setAlignmentX(LEFT_ALIGNMENT); linea.setForeground(BORDE); linea.setMaximumSize(new Dimension(220,1)); lado.add(linea); lado.add(Box.createVerticalStrut(24));
        JButton inicio=nav("Inicio","home",false); inicio.addActionListener(e->inicio()); lado.add(inicio); lado.add(Box.createVerticalStrut(10));
        JButton libros=nav("Libros","book",true); libros.addActionListener(e->ejecutar(()->mostrar(servicio.listar()))); lado.add(libros); lado.add(Box.createVerticalStrut(10));
        for(String nombre:new String[]{"Usuarios","Préstamos"}){
            JButton b=nav(nombre,nombre.equals("Usuarios")?"user":"books",false); b.setEnabled(false); b.setToolTipText("Módulo pendiente de integración"); lado.add(b); lado.add(Box.createVerticalStrut(10));
        }
        lado.add(Box.createVerticalGlue());
        JLabel pie=new JLabel("CATÁLOGO DE LA BIBLIOTECA"); pie.setFont(new Font("SansSerif",Font.PLAIN,10)); pie.setForeground(MUTED); pie.setAlignmentX(LEFT_ALIGNMENT); lado.add(pie);
        return lado;
    }
    private JButton nav(String texto,String icono,boolean activo){
        JButton b=boton(texto,activo); b.setIcon(new Simbolo(icono,26,activo?Color.WHITE:MUTED)); b.setHorizontalAlignment(SwingConstants.LEFT); b.setIconTextGap(20); b.setFont(new Font("SansSerif",Font.PLAIN,18));
        b.setMaximumSize(new Dimension(220,56)); b.setAlignmentX(LEFT_ALIGNMENT); return b;
    }
    private void inicio(){
        for(Window w:Window.getWindows()) if(w instanceof VentanaPrincipal && w.isDisplayable()){ w.setVisible(true);w.toFront();dispose();return; }
        new VentanaPrincipal().setVisible(true); dispose();
    }
    private JPanel catalogo(){
        JPanel centro=new JPanel(new BorderLayout(0,26)); centro.setOpaque(false);
        JPanel arriba=new JPanel(new BorderLayout(0,32)); arriba.setOpaque(false);
        JLabel titulo=new JLabel("Catálogo de libros",new Simbolo("books",44,ACENTO),SwingConstants.LEFT); titulo.setFont(TITULO); titulo.setForeground(VINO); titulo.setIconTextGap(16); arriba.add(titulo,BorderLayout.NORTH);
        JPanel herramientas=new JPanel(new BorderLayout(16,0)); herramientas.setOpaque(false);
        JPanel buscador=new Redondo(PAPEL,10); buscador.setLayout(new BorderLayout(12,0)); buscador.setBorder(BorderFactory.createEmptyBorder(8,14,8,10));
        buscador.add(new JLabel(new Simbolo("search",24,MUTED)),BorderLayout.WEST);
        txtBuscar.setFont(TEXTO); txtBuscar.setBorder(BorderFactory.createEmptyBorder(4,0,4,0)); txtBuscar.setOpaque(false); txtBuscar.setToolTipText("Buscar por título, autor o ID"); txtBuscar.getAccessibleContext().setAccessibleName("Buscar por título, autor o ID");
        // El mensaje vacío se dibuja sin convertirse en texto del campo.
        JLayer<JTextField> capa=new JLayer<>(txtBuscar,new javax.swing.plaf.LayerUI<JTextField>(){
            @Override public void paint(Graphics g,JComponent c){super.paint(g,c);if(txtBuscar.getText().isEmpty()&&!txtBuscar.hasFocus()){g.setColor(MUTED);g.setFont(TEXTO);g.drawString("Buscar por título o autor",2,c.getHeight()/2+5);}}
        });
        buscador.add(capa,BorderLayout.CENTER); herramientas.add(buscador,BorderLayout.CENTER);
        JButton nuevo=boton("Nuevo libro",true); nuevo.setIcon(new Simbolo("plus",22,Color.WHITE)); nuevo.addActionListener(e->{limpiar();tarjeta.setVisible(true);txtTitulo.requestFocusInWindow();}); herramientas.add(nuevo,BorderLayout.EAST);
        herramientas.setPreferredSize(new Dimension(0,52)); arriba.add(herramientas,BorderLayout.SOUTH); centro.add(arriba,BorderLayout.NORTH);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); tabla.setRowHeight(116); tabla.setFont(TEXTO); tabla.setForeground(TINTA); tabla.setBackground(PAPEL);
        tabla.setShowVerticalLines(false); tabla.setShowHorizontalLines(true); tabla.setGridColor(BORDE); tabla.setIntercellSpacing(new Dimension(0,1)); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false); tabla.getTableHeader().setPreferredSize(new Dimension(0,56));
        tabla.getTableHeader().setDefaultRenderer((t,v,s,f,r,c)->{
            JLabel l=new JLabel(v.toString(),c==0?SwingConstants.LEFT:SwingConstants.CENTER); l.setOpaque(true); l.setBackground(new Color(244,231,221)); l.setForeground(VINO); l.setFont(new Font("Serif",Font.BOLD,17)); l.setBorder(BorderFactory.createEmptyBorder(0,8,0,6));return l;
        });
        int[] anchos={300,85,110,115,155,126};
        for(int i=0;i<anchos.length;i++){tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);tabla.getColumnModel().getColumn(i).setMinWidth(i==0?220:anchos[i]);}
        tabla.setDefaultRenderer(Object.class,new Fila());
        tabla.getColumnModel().getColumn(5).setCellRenderer((t,v,s,f,r,c)->acciones(r,false));
        tabla.getColumnModel().getColumn(5).setCellEditor(new EditorAcciones());
        tabla.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&!actualizandoTabla&&tabla.getSelectedRow()>=0)editar(libroFila(tabla.getSelectedRow()),false);});
        JScrollPane scroll=new JScrollPane(tabla); scroll.getViewport().setBackground(PAPEL); scroll.setBorder(BorderFactory.createLineBorder(BORDE)); centro.add(scroll,BorderLayout.CENTER);
        JPanel abajo=new JPanel(new BorderLayout()); abajo.setOpaque(false); estado.setFont(new Font("SansSerif",Font.PLAIN,13));estado.setForeground(MUTED);abajo.add(estado,BorderLayout.CENTER);
        JButton todos=boton("Ver todos",false);todos.setFont(new Font("SansSerif",Font.PLAIN,13));todos.addActionListener(e->{txtBuscar.setText("");ejecutar(()->mostrar(servicio.listar()));});abajo.add(todos,BorderLayout.EAST);centro.add(abajo,BorderLayout.SOUTH);
        return centro;
    }
    private JPanel formulario(){
        tarjeta.setLayout(new BorderLayout(0,20)); tarjeta.setBorder(BorderFactory.createEmptyBorder(24,24,24,24)); tarjeta.setPreferredSize(new Dimension(340,0));
        JPanel cabeza=new JPanel(new BorderLayout());cabeza.setOpaque(false);tituloFormulario.setFont(new Font("Serif",Font.BOLD,29));tituloFormulario.setForeground(VINO);cabeza.add(tituloFormulario);
        JButton cerrar=boton("",false); cerrar.setIcon(new Simbolo("close",18,MUTED));cerrar.setToolTipText("Cerrar formulario");cerrar.addActionListener(e->{limpiar();tarjeta.setVisible(false);});cabeza.add(cerrar,BorderLayout.EAST);
        cabeza.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDE));tarjeta.add(cabeza,BorderLayout.NORTH);
        JPanel campos=new Campos();campos.setOpaque(false);campos.setLayout(new BoxLayout(campos,BoxLayout.Y_AXIS));
        txtId.setEditable(false);txtDisponibles.setEditable(false);txtId.setBackground(new Color(237,232,226));txtDisponibles.setBackground(new Color(237,232,226));
        agregarCampo(campos,"ID automático",txtId,false);agregarCampo(campos,"Título",txtTitulo,true);agregarCampo(campos,"Autor",txtAutor,true);agregarCampo(campos,"Ejemplares totales",txtTotales,true);agregarCampo(campos,"Disponibles (según préstamos)",txtDisponibles,false);
        JLabel nota=new JLabel("<html>La disponibilidad se actualiza con<br>los préstamos y las devoluciones.</html>");nota.setFont(new Font("SansSerif",Font.PLAIN,12));nota.setForeground(MUTED);nota.setAlignmentX(LEFT_ALIGNMENT);campos.add(nota);campos.add(Box.createVerticalGlue());
        JScrollPane scroll=new JScrollPane(campos);scroll.setBorder(null);scroll.setOpaque(false);scroll.getViewport().setOpaque(false);scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);tarjeta.add(scroll,BorderLayout.CENTER);
        JPanel pie=new JPanel(new GridLayout(1,2,10,0));pie.setOpaque(false);pie.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,BORDE),BorderFactory.createEmptyBorder(22,0,0,0)));
        cancelar.addActionListener(e->limpiar());guardar.addActionListener(e->guardar());pie.add(cancelar);pie.add(guardar);tarjeta.add(pie,BorderLayout.SOUTH);return tarjeta;
    }
    private void agregarCampo(JPanel panel,String texto,JTextField campo,boolean requerido){
        JPanel etiqueta=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));etiqueta.setOpaque(false);etiqueta.setAlignmentX(LEFT_ALIGNMENT);etiqueta.setMaximumSize(new Dimension(Integer.MAX_VALUE,24));
        JLabel l=new JLabel(texto);l.setFont(new Font("SansSerif",Font.BOLD,15));l.setForeground(TINTA);l.setLabelFor(campo);etiqueta.add(l);
        if(requerido){JLabel a=new JLabel(" *");a.setForeground(ACENTO);a.setFont(TEXTO);etiqueta.add(a);}
        panel.add(etiqueta);panel.add(Box.createVerticalStrut(8));campo.setAlignmentX(LEFT_ALIGNMENT);campo.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));campo.setPreferredSize(new Dimension(260,44));panel.add(campo);panel.add(Box.createVerticalStrut(22));
    }
    private static JTextField campo(){JTextField t=new JTextField();t.setFont(TEXTO);t.setForeground(TINTA);t.setBackground(PAPEL);t.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(203,192,183)),BorderFactory.createEmptyBorder(9,12,9,12)));return t;}
    private static JButton boton(String texto,boolean principal){
        JButton b=new JButton(texto){@Override protected void paintComponent(Graphics g){Graphics2D p=(Graphics2D)g.create();p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);p.setColor(principal?(getModel().isRollover()?ACENTO.darker():ACENTO):PAPEL);p.fillRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);if(!principal){p.setColor(BORDE);p.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);}p.dispose();super.paintComponent(g);}};
        b.setFont(new Font("SansSerif",Font.BOLD,14));b.setForeground(principal?Color.WHITE:VINO);b.setContentAreaFilled(false);b.setFocusPainted(false);b.setBorder(BorderFactory.createEmptyBorder(13,16,13,16));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;
    }
    private void buscar(){ejecutar(()->mostrar(servicio.buscar(txtBuscar.getText())));}
    private int leerTotal(){String t=txtTotales.getText().strip();if(!t.matches("[0-9]+"))throw new NumberFormatException();return Integer.parseInt(t);}
    private void guardar(){ejecutar(()->{
        String id=idSeleccionado;
        if(id==null) id=servicio.registrar(txtTitulo.getText(),txtAutor.getText(),leerTotal()).getId();
        else servicio.modificar(id,txtTitulo.getText(),txtAutor.getText(),leerTotal());
        mostrar(servicio.listar());limpiar();estado.setText("Libro guardado correctamente · "+id);
    });}
    private void eliminar(Libro libro){ejecutar(()->{
        if(JOptionPane.showConfirmDialog(this,"¿Eliminar «"+libro.getTitulo()+"»?","Eliminar libro",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        servicio.eliminar(libro.getId());mostrar(servicio.listar());limpiar();estado.setText("Libro eliminado.");
    });}
    private void mostrar(List<Libro> libros){actualizandoTabla=true;try{if(tabla.isEditing())tabla.getCellEditor().cancelCellEditing();tabla.clearSelection();modeloTabla.setRowCount(0);for(Libro l:libros)modeloTabla.addRow(new Object[]{l,l.getId(),l.getEjemplaresTotales(),l.getEjemplaresDisponibles(),l, l});estado.setText(libros.isEmpty()?"No hay libros que coincidan con la búsqueda.":libros.size()+" libros en el catálogo");}finally{actualizandoTabla=false;}}
    private Libro libroFila(int fila){return (Libro)modeloTabla.getValueAt(fila,0);}
    private void editar(Libro libro,boolean lectura){
        idSeleccionado=libro.getId();txtId.setText(idSeleccionado);txtTitulo.setText(libro.getTitulo());txtAutor.setText(libro.getAutor());txtTotales.setText(Integer.toString(libro.getEjemplaresTotales()));txtDisponibles.setText(Integer.toString(libro.getEjemplaresDisponibles()));
        tituloFormulario.setText(lectura?"Detalle del libro":"Editar libro");guardar.setText("Guardar cambios");guardar.setEnabled(!lectura);txtTitulo.setEditable(!lectura);txtAutor.setEditable(!lectura);txtTotales.setEditable(!lectura);tarjeta.setVisible(true);
    }
    private void limpiar(){tabla.clearSelection();idSeleccionado=null;txtId.setText("Se asignará al guardar");txtTitulo.setText("");txtAutor.setText("");txtTotales.setText("");txtDisponibles.setText("");tituloFormulario.setText("Nuevo libro");guardar.setText("Guardar libro");guardar.setEnabled(true);txtTitulo.setEditable(true);txtAutor.setEditable(true);txtTotales.setEditable(true);}
    private void actualizarDisponibleNuevo(){if(idSeleccionado==null)txtDisponibles.setText(txtTotales.getText());}
    private interface Accion{void ejecutar()throws PersistenciaException,ValidacionException;}
    private void ejecutar(Accion a){try{a.ejecutar();}catch(NumberFormatException e){error("Escribe una cantidad entera entre 0 y "+Integer.MAX_VALUE+".");}catch(PersistenciaException|ValidacionException|IllegalArgumentException e){error(e.getMessage());}}
    private void error(String m){JOptionPane.showMessageDialog(this,m,"No se pudo completar la operación",JOptionPane.ERROR_MESSAGE);}
    @Override public void dispose(){if(filtro!=null)filtro.stop();super.dispose();}

    private JPanel acciones(int fila,boolean vivo){
        JPanel p=new JPanel(new FlowLayout(FlowLayout.CENTER,5,0));p.setBackground(PAPEL);
        String[] nombres={"Editar","Eliminar","Ver detalle"}, dibujos={"pen","trash","eye"};
        for(int i=0;i<3;i++){final int accion=i;JButton b=boton("",false);b.setBorder(BorderFactory.createEmptyBorder(7,7,7,7));b.setIcon(new Simbolo(dibujos[i],17,i==1?new Color(170,62,59):MUTED));b.setToolTipText(nombres[i]);b.getAccessibleContext().setAccessibleName(nombres[i]);
            if(vivo)b.addActionListener(e->{Libro l=libroFila(fila);if(tabla.isEditing())tabla.getCellEditor().stopCellEditing();if(accion==1)eliminar(l);else editar(l,accion==2);});p.add(b);}
        JPanel centrado=new JPanel(new GridBagLayout());centrado.setBackground(PAPEL);centrado.add(p);return centrado;
    }
    private class EditorAcciones extends AbstractCellEditor implements TableCellEditor{
        public Object getCellEditorValue(){return null;}
        public Component getTableCellEditorComponent(JTable t,Object v,boolean s,int r,int c){return acciones(r,true);}
    }
    private class Fila implements TableCellRenderer{
        public Component getTableCellRendererComponent(JTable t,Object v,boolean seleccion,boolean foco,int fila,int col){
            Color fondo=seleccion?new Color(249,239,229):PAPEL;
            if(col==0){
                Libro libro=(Libro)v;
                JPanel p=new JPanel(new BorderLayout(14,0));p.setBackground(fondo);p.setBorder(BorderFactory.createEmptyBorder(13,14,13,8));p.add(new JLabel(new Portada(libro)),BorderLayout.WEST);
                JPanel textos=new JPanel(new GridLayout(2,1,0,2));textos.setOpaque(false);
                JTextArea nombre=textoFila(libro.getTitulo(),new Font("Serif",Font.BOLD,18),TINTA);
                JTextArea autor=textoFila(libro.getAutor(),new Font("SansSerif",Font.PLAIN,13),MUTED);
                textos.add(nombre);textos.add(autor);p.add(textos);return p;
            }
            if(col==4){Libro l=(Libro)v;int n=l.getEjemplaresDisponibles();String texto=n==0?"No disponible":n<=1?"Pocos disponibles":"Disponible";Color fg=n==0?new Color(173,47,42):n<=1?new Color(132,97,22):new Color(59,110,53);Color bg=n==0?new Color(252,224,218):n<=1?new Color(252,239,193):new Color(230,241,219);
                JPanel p=new JPanel(new GridBagLayout());p.setBackground(fondo);JLabel etiqueta=new JLabel("●  "+texto);etiqueta.setFont(new Font("SansSerif",Font.PLAIN,12));etiqueta.setForeground(fg);etiqueta.setPreferredSize(new Dimension(etiqueta.getPreferredSize().width+7,etiqueta.getPreferredSize().height+2));JPanel pill=new Redondo(bg,24);pill.setLayout(new BorderLayout());pill.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));pill.add(etiqueta);p.add(pill);return p;}
            JLabel l=new JLabel(String.valueOf(v),SwingConstants.CENTER);l.setOpaque(true);l.setBackground(fondo);l.setFont(TEXTO);l.setForeground(TINTA);return l;
        }
    }
    private static JTextArea textoFila(String texto,Font fuente,Color color){
        JTextArea area=new JTextArea(texto);area.setFont(fuente);area.setForeground(color);area.setOpaque(false);area.setEditable(false);area.setLineWrap(true);area.setWrapStyleWord(true);area.setBorder(null);return area;
    }
    private static class Campos extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize(){return getPreferredSize();}
        public int getScrollableUnitIncrement(Rectangle r,int o,int d){return 20;}
        public int getScrollableBlockIncrement(Rectangle r,int o,int d){return 100;}
        public boolean getScrollableTracksViewportWidth(){return true;}
        public boolean getScrollableTracksViewportHeight(){return false;}
    }
    private static class Redondo extends JPanel{
        private final Color color;private final int radio;
        Redondo(Color color,int radio){this.color=color;this.radio=radio;setOpaque(false);}
        @Override protected void paintComponent(Graphics g){Graphics2D p=(Graphics2D)g.create();p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);p.setColor(color);p.fillRoundRect(0,0,getWidth()-1,getHeight()-1,radio,radio);p.setColor(BORDE);p.drawRoundRect(0,0,getWidth()-1,getHeight()-1,radio,radio);p.dispose();super.paintComponent(g);}
    }
    /** Miniaturas decorativas generadas; el modelo aún no almacena imágenes de portada. */
    private static class Portada implements Icon{
        private final Libro libro;Portada(Libro libro){this.libro=libro;}
        public int getIconWidth(){return 57;}public int getIconHeight(){return 85;}
        public void paintIcon(Component c,Graphics g,int x,int y){Graphics2D p=(Graphics2D)g.create();p.translate(x,y);p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);Color[] colores={new Color(44,68,86),new Color(136,54,43),new Color(134,108,67),new Color(66,91,73)};p.setColor(colores[Math.floorMod(libro.getId().hashCode(),4)]);p.fillRoundRect(0,0,57,85,7,7);p.setColor(new Color(255,255,255,35));p.fillRect(4,0,3,85);p.setColor(new Color(246,226,179));p.drawRect(12,10,34,64);p.setFont(new Font("Serif",Font.BOLD,9));int linea=24;for(String palabra:libro.getTitulo().split(" ")){if(linea>53)break;p.drawString(palabra.length()>9?palabra.substring(0,8)+"…":palabra,16,linea);linea+=11;}p.drawLine(20,63,39,63);p.fillOval(27,67,5,5);p.dispose();}
    }
    private static class Simbolo implements Icon{
        private final String tipo;private final int size;private final Color color;
        Simbolo(String tipo,int size,Color color){this.tipo=tipo;this.size=size;this.color=color;}
        public int getIconWidth(){return size;}public int getIconHeight(){return size;}
        public void paintIcon(Component c,Graphics g,int x,int y){Graphics2D p=(Graphics2D)g.create();p.translate(x,y);p.scale(size/24.0,size/24.0);p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);p.setColor(c.isEnabled()?color:new Color(174,164,154));p.setStroke(new BasicStroke(1.6f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            switch(tipo){
                case "plus":p.drawLine(12,4,12,20);p.drawLine(4,12,20,12);break;
                case "close":p.drawLine(5,5,19,19);p.drawLine(19,5,5,19);break;
                case "search":p.drawOval(3,3,13,13);p.drawLine(15,15,21,21);break;
                case "home":p.drawPolyline(new int[]{2,12,22},new int[]{11,2,11},3);p.drawRect(5,10,14,11);p.drawRect(10,14,4,7);break;
                case "user":p.drawOval(8,2,8,8);p.drawArc(3,12,18,18,0,180);p.drawLine(3,21,21,21);break;
                case "trash":p.drawRect(6,7,12,14);p.drawLine(4,5,20,5);p.drawRect(9,2,6,3);p.drawLine(10,10,10,18);p.drawLine(14,10,14,18);break;
                case "pen":p.drawPolygon(new int[]{4,6,20,17,4},new int[]{20,14,2,0,20},5);p.drawLine(14,4,18,8);break;
                case "eye":p.drawOval(2,7,20,10);p.drawOval(9,9,6,6);break;
                case "books":for(int a=0;a<3;a++){int yy=3+a*6;p.drawPolygon(new int[]{2,13,22,11},new int[]{yy+4,yy,yy+4,yy+8},4);}break;
                default:Path2D b=new Path2D.Double();b.moveTo(12,5);b.curveTo(8,1,3,2,2,4);b.lineTo(2,21);b.curveTo(5,18,9,18,12,21);b.curveTo(16,18,19,18,22,21);b.lineTo(22,4);b.curveTo(19,2,15,2,12,5);b.lineTo(12,21);p.draw(b);
            }p.dispose();}
    }
    public static void main(String[] args){SwingUtilities.invokeLater(()->new LibroView().setVisible(true));}
}
