package vista;

import modelo.DiaSemana;
import modelo.Grupo;
import modelo.HorarioBorrador;
import modelo.Materia;
import persistencia.GestorPersistencia;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class VistaPrincipal extends JFrame {

    private List<Materia> materiasRegistradas;
    private List<HorarioBorrador> borradores;

    // Formulario Materias
    private JTextField txtNombreMateria;
    private JButton btnElegirColor;
    private Color colorSeleccionado = new Color(52, 152, 219);

    // Formulario Grupos
    private JComboBox<Materia> cbMateriasExistentes;
    private JTextField txtClaveGrupo, txtProfesor;
    private JComboBox<String> cbHoraInicio;
    private JCheckBox chkLunes, chkMartes, chkMiercoles, chkJueves, chkViernes;

    // Listado Catalogo
    private DefaultListModel<Grupo> modelListaGrupos;
    private JList<Grupo> listGrupos;

    // Componente de Pestañas (Tabs)
    private JTabbedPane tabbedPaneHorarios;

    public VistaPrincipal() {
        materiasRegistradas = GestorPersistencia.cargarMaterias();
        borradores = GestorPersistencia.cargarBorradores();

        setTitle("Simulador de Horarios Universitarios - Múltiples Borradores");
        setSize(1250, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                crearPanelIzquierdo(),
                crearPanelDerecho()
        );
        splitPane.setDividerLocation(430);
        add(splitPane, BorderLayout.CENTER);

        actualizarCombosYListas();
        reconstruirPestanias();
    }

    private JPanel crearPanelIzquierdo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelFormularios = new JPanel(new BorderLayout(5, 5));

        // --- SUBFORMULARIO 1: MATERIA ---
        JPanel panelMateria = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelMateria.setBorder(BorderFactory.createTitledBorder("1. Crear Nueva Materia"));

        txtNombreMateria = new JTextField(10);
        btnElegirColor = new JButton("Color");
        btnElegirColor.setBackground(colorSeleccionado);
        btnElegirColor.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Selecciona Color", colorSeleccionado);
            if (c != null) {
                colorSeleccionado = c;
                btnElegirColor.setBackground(c);
            }
        });

        JButton btnGuardarMateria = new JButton("Guardar");
        btnGuardarMateria.addActionListener(e -> crearMateria());

        panelMateria.add(new JLabel("Nombre:"));
        panelMateria.add(txtNombreMateria);
        panelMateria.add(btnElegirColor);
        panelMateria.add(btnGuardarMateria);

        // --- SUBFORMULARIO 2: GRUPOS ---
        JPanel panelGrupo = new JPanel(new GridBagLayout());
        panelGrupo.setBorder(BorderFactory.createTitledBorder("2. Añadir Grupos a Materia"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbMateriasExistentes = new JComboBox<>();
        txtClaveGrupo = new JTextField(10);
        txtProfesor = new JTextField(10);

        // Horarios disponibles de 07:00 hrs a 19:00 hrs (13 franjas)
        String[] horas = new String[13];
        for (int i = 0; i < 13; i++) horas[i] = String.format("%02d:00 hrs", i + 7);
        cbHoraInicio = new JComboBox<>(horas);

        chkLunes = new JCheckBox("L"); chkMartes = new JCheckBox("M"); chkMiercoles = new JCheckBox("X");
        chkJueves = new JCheckBox("J"); chkViernes = new JCheckBox("V");
        JPanel panelDias = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        panelDias.add(chkLunes); panelDias.add(chkMartes); panelDias.add(chkMiercoles);
        panelDias.add(chkJueves); panelDias.add(chkViernes);

        gbc.gridx = 0; gbc.gridy = 0; panelGrupo.add(new JLabel("Materia:"), gbc);
        gbc.gridx = 1; panelGrupo.add(cbMateriasExistentes, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panelGrupo.add(new JLabel("Cod. Grupo:"), gbc);
        gbc.gridx = 1; panelGrupo.add(txtClaveGrupo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panelGrupo.add(new JLabel("Profesor:"), gbc);
        gbc.gridx = 1; panelGrupo.add(txtProfesor, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panelGrupo.add(new JLabel("Hora Inicio:"), gbc);
        gbc.gridx = 1; panelGrupo.add(cbHoraInicio, gbc);

        gbc.gridx = 0; gbc.gridy = 4; panelGrupo.add(new JLabel("Días:"), gbc);
        gbc.gridx = 1; panelGrupo.add(panelDias, gbc);

        JButton btnGuardarGrupo = new JButton("+ Registrar Grupo");
        btnGuardarGrupo.addActionListener(e -> agregarGrupoAMateria());
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panelGrupo.add(btnGuardarGrupo, gbc);

        panelFormularios.add(panelMateria, BorderLayout.NORTH);
        panelFormularios.add(panelGrupo, BorderLayout.CENTER);
        panel.add(panelFormularios, BorderLayout.NORTH);

        // --- CATÁLOGO ---
        modelListaGrupos = new DefaultListModel<>();
        listGrupos = new JList<>(modelListaGrupos);
        listGrupos.setCellRenderer(new RenderizadorGrupoLista());
        panel.add(new JScrollPane(listGrupos), BorderLayout.CENTER);

        JButton btnInscribir = new JButton("► Inscribir en Borrador Activo");
        btnInscribir.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnInscribir.setBackground(new Color(220, 235, 252));
        btnInscribir.addActionListener(e -> inscribirGrupoEnBorradorSeleccionado());
        panel.add(btnInscribir, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelDerecho() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Barra Superior: Botones para gestionar pestañas
        JPanel panelTopBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNuevoBorrador = new JButton("+ Nuevo Borrador");
        btnNuevoBorrador.addActionListener(e -> crearNuevoBorrador());

        JButton btnEliminarBorrador = new JButton("Eliminar Borrador Actual");
        btnEliminarBorrador.addActionListener(e -> eliminarBorradorActual());

        JButton btnExportarJPG = new JButton("Exportar Horarios (JPG)");
        btnExportarJPG.addActionListener(e -> exportarHorariosJPG());

        panelTopBar.add(btnNuevoBorrador);
        panelTopBar.add(btnEliminarBorrador);
        panelTopBar.add(btnExportarJPG);
        panel.add(panelTopBar, BorderLayout.NORTH);

        // TABBED PANE (PESTAÑAS)
        tabbedPaneHorarios = new JTabbedPane();
        panel.add(tabbedPaneHorarios, BorderLayout.CENTER);

        return panel;
    }

    // =======================================================
    // CONSTRUCCIÓN DE CADA PESTAÑA (TAB)
    // =======================================================

    private void reconstruirPestanias() {
        tabbedPaneHorarios.removeAll();

        for (HorarioBorrador borrador : borradores) {
            JPanel panelTab = crearPanelVistaBorrador(borrador);
            tabbedPaneHorarios.addTab(borrador.getNombre(), panelTab);
        }
    }

    private JPanel crearPanelVistaBorrador(HorarioBorrador borrador) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // 1. TABLA PARRILLA (HORARIO)
        String[] colHorario = {"Hora", "LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES"};
        DefaultTableModel modelHorario = new DefaultTableModel(colHorario, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tablaHorario = new JTable(modelHorario);
        tablaHorario.setRowHeight(32);
        tablaHorario.getTableHeader().setReorderingAllowed(false);
        tablaHorario.setDefaultRenderer(Object.class, new RenderizadorColorTabla());

        // RENDERER CENTRADO EXCLUSIVAMENTE PARA LA COLUMNA 0 ("Hora")
        DefaultTableCellRenderer centradoRenderer = new DefaultTableCellRenderer();
        centradoRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tablaHorario.getColumnModel().getColumn(0).setCellRenderer(centradoRenderer);

        JScrollPane scrollHorario = new JScrollPane(tablaHorario);
        scrollHorario.setBorder(BorderFactory.createTitledBorder("Parrilla Semanal"));

        // 2. TABLA RESUMEN (PROPORCIÓN 1/5)
        String[] colResumen = {"Materia", "Grupo", "Profesor", "Horario", "Días"};
        DefaultTableModel modelResumen = new DefaultTableModel(colResumen, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tablaResumen = new JTable(modelResumen);
        tablaResumen.setRowHeight(20);
        tablaResumen.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollResumen = new JScrollPane(tablaResumen);
        scrollResumen.setBorder(BorderFactory.createTitledBorder("Resumen de Materias"));

        // Rellenar datos del borrador
        renderizarParrillaYResumen(borrador, modelHorario, modelResumen);

        // SplitPane en proporción 4/5 (0.80) vs 1/5 (0.20)
        JSplitPane splitVer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollHorario, scrollResumen);
        splitVer.setResizeWeight(0.80);
        splitVer.setDividerLocation(480);

        panel.add(splitVer, BorderLayout.CENTER);

        // Botón Vaciar para este borrador
        JButton btnVaciar = new JButton("Vaciar " + borrador.getNombre());
        btnVaciar.addActionListener(e -> {
            borrador.getGruposActivos().clear();
            GestorPersistencia.guardar(materiasRegistradas, borradores);
            renderizarParrillaYResumen(borrador, modelHorario, modelResumen);
        });
        panel.add(btnVaciar, BorderLayout.SOUTH);

        return panel;
    }

    private void renderizarParrillaYResumen(HorarioBorrador borrador, DefaultTableModel mHorario, DefaultTableModel mResumen) {
        // --- PINTAR PARRILLA (DE 07:00 A 20:00) ---
        mHorario.setRowCount(0);
        for (int h = 7; h <= 19; h++) {
            Object[] fila = new Object[6];
            fila[0] = String.format("%02d:00 - %02d:00", h, h + 1);

            for (Grupo g : borrador.getGruposActivos()) {
                if (g.getHoraInicio() == h) {
                    for (DiaSemana d : g.getDias()) {
                        if (d != DiaSemana.SABADO) {
                            fila[d.ordinal() + 1] = g;
                        }
                    }
                }
            }
            mHorario.addRow(fila);
        }

        // --- PINTAR RESUMEN (ORDENADO POR HORA ASCENDENTE) ---
        mResumen.setRowCount(0);

        List<Grupo> gruposOrdenados = new ArrayList<>(borrador.getGruposActivos());
        Collections.sort(gruposOrdenados, Comparator.comparingInt(Grupo::getHoraInicio));

        for (Grupo g : gruposOrdenados) {
            String nombreMateria = (g.getMateriaPadre() != null) ? g.getMateriaPadre().getNombre() : "N/A";
            String clave = g.getClaveGrupo();
            String profesor = g.getProfesor();
            String rangoHora = String.format("%02d:00 - %02d:00 hrs", g.getHoraInicio(), g.getHoraInicio() + 1);

            StringBuilder strDias = new StringBuilder();
            for (DiaSemana d : g.getDias()) {
                if (strDias.length() > 0) strDias.append(", ");
                strDias.append(d.name().substring(0, 3));
            }

            mResumen.addRow(new Object[]{nombreMateria, clave, profesor, rangoHora, strDias.toString()});
        }
    }

    // =======================================================
    // ACCIONES DE BORRADORES
    // =======================================================

    private void crearNuevoBorrador() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del nuevo borrador:", "Crear Borrador", JOptionPane.QUESTION_MESSAGE);
        if (nombre != null && !nombre.trim().isEmpty()) {
            HorarioBorrador nuevo = new HorarioBorrador(nombre.trim());
            borradores.add(nuevo);
            GestorPersistencia.guardar(materiasRegistradas, borradores);
            reconstruirPestanias();
            tabbedPaneHorarios.setSelectedIndex(borradores.size() - 1);
        }
    }

    private void eliminarBorradorActual() {
        if (borradores.size() <= 1) {
            JOptionPane.showMessageDialog(this, "Debes conservar al menos un borrador.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int index = tabbedPaneHorarios.getSelectedIndex();
        if (index >= 0) {
            borradores.remove(index);
            GestorPersistencia.guardar(materiasRegistradas, borradores);
            reconstruirPestanias();
        }
    }

    /**
     * Exporta el panel exactamente con la misma proporción, resolución 
     * y aspecto visual que tiene en pantalla.
     */
    private void exportarHorariosJPG() {
        if (borradores.isEmpty() || tabbedPaneHorarios.getTabCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay borradores para exportar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecciona la carpeta destino para las imágenes");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int resultado = chooser.showSaveDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) return;

        File carpetaDestino = chooser.getSelectedFile();
        int tabOriginal = tabbedPaneHorarios.getSelectedIndex();
        int exportados = 0;
        StringBuilder errores = new StringBuilder();

        for (int i = 0; i < tabbedPaneHorarios.getTabCount(); i++) {
            tabbedPaneHorarios.setSelectedIndex(i);
            tabbedPaneHorarios.validate();

            Component panelTab = tabbedPaneHorarios.getComponentAt(i);
            int ancho = panelTab.getWidth();
            int alto = panelTab.getHeight();

            if (ancho <= 0 || alto <= 0) {
                errores.append("• ").append(borradores.get(i).getNombre()).append(" (tamaño inválido)\n");
                continue;
            }

            // Crear la imagen respetando exactamente el ancho y alto del componente nativo
            BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = imagen.createGraphics();
            
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, ancho, alto);

            // "Fotografiar" el panel tal cual aparece en la UI
            panelTab.printAll(g2);
            g2.dispose();

            String nombreArchivo = sanitizarNombreArchivo(borradores.get(i).getNombre()) + ".jpg";
            File archivoDestino = new File(carpetaDestino, nombreArchivo);
            try {
                ImageIO.write(imagen, "jpg", archivoDestino);
                exportados++;
            } catch (IOException ex) {
                errores.append("• ").append(borradores.get(i).getNombre()).append(" (").append(ex.getMessage()).append(")\n");
            }
        }

        tabbedPaneHorarios.setSelectedIndex(tabOriginal);

        StringBuilder mensaje = new StringBuilder();
        mensaje.append(exportados).append(" horario(s) exportado(s) a:\n").append(carpetaDestino.getAbsolutePath());
        if (errores.length() > 0) {
            mensaje.append("\n\nNo se pudieron exportar:\n").append(errores);
        }
        JOptionPane.showMessageDialog(this, mensaje.toString(), "Exportación de horarios",
                errores.length() > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    private String sanitizarNombreArchivo(String nombre) {
        return nombre.trim().replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private void inscribirGrupoEnBorradorSeleccionado() {
        int indexTab = tabbedPaneHorarios.getSelectedIndex();
        if (indexTab < 0) return;

        Grupo seleccionado = listGrupos.getSelectedValue();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un grupo del catálogo.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        HorarioBorrador borradorActivo = borradores.get(indexTab);
        List<Grupo> activos = borradorActivo.getGruposActivos();
        List<Grupo> removidos = new ArrayList<>();

        if (activos.contains(seleccionado)) {
            JOptionPane.showMessageDialog(this, "Ese grupo ya está inscrito en este borrador.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Iterator<Grupo> iter = activos.iterator();
        while (iter.hasNext()) {
            Grupo existente = iter.next();
            boolean mismaMateria = existente.getMateriaPadre() == seleccionado.getMateriaPadre();
            if (existente.chocaCon(seleccionado) || mismaMateria) {
                removidos.add(existente);
                iter.remove();
            }
        }

        activos.add(seleccionado);
        GestorPersistencia.guardar(materiasRegistradas, borradores);

        JPanel panelTab = (JPanel) tabbedPaneHorarios.getComponentAt(indexTab);
        JSplitPane splitVer = (JSplitPane) panelTab.getComponent(0);
        JScrollPane scrollHorario = (JScrollPane) splitVer.getTopComponent();
        JScrollPane scrollResumen = (JScrollPane) splitVer.getBottomComponent();

        JTable tablaH = (JTable) scrollHorario.getViewport().getView();
        JTable tablaR = (JTable) scrollResumen.getViewport().getView();

        renderizarParrillaYResumen(borradorActivo, (DefaultTableModel) tablaH.getModel(), (DefaultTableModel) tablaR.getModel());

        if (!removidos.isEmpty()) {
            StringBuilder msg = new StringBuilder("Se reemplazaron los siguientes grupos en [" + borradorActivo.getNombre()
                    + "] (por empalme de horario o por ser la misma materia):\n");
            for (Grupo g : removidos) {
                msg.append("• ").append(g.getMateriaPadre().getNombre()).append(" [").append(g.getClaveGrupo()).append("]\n");
            }
            JOptionPane.showMessageDialog(this, msg.toString(), "Grupo Reemplazado", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void crearMateria() {
        String nombre = txtNombreMateria.getText().trim();
        if (nombre.isEmpty()) return;

        Materia m = new Materia(nombre, colorSeleccionado);
        materiasRegistradas.add(m);
        GestorPersistencia.guardar(materiasRegistradas, borradores);

        txtNombreMateria.setText("");
        actualizarCombosYListas();
    }

    private void agregarGrupoAMateria() {
        Materia materiaSeleccionada = (Materia) cbMateriasExistentes.getSelectedItem();
        if (materiaSeleccionada == null) return;

        String clave = txtClaveGrupo.getText().trim();
        String prof = txtProfesor.getText().trim();
        if (clave.isEmpty()) return;

        Set<DiaSemana> dias = EnumSet.noneOf(DiaSemana.class);
        if (chkLunes.isSelected()) dias.add(DiaSemana.LUNES);
        if (chkMartes.isSelected()) dias.add(DiaSemana.MARTES);
        if (chkMiercoles.isSelected()) dias.add(DiaSemana.MIERCOLES);
        if (chkJueves.isSelected()) dias.add(DiaSemana.JUEVES);
        if (chkViernes.isSelected()) dias.add(DiaSemana.VIERNES);

        if (dias.isEmpty()) return;

        int hInicio = cbHoraInicio.getSelectedIndex() + 7;
        Grupo nuevoGrupo = new Grupo(clave, prof, hInicio, dias);
        materiaSeleccionada.agregarGrupo(nuevoGrupo);

        GestorPersistencia.guardar(materiasRegistradas, borradores);
        actualizarCombosYListas();

        txtClaveGrupo.setText("");
        txtProfesor.setText("");
    }

    private void actualizarCombosYListas() {
        cbMateriasExistentes.removeAllItems();
        modelListaGrupos.clear();

        for (Materia m : materiasRegistradas) {
            cbMateriasExistentes.addItem(m);
            for (Grupo g : m.getGrupos()) {
                modelListaGrupos.addElement(g);
            }
        }
    }

    private Color colorSuave(Color base) {
        float factor = 0.15f;
        int r = Math.round(base.getRed() * factor + 255 * (1 - factor));
        int g = Math.round(base.getGreen() * factor + 255 * (1 - factor));
        int b = Math.round(base.getBlue() * factor + 255 * (1 - factor));
        return new Color(r, g, b);
    }

    private class RenderizadorGrupoLista extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Grupo) {
                Grupo g = (Grupo) value;
                Materia m = g.getMateriaPadre();

                setText(m != null
                        ? m.getNombre() + " [" + g.getClaveGrupo() + "] - " + g.getProfesor()
                            + " (" + g.getHoraInicio() + ":00 hrs)"
                        : g.toString());

                if (!isSelected && m != null) {
                    c.setBackground(colorSuave(m.getColor()));
                    c.setForeground(Color.DARK_GRAY);
                }
            }
            return c;
        }
    }

    private class RenderizadorColorTabla extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value instanceof Grupo) {
                Grupo g = (Grupo) value;
                c.setBackground(g.getMateriaPadre().getColor());
                c.setForeground(Color.WHITE);
                setText(g.getMateriaPadre().getNombre() + " (" + g.getClaveGrupo() + ")");
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new VistaPrincipal().setVisible(true);
        });
    }
}