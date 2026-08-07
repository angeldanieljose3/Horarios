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
    private JSpinner spinnerDificultadMateria;

    // Formulario Grupos
    private JComboBox<Materia> cbMateriasExistentes;
    private JTextField txtClaveGrupo, txtProfesor;
    private JComboBox<String> cbHoraInicio;
    private JCheckBox chkLunes, chkMartes, chkMiercoles, chkJueves, chkViernes;
    private JSpinner spinnerDificultadGrupo;

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
        JPanel panel = new FrutigerAeroUI.PanelCielo(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelFormularios = new JPanel(new BorderLayout(5, 5));
        panelFormularios.setOpaque(false);

        // --- SUBFORMULARIO 1: MATERIA ---
        JPanel panelMateria = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelMateria.setOpaque(false);
        panelMateria.setBorder(FrutigerAeroUI.bordeTitulado("1. Crear Nueva Materia"));

        txtNombreMateria = new JTextField(10);
        btnElegirColor = new JButton("Color");
        btnElegirColor.setBackground(colorSeleccionado);
        btnElegirColor.setForeground(Color.WHITE);
        btnElegirColor.setFont(FrutigerAeroUI.FUENTE_TITULO);
        btnElegirColor.setFocusPainted(false);
        btnElegirColor.setBorder(new FrutigerAeroUI.BordeBurbuja(FrutigerAeroUI.AGUA_PROFUNDA));
        btnElegirColor.setOpaque(true);
        btnElegirColor.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Selecciona Color", colorSeleccionado);
            if (c != null) {
                colorSeleccionado = c;
                btnElegirColor.setBackground(c);
            }
        });

        JButton btnGuardarMateria = new FrutigerAeroUI.BotonGlossy("Guardar", FrutigerAeroUI.VERDE_HOJA);
        btnGuardarMateria.addActionListener(e -> crearMateria());

        spinnerDificultadMateria = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));

        panelMateria.add(new JLabel("Nombre:"));
        panelMateria.add(txtNombreMateria);
        panelMateria.add(btnElegirColor);
        panelMateria.add(new JLabel("Dificultad:"));
        panelMateria.add(spinnerDificultadMateria);
        panelMateria.add(btnGuardarMateria);

        // --- SUBFORMULARIO 2: GRUPOS ---
        JPanel panelGrupo = new JPanel(new GridBagLayout());
        panelGrupo.setOpaque(false);
        panelGrupo.setBorder(FrutigerAeroUI.bordeTitulado("2. Añadir Grupos a Materia"));
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

        spinnerDificultadGrupo = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        gbc.gridx = 0; gbc.gridy = 5; panelGrupo.add(new JLabel("Dificultad (1-5):"), gbc);
        gbc.gridx = 1; panelGrupo.add(spinnerDificultadGrupo, gbc);

        JButton btnGuardarGrupo = new FrutigerAeroUI.BotonGlossy("+ Registrar Grupo", FrutigerAeroUI.VERDE_HOJA);
        btnGuardarGrupo.addActionListener(e -> agregarGrupoAMateria());
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        panelGrupo.add(btnGuardarGrupo, gbc);

        panelFormularios.add(panelMateria, BorderLayout.NORTH);
        panelFormularios.add(panelGrupo, BorderLayout.CENTER);
        panel.add(panelFormularios, BorderLayout.NORTH);

        // --- CATÁLOGO ---
        modelListaGrupos = new DefaultListModel<>();
        listGrupos = new JList<>(modelListaGrupos);
        listGrupos.setCellRenderer(new RenderizadorGrupoLista());
        panel.add(new JScrollPane(listGrupos), BorderLayout.CENTER);

        JButton btnToggleDisponible = new FrutigerAeroUI.BotonGlossy("⛔ Marcar/Desmarcar como Lleno", FrutigerAeroUI.NARANJA_SOL);
        btnToggleDisponible.addActionListener(e -> toggleDisponibilidadGrupoSeleccionado());

        JButton btnInscribir = new FrutigerAeroUI.BotonGlossy("► Inscribir en Borrador Activo", FrutigerAeroUI.AGUA_PROFUNDA);
        btnInscribir.addActionListener(e -> inscribirGrupoEnBorradorSeleccionado());

        JPanel panelBotonesCatalogo = new JPanel(new GridLayout(2, 1, 0, 4));
        panelBotonesCatalogo.setOpaque(false);
        panelBotonesCatalogo.add(btnToggleDisponible);
        panelBotonesCatalogo.add(btnInscribir);
        panel.add(panelBotonesCatalogo, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelDerecho() {
        JPanel panel = new FrutigerAeroUI.PanelCielo(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Barra Superior: Botones para gestionar pestañas
        JPanel panelTopBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTopBar.setOpaque(false);

        JButton btnNuevoBorrador = new FrutigerAeroUI.BotonGlossy("+ Nuevo Borrador", FrutigerAeroUI.AGUA_PROFUNDA);
        btnNuevoBorrador.addActionListener(e -> crearNuevoBorrador());

        JButton btnEliminarBorrador = new FrutigerAeroUI.BotonGlossy("Eliminar Borrador Actual", FrutigerAeroUI.ROJO_CORAL);
        btnEliminarBorrador.addActionListener(e -> eliminarBorradorActual());

        JButton btnExportarJPG = new FrutigerAeroUI.BotonGlossy("Exportar Horarios (JPG)", FrutigerAeroUI.CIELO_MEDIO.darker());
        btnExportarJPG.addActionListener(e -> exportarHorariosJPG());

        JButton btnModoRapido = new FrutigerAeroUI.BotonGlossy("⚡ Modo Rápido", FrutigerAeroUI.NARANJA_SOL);
        btnModoRapido.addActionListener(e -> mostrarDialogoModoRapido());

        JButton btnHorarioOptimo = new FrutigerAeroUI.BotonGlossy("🏆 Horario Óptimo", FrutigerAeroUI.VERDE_HOJA);
        btnHorarioOptimo.addActionListener(e -> mostrarDialogoHorarioOptimo());

        panelTopBar.add(btnNuevoBorrador);
        panelTopBar.add(btnEliminarBorrador);
        panelTopBar.add(btnExportarJPG);
        panelTopBar.add(btnModoRapido);
        panelTopBar.add(btnHorarioOptimo);
        panel.add(panelTopBar, BorderLayout.NORTH);

        // TABBED PANE (PESTAÑAS)
        tabbedPaneHorarios = new JTabbedPane();
        FrutigerAeroUI.estilizarTabbedPane(tabbedPaneHorarios);
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
        JPanel panel = new FrutigerAeroUI.PanelCielo(new BorderLayout(5, 5));

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

        FrutigerAeroUI.estilizarHeaderTabla(tablaHorario);

        JScrollPane scrollHorario = new JScrollPane(tablaHorario);
        scrollHorario.setBorder(FrutigerAeroUI.bordeTitulado("Parrilla Semanal"));

        // 2. TABLA RESUMEN (PROPORCIÓN 1/5)
        String[] colResumen = {"Materia", "Grupo", "Profesor", "Horario", "Días"};
        DefaultTableModel modelResumen = new DefaultTableModel(colResumen, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tablaResumen = new JTable(modelResumen);
        tablaResumen.setRowHeight(20);
        tablaResumen.getTableHeader().setReorderingAllowed(false);

        FrutigerAeroUI.estilizarHeaderTabla(tablaResumen);

        JScrollPane scrollResumen = new JScrollPane(tablaResumen);
        scrollResumen.setBorder(FrutigerAeroUI.bordeTitulado("Resumen de Materias"));

        // Rellenar datos del borrador
        renderizarParrillaYResumen(borrador, modelHorario, modelResumen);

        // SplitPane en proporción 4/5 (0.80) vs 1/5 (0.20)
        JSplitPane splitVer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollHorario, scrollResumen);
        splitVer.setResizeWeight(0.80);
        splitVer.setDividerLocation(480);

        panel.add(splitVer, BorderLayout.CENTER);

        // Botón Vaciar para este borrador
        JButton btnVaciar = new FrutigerAeroUI.BotonGlossy("Vaciar " + borrador.getNombre(), FrutigerAeroUI.ROJO_CORAL);
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

        if (!seleccionado.isDisponible()) {
            JOptionPane.showMessageDialog(this, "Este grupo está marcado como LLENO y no se puede inscribir.",
                    "Cupo agotado", JOptionPane.WARNING_MESSAGE);
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

    private void toggleDisponibilidadGrupoSeleccionado() {
        Grupo seleccionado = listGrupos.getSelectedValue();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un grupo del catálogo.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        seleccionado.setDisponible(!seleccionado.isDisponible());
        GestorPersistencia.guardar(materiasRegistradas, borradores);

        // Refrescar visualmente el JList sin reconstruir todo el modelo
        int index = listGrupos.getSelectedIndex();
        listGrupos.repaint();

        String estado = seleccionado.isDisponible() ? "DISPONIBLE" : "LLENO";
        JOptionPane.showMessageDialog(this,
                seleccionado.getMateriaPadre().getNombre() + " [" + seleccionado.getClaveGrupo() + "] ahora está: " + estado,
                "Disponibilidad actualizada", JOptionPane.INFORMATION_MESSAGE);
    }

    // =======================================================
    // MODO RÁPIDO
    // =======================================================

    private static final int MAX_MATERIAS_SELECCIONABLES = 8;
    private static final int MAX_HORARIOS_GENERABLES = 5;
    private static final int MAX_HORAS_LIBRES_PERMITIDAS = 5;
    private static final int LIMITE_EXPLORACION_OPTIMO = 200_000;

    private enum FuenteDificultad { GRUPO, MATERIA, AMBAS }
    private enum EstrategiaBalance { HUECOS_PRIMERO, DIFICULTAD_PRIMERO, INTERMEDIO }

    /** Combinación de grupos ya evaluada: costo en huecos y en dificultad. */
    private static class CombinacionEvaluada {
        List<Grupo> grupos;
        int huecos;
        int dificultad;
    }

    private void mostrarDialogoModoRapido() {
        if (materiasRegistradas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero registra materias y grupos en el catálogo.",
                    "Sin materias", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialogo = new JDialog(this, "Modo Rápido - Generar Horario(s)", true);
        FrutigerAeroUI.PanelCielo fondoDialogo = new FrutigerAeroUI.PanelCielo(new BorderLayout(10, 10));
        fondoDialogo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialogo.setContentPane(fondoDialogo);
        dialogo.setLayout(new BorderLayout(10, 10));

        // --- Selección de materias tentativas ---
        JPanel panelMaterias = new JPanel();
        panelMaterias.setOpaque(false);
        panelMaterias.setLayout(new BoxLayout(panelMaterias, BoxLayout.Y_AXIS));
        JLabel lblContador = new JLabel("Seleccionadas: 0 / " + MAX_MATERIAS_SELECCIONABLES);

        List<JCheckBox> checkboxes = new ArrayList<>();
        for (Materia m : materiasRegistradas) {
            JCheckBox chk = new JCheckBox(m.getNombre() + "  (" + m.getGrupos().size() + " grupos)");
            chk.setOpaque(false);
            chk.putClientProperty("materia", m);
            chk.addItemListener(e -> {
                long seleccionadas = checkboxes.stream().filter(JCheckBox::isSelected).count();
                if (seleccionadas > MAX_MATERIAS_SELECCIONABLES) {
                    chk.setSelected(false);
                    JOptionPane.showMessageDialog(dialogo, "Máximo " + MAX_MATERIAS_SELECCIONABLES + " materias tentativas.",
                            "Límite alcanzado", JOptionPane.WARNING_MESSAGE);
                } else {
                    lblContador.setText("Seleccionadas: " + checkboxes.stream().filter(JCheckBox::isSelected).count()
                            + " / " + MAX_MATERIAS_SELECCIONABLES);
                }
            });
            checkboxes.add(chk);
            panelMaterias.add(chk);
        }

        JScrollPane scrollMaterias = new JScrollPane(panelMaterias);
        scrollMaterias.getViewport().setOpaque(false);
        scrollMaterias.setOpaque(false);
        scrollMaterias.setPreferredSize(new Dimension(360, 220));
        scrollMaterias.setBorder(FrutigerAeroUI.bordeTitulado("1. ¿Qué materias piensas cursar? (máx. " + MAX_MATERIAS_SELECCIONABLES + ")"));

        JPanel panelSeleccion = new JPanel(new BorderLayout());
        panelSeleccion.setOpaque(false);
        panelSeleccion.add(scrollMaterias, BorderLayout.CENTER);
        panelSeleccion.add(lblContador, BorderLayout.SOUTH);

        // --- Cantidad de horarios a generar ---
        JPanel panelCantidad = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCantidad.setOpaque(false);
        panelCantidad.setBorder(FrutigerAeroUI.bordeTitulado("2. ¿Cuántos horarios distintos quieres generar?"));
        JSpinner spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, MAX_HORARIOS_GENERABLES, 1));
        panelCantidad.add(new JLabel("Cantidad (máx. " + MAX_HORARIOS_GENERABLES + "):"));
        panelCantidad.add(spinnerCantidad);

        // --- Horas libres máximas permitidas por día ---
        JPanel panelHuecos = new JPanel(new BorderLayout(5, 0));
        panelHuecos.setOpaque(false);
        panelHuecos.setBorder(FrutigerAeroUI.bordeTitulado("3. ¿Cuántas horas libres al día toleras como máximo?"));
        JSlider sliderHuecos = new JSlider(JSlider.HORIZONTAL, 0, MAX_HORAS_LIBRES_PERMITIDAS, 1);
        sliderHuecos.setOpaque(false);
        sliderHuecos.setMajorTickSpacing(1);
        sliderHuecos.setPaintTicks(true);
        sliderHuecos.setPaintLabels(true);
        sliderHuecos.setSnapToTicks(true);
        JLabel lblHuecos = new JLabel("Horas libres máximas: 1", SwingConstants.CENTER);
        sliderHuecos.addChangeListener(e -> lblHuecos.setText("Horas libres máximas: " + sliderHuecos.getValue()));
        panelHuecos.add(lblHuecos, BorderLayout.NORTH);
        panelHuecos.add(sliderHuecos, BorderLayout.CENTER);

        // --- Destino ---
        JPanel panelDestino = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelDestino.setOpaque(false);
        panelDestino.setBorder(FrutigerAeroUI.bordeTitulado("4. ¿Dónde quieres el/los resultado(s)?"));
        JRadioButton radioNuevo = new JRadioButton("Crear borrador(es) nuevo(s)", true);
        JRadioButton radioActual = new JRadioButton("Llenar el borrador actualmente abierto (solo 1 horario)");
        radioNuevo.setOpaque(false);
        radioActual.setOpaque(false);
        ButtonGroup grupoDestino = new ButtonGroup();
        grupoDestino.add(radioNuevo);
        grupoDestino.add(radioActual);
        panelDestino.add(radioNuevo);
        panelDestino.add(radioActual);

        JPanel panelCentro = new JPanel();
        panelCentro.setOpaque(false);
        panelCentro.setLayout(new BoxLayout(panelCentro, BoxLayout.Y_AXIS));
        panelCentro.add(panelCantidad);
        panelCentro.add(panelHuecos);
        panelCentro.add(panelDestino);

        dialogo.add(panelSeleccion, BorderLayout.CENTER);

        JButton btnGenerar = new FrutigerAeroUI.BotonGlossy("Generar Horario(s)", FrutigerAeroUI.VERDE_HOJA);
        JButton btnCancelar = new FrutigerAeroUI.BotonGlossy("Cancelar", FrutigerAeroUI.ROJO_CORAL);
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        panelBotones.add(btnCancelar);
        panelBotones.add(btnGenerar);

        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.setOpaque(false);
        panelSur.add(panelCentro, BorderLayout.NORTH);
        panelSur.add(panelBotones, BorderLayout.SOUTH);
        dialogo.add(panelSur, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dialogo.dispose());
        btnGenerar.addActionListener(e -> {
            List<Materia> seleccionadas = new ArrayList<>();
            for (JCheckBox chk : checkboxes) {
                if (chk.isSelected()) seleccionadas.add((Materia) chk.getClientProperty("materia"));
            }
            if (seleccionadas.isEmpty()) {
                JOptionPane.showMessageDialog(dialogo, "Selecciona al menos una materia.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int cantidad = (Integer) spinnerCantidad.getValue();
            boolean usarActual = radioActual.isSelected();
            if (usarActual) cantidad = 1; // solo cabe un horario en el borrador actual
            int limiteHuecos = sliderHuecos.getValue();

            dialogo.dispose();
            ejecutarModoRapido(seleccionadas, cantidad, usarActual, limiteHuecos);
        });

        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    private void ejecutarModoRapido(List<Materia> materiasSeleccionadas, int cantidad, boolean usarBorradorActual, int limiteHuecos) {
        List<String> materiasSinGruposDisponibles = new ArrayList<>();
        List<Materia> materiasConGrupos = new ArrayList<>();

        for (Materia m : materiasSeleccionadas) {
            boolean tieneDisponible = false;
            for (Grupo g : m.getGrupos()) {
                if (g.isDisponible()) { tieneDisponible = true; break; }
            }
            if (tieneDisponible) materiasConGrupos.add(m);
            else materiasSinGruposDisponibles.add(m.getNombre());
        }

        if (materiasConGrupos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ninguna de las materias seleccionadas tiene grupos disponibles.",
                    "No se pudo generar", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<List<Grupo>> combinaciones = generarCombinacionesRapidas(materiasConGrupos, cantidad, limiteHuecos);

        if (combinaciones.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No se encontró ningún horario sin choques con un máximo de " + limiteHuecos + " hora(s) libre(s) al día.\n" +
                    "Sugerencia: sube el control de 'horas libres máximas' e inténtalo de nuevo, o revisa si hay grupos " +
                    "marcados como llenos que podrías liberar.",
                    "Sin resultados con este límite", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (usarBorradorActual) {
            int indexTab = tabbedPaneHorarios.getSelectedIndex();
            if (indexTab < 0) {
                JOptionPane.showMessageDialog(this, "No hay ningún borrador abierto.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            HorarioBorrador borradorActivo = borradores.get(indexTab);
            borradorActivo.getGruposActivos().clear();
            borradorActivo.getGruposActivos().addAll(combinaciones.get(0));
            GestorPersistencia.guardar(materiasRegistradas, borradores);
            reconstruirPestanias();
            tabbedPaneHorarios.setSelectedIndex(indexTab);
        } else {
            int primerIndiceNuevo = borradores.size();
            for (int i = 0; i < combinaciones.size(); i++) {
                String nombre = nombreBorradorDisponible("Rápido", i + 1);
                HorarioBorrador nuevo = new HorarioBorrador(nombre);
                nuevo.getGruposActivos().addAll(combinaciones.get(i));
                borradores.add(nuevo);
            }
            GestorPersistencia.guardar(materiasRegistradas, borradores);
            reconstruirPestanias();
            tabbedPaneHorarios.setSelectedIndex(primerIndiceNuevo);
        }

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Se generaron ").append(combinaciones.size()).append(" horario(s) correctamente.");
        if (!materiasSinGruposDisponibles.isEmpty()) {
            mensaje.append("\n\nNo se pudieron incluir estas materias (sin grupos disponibles):\n");
            for (String nombre : materiasSinGruposDisponibles) mensaje.append("• ").append(nombre).append("\n");
        }
        JOptionPane.showMessageDialog(this, mensaje.toString(), "Modo Rápido",
                materiasSinGruposDisponibles.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    private String nombreBorradorDisponible(String prefijo, int numeroBase) {
        Set<String> nombresExistentes = new HashSet<>();
        for (HorarioBorrador b : borradores) nombresExistentes.add(b.getNombre());

        int n = numeroBase;
        String candidato;
        do {
            candidato = prefijo + " " + n;
            n++;
        } while (nombresExistentes.contains(candidato));
        return candidato;
    }

    /**
     * Genera hasta 'cantidadDeseada' combinaciones distintas de un grupo por materia,
     * sin choques de horario entre ellos y con un total de horas muertas por día
     * MENOR O IGUAL al límite indicado por el usuario. Usa orden aleatorizado en
     * cada intento para obtener variedad (modo rápido: no pondera dificultad de
     * maestro/materia, solo huecos y disponibilidad).
     */
    private List<List<Grupo>> generarCombinacionesRapidas(List<Materia> materias, int cantidadDeseada, int limiteHuecos) {
        List<List<Grupo>> resultados = new ArrayList<>();
        Set<String> firmasVistas = new HashSet<>();
        Random rnd = new Random();

        int intentosMax = 500; // límite de seguridad para no ciclar indefinidamente
        for (int intento = 0; intento < intentosMax && resultados.size() < cantidadDeseada; intento++) {
            List<Grupo> combinacion = new ArrayList<>();
            boolean encontrada = backtrackCombinacion(materias, 0, combinacion, rnd, limiteHuecos);
            if (encontrada) {
                String firma = firmaDeCombinacion(combinacion);
                if (firmasVistas.add(firma)) {
                    resultados.add(new ArrayList<>(combinacion));
                }
            }
        }
        return resultados;
    }

    private boolean backtrackCombinacion(List<Materia> materias, int indice, List<Grupo> actual, Random rnd, int limiteHuecos) {
        if (indice == materias.size()) {
            // Combinación completa: solo es válida si el total de horas muertas no rebasa el límite
            return calcularHorasMuertas(actual) <= limiteHuecos;
        }

        List<Grupo> candidatos = new ArrayList<>();
        for (Grupo g : materias.get(indice).getGrupos()) {
            if (g.isDisponible()) candidatos.add(g);
        }
        Collections.shuffle(candidatos, rnd);

        for (Grupo g : candidatos) {
            boolean choca = false;
            for (Grupo existente : actual) {
                if (existente.chocaCon(g)) { choca = true; break; }
            }
            if (!choca) {
                actual.add(g);
                if (backtrackCombinacion(materias, indice + 1, actual, rnd, limiteHuecos)) return true;
                actual.remove(actual.size() - 1);
            }
        }
        return false;
    }

    /**
     * Calcula el total de horas muertas (celdas vacías entre la primera y la
     * última clase) sumadas de todos los días de la combinación. El Sábado se
     * ignora porque la parrilla del horario no lo utiliza.
     */
    private int calcularHorasMuertas(List<Grupo> combinacion) {
        Map<DiaSemana, TreeSet<Integer>> horasPorDia = new EnumMap<>(DiaSemana.class);

        for (Grupo g : combinacion) {
            for (DiaSemana d : g.getDias()) {
                if (d == DiaSemana.SABADO) continue;
                horasPorDia.computeIfAbsent(d, k -> new TreeSet<>()).add(g.getHoraInicio());
            }
        }

        int totalHuecos = 0;
        for (TreeSet<Integer> horas : horasPorDia.values()) {
            if (horas.isEmpty()) continue;
            int minHora = horas.first();
            int maxHora = horas.last();
            int rangoEsperado = maxHora - minHora + 1;
            totalHuecos += (rangoEsperado - horas.size());
        }
        return totalHuecos;
    }

    private String firmaDeCombinacion(List<Grupo> combinacion) {
        List<String> claves = new ArrayList<>();
        for (Grupo g : combinacion) {
            claves.add(g.getMateriaPadre().getNombre() + "#" + g.getClaveGrupo());
        }
        Collections.sort(claves);
        return String.join("|", claves);
    }

    // =======================================================
    // HORARIO ÓPTIMO
    // =======================================================

    private void mostrarDialogoHorarioOptimo() {
        if (materiasRegistradas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero registra materias y grupos en el catálogo.",
                    "Sin materias", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialogo = new JDialog(this, "Horario Óptimo - Generar Mejores Opciones", true);
        FrutigerAeroUI.PanelCielo fondoDialogo = new FrutigerAeroUI.PanelCielo(new BorderLayout(10, 10));
        fondoDialogo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialogo.setContentPane(fondoDialogo);
        dialogo.setLayout(new BorderLayout(10, 10));

        // --- Selección de materias tentativas ---
        JPanel panelMaterias = new JPanel();
        panelMaterias.setOpaque(false);
        panelMaterias.setLayout(new BoxLayout(panelMaterias, BoxLayout.Y_AXIS));
        JLabel lblContador = new JLabel("Seleccionadas: 0 / " + MAX_MATERIAS_SELECCIONABLES);

        List<JCheckBox> checkboxes = new ArrayList<>();
        for (Materia m : materiasRegistradas) {
            JCheckBox chk = new JCheckBox(m.getNombre() + "  (" + m.getGrupos().size() + " grupos, dif. materia: " + m.getDificultad() + ")");
            chk.setOpaque(false);
            chk.putClientProperty("materia", m);
            chk.addItemListener(e -> {
                long seleccionadas = checkboxes.stream().filter(JCheckBox::isSelected).count();
                if (seleccionadas > MAX_MATERIAS_SELECCIONABLES) {
                    chk.setSelected(false);
                    JOptionPane.showMessageDialog(dialogo, "Máximo " + MAX_MATERIAS_SELECCIONABLES + " materias tentativas.",
                            "Límite alcanzado", JOptionPane.WARNING_MESSAGE);
                } else {
                    lblContador.setText("Seleccionadas: " + checkboxes.stream().filter(JCheckBox::isSelected).count()
                            + " / " + MAX_MATERIAS_SELECCIONABLES);
                }
            });
            checkboxes.add(chk);
            panelMaterias.add(chk);
        }

        JScrollPane scrollMaterias = new JScrollPane(panelMaterias);
        scrollMaterias.setPreferredSize(new Dimension(400, 200));
        scrollMaterias.getViewport().setOpaque(false);
        scrollMaterias.setOpaque(false);
        scrollMaterias.setBorder(FrutigerAeroUI.bordeTitulado("1. ¿Qué materias piensas cursar? (máx. " + MAX_MATERIAS_SELECCIONABLES + ")"));

        JPanel panelSeleccion = new JPanel(new BorderLayout());
        panelSeleccion.setOpaque(false);
        panelSeleccion.add(scrollMaterias, BorderLayout.CENTER);
        panelSeleccion.add(lblContador, BorderLayout.SOUTH);

        // --- Cantidad de horarios a mostrar ---
        JPanel panelCantidad = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCantidad.setOpaque(false);
        panelCantidad.setBorder(FrutigerAeroUI.bordeTitulado("2. ¿Cuántas de las mejores opciones quieres ver?"));
        JSpinner spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, MAX_HORARIOS_GENERABLES, 1));
        panelCantidad.add(new JLabel("Cantidad (máx. " + MAX_HORARIOS_GENERABLES + "):"));
        panelCantidad.add(spinnerCantidad);

        // --- Fuente de dificultad ---
        JPanel panelFuente = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFuente.setOpaque(false);
        panelFuente.setBorder(FrutigerAeroUI.bordeTitulado("3. ¿Dónde se toma la dificultad?"));
        JRadioButton radioFuenteGrupo = new JRadioButton("Por grupo/profesor", true);
        JRadioButton radioFuenteMateria = new JRadioButton("Por materia");
        JRadioButton radioFuenteAmbas = new JRadioButton("Ambas (se suman)");
        radioFuenteGrupo.setOpaque(false);
        radioFuenteMateria.setOpaque(false);
        radioFuenteAmbas.setOpaque(false);
        ButtonGroup grupoFuente = new ButtonGroup();
        grupoFuente.add(radioFuenteGrupo);
        grupoFuente.add(radioFuenteMateria);
        grupoFuente.add(radioFuenteAmbas);
        panelFuente.add(radioFuenteGrupo);
        panelFuente.add(radioFuenteMateria);
        panelFuente.add(radioFuenteAmbas);

        // --- Estrategia de balance ---
        JPanel panelEstrategia = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEstrategia.setOpaque(false);
        panelEstrategia.setBorder(FrutigerAeroUI.bordeTitulado("4. ¿Cómo se balancean huecos vs. dificultad?"));
        JRadioButton radioHuecosPrimero = new JRadioButton("Priorizar menos huecos", true);
        JRadioButton radioDificultadPrimero = new JRadioButton("Priorizar menos dificultad");
        JRadioButton radioIntermedio = new JRadioButton("Punto intermedio (ambos pesan igual)");
        radioHuecosPrimero.setOpaque(false);
        radioDificultadPrimero.setOpaque(false);
        radioIntermedio.setOpaque(false);
        ButtonGroup grupoEstrategia = new ButtonGroup();
        grupoEstrategia.add(radioHuecosPrimero);
        grupoEstrategia.add(radioDificultadPrimero);
        grupoEstrategia.add(radioIntermedio);
        panelEstrategia.add(radioHuecosPrimero);
        panelEstrategia.add(radioDificultadPrimero);
        panelEstrategia.add(radioIntermedio);

        // --- Límite opcional de horas libres máximas por día ---
        JPanel panelHuecos = new JPanel(new BorderLayout(5, 0));
        panelHuecos.setOpaque(false);
        panelHuecos.setBorder(FrutigerAeroUI.bordeTitulado("5. Límite de horas libres (opcional)"));

        JCheckBox chkLimitarHuecos = new JCheckBox("Limitar horas libres máximas al día");
        chkLimitarHuecos.setOpaque(false);
        JSlider sliderHuecosOptimo = new JSlider(JSlider.HORIZONTAL, 0, MAX_HORAS_LIBRES_PERMITIDAS, 2);
        sliderHuecosOptimo.setOpaque(false);
        sliderHuecosOptimo.setMajorTickSpacing(1);
        sliderHuecosOptimo.setPaintTicks(true);
        sliderHuecosOptimo.setPaintLabels(true);
        sliderHuecosOptimo.setSnapToTicks(true);
        sliderHuecosOptimo.setEnabled(false);

        JLabel lblHuecosOptimo = new JLabel("Horas libres máximas: 2", SwingConstants.CENTER);
        sliderHuecosOptimo.addChangeListener(e -> lblHuecosOptimo.setText("Horas libres máximas: " + sliderHuecosOptimo.getValue()));

        chkLimitarHuecos.addItemListener(e -> {
            boolean activo = chkLimitarHuecos.isSelected();
            sliderHuecosOptimo.setEnabled(activo);
            lblHuecosOptimo.setEnabled(activo);
        });
        lblHuecosOptimo.setEnabled(false);

        JPanel panelSliderHuecos = new JPanel(new BorderLayout());
        panelSliderHuecos.setOpaque(false);
        panelSliderHuecos.add(lblHuecosOptimo, BorderLayout.NORTH);
        panelSliderHuecos.add(sliderHuecosOptimo, BorderLayout.CENTER);

        panelHuecos.add(chkLimitarHuecos, BorderLayout.NORTH);
        panelHuecos.add(panelSliderHuecos, BorderLayout.CENTER);

        // --- Destino ---
        JPanel panelDestino = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelDestino.setOpaque(false);
        panelDestino.setBorder(FrutigerAeroUI.bordeTitulado("6. ¿Dónde quieres el/los resultado(s)?"));
        JRadioButton radioNuevo = new JRadioButton("Crear borrador(es) nuevo(s)", true);
        JRadioButton radioActual = new JRadioButton("Llenar el borrador actualmente abierto (solo el mejor)");
        radioNuevo.setOpaque(false);
        radioActual.setOpaque(false);
        ButtonGroup grupoDestino = new ButtonGroup();
        grupoDestino.add(radioNuevo);
        grupoDestino.add(radioActual);
        panelDestino.add(radioNuevo);
        panelDestino.add(radioActual);

        JPanel panelCentro = new JPanel();
        panelCentro.setOpaque(false);
        panelCentro.setLayout(new BoxLayout(panelCentro, BoxLayout.Y_AXIS));
        panelCentro.add(panelCantidad);
        panelCentro.add(panelFuente);
        panelCentro.add(panelEstrategia);
        panelCentro.add(panelHuecos);
        panelCentro.add(panelDestino);

        dialogo.add(panelSeleccion, BorderLayout.CENTER);

        JButton btnGenerar = new FrutigerAeroUI.BotonGlossy("Calcular Mejor(es) Horario(s)", FrutigerAeroUI.VERDE_HOJA);
        JButton btnCancelar = new FrutigerAeroUI.BotonGlossy("Cancelar", FrutigerAeroUI.ROJO_CORAL);
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        panelBotones.add(btnCancelar);
        panelBotones.add(btnGenerar);

        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.setOpaque(false);
        panelSur.add(panelCentro, BorderLayout.NORTH);
        panelSur.add(panelBotones, BorderLayout.SOUTH);
        dialogo.add(panelSur, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dialogo.dispose());
        btnGenerar.addActionListener(e -> {
            List<Materia> seleccionadas = new ArrayList<>();
            for (JCheckBox chk : checkboxes) {
                if (chk.isSelected()) seleccionadas.add((Materia) chk.getClientProperty("materia"));
            }
            if (seleccionadas.isEmpty()) {
                JOptionPane.showMessageDialog(dialogo, "Selecciona al menos una materia.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int cantidad = (Integer) spinnerCantidad.getValue();
            boolean usarActual = radioActual.isSelected();
            if (usarActual) cantidad = 1; // solo cabe un horario en el borrador actual

            FuenteDificultad fuente = radioFuenteMateria.isSelected() ? FuenteDificultad.MATERIA
                    : radioFuenteAmbas.isSelected() ? FuenteDificultad.AMBAS
                    : FuenteDificultad.GRUPO;

            EstrategiaBalance estrategia = radioDificultadPrimero.isSelected() ? EstrategiaBalance.DIFICULTAD_PRIMERO
                    : radioIntermedio.isSelected() ? EstrategiaBalance.INTERMEDIO
                    : EstrategiaBalance.HUECOS_PRIMERO;

            boolean limitarHuecos = chkLimitarHuecos.isSelected();
            int limiteHuecos = sliderHuecosOptimo.getValue();

            dialogo.dispose();
            ejecutarHorarioOptimo(seleccionadas, cantidad, usarActual, fuente, estrategia, limitarHuecos, limiteHuecos);
        });

        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    private void ejecutarHorarioOptimo(List<Materia> materiasSeleccionadas, int cantidad, boolean usarBorradorActual,
                                        FuenteDificultad fuente, EstrategiaBalance estrategia,
                                        boolean limitarHuecos, int limiteHuecos) {
        List<String> materiasSinGruposDisponibles = new ArrayList<>();
        List<Materia> materiasConGrupos = new ArrayList<>();

        for (Materia m : materiasSeleccionadas) {
            boolean tieneDisponible = false;
            for (Grupo g : m.getGrupos()) {
                if (g.isDisponible()) { tieneDisponible = true; break; }
            }
            if (tieneDisponible) materiasConGrupos.add(m);
            else materiasSinGruposDisponibles.add(m.getNombre());
        }

        if (materiasConGrupos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ninguna de las materias seleccionadas tiene grupos disponibles.",
                    "No se pudo generar", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<List<Grupo>> todasLasCombinaciones = new ArrayList<>();
        int[] contadorExploracion = {0};
        enumerarCombinacionesValidas(materiasConGrupos, 0, new ArrayList<>(), todasLasCombinaciones, contadorExploracion);

        if (todasLasCombinaciones.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No fue posible encontrar ningún horario sin choques con las materias seleccionadas.\n" +
                    "Prueba quitando alguna materia o liberando algún grupo marcado como lleno.",
                    "No se pudo generar", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<CombinacionEvaluada> evaluadas = new ArrayList<>();
        for (List<Grupo> combo : todasLasCombinaciones) {
            CombinacionEvaluada ce = new CombinacionEvaluada();
            ce.grupos = combo;
            ce.huecos = calcularHorasMuertas(combo);
            ce.dificultad = calcularDificultadTotal(combo, fuente);
            evaluadas.add(ce);
        }

        // Si el usuario activó el límite de horas libres, se descartan (filtran) de raíz
        // las combinaciones que lo superen, ANTES de ordenar y elegir las mejores.
        if (limitarHuecos) {
            List<CombinacionEvaluada> dentroDelLimite = new ArrayList<>();
            for (CombinacionEvaluada ce : evaluadas) {
                if (ce.huecos <= limiteHuecos) dentroDelLimite.add(ce);
            }
            if (dentroDelLimite.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Se calcularon " + todasLasCombinaciones.size() + " horario(s) posible(s), pero ninguno respeta "
                        + "el límite de " + limiteHuecos + " hora(s) libre(s) al día.\n"
                        + "Sugerencia: sube el control deslizante, desactiva la casilla de límite, o revisa si hay "
                        + "grupos marcados como llenos que estén bloqueando mejores combinaciones.",
                        "Sin resultados dentro del límite", JOptionPane.WARNING_MESSAGE);
                return;
            }
            evaluadas = dentroDelLimite;
        }

        evaluadas.sort(construirComparadorOptimo(estrategia));

        List<List<Grupo>> mejores = new ArrayList<>();
        for (CombinacionEvaluada ce : evaluadas) {
            if (mejores.size() >= cantidad) break;
            mejores.add(ce.grupos);
        }

        if (usarBorradorActual) {
            int indexTab = tabbedPaneHorarios.getSelectedIndex();
            if (indexTab < 0) {
                JOptionPane.showMessageDialog(this, "No hay ningún borrador abierto.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            HorarioBorrador borradorActivo = borradores.get(indexTab);
            borradorActivo.getGruposActivos().clear();
            borradorActivo.getGruposActivos().addAll(mejores.get(0));
            GestorPersistencia.guardar(materiasRegistradas, borradores);
            reconstruirPestanias();
            tabbedPaneHorarios.setSelectedIndex(indexTab);
        } else {
            int primerIndiceNuevo = borradores.size();
            for (int i = 0; i < mejores.size(); i++) {
                String nombre = nombreBorradorDisponible("Óptimo", i + 1);
                HorarioBorrador nuevo = new HorarioBorrador(nombre);
                nuevo.getGruposActivos().addAll(mejores.get(i));
                borradores.add(nuevo);
            }
            GestorPersistencia.guardar(materiasRegistradas, borradores);
            reconstruirPestanias();
            tabbedPaneHorarios.setSelectedIndex(primerIndiceNuevo);
        }

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Se calcularon ").append(todasLasCombinaciones.size()).append(" horario(s) posible(s)");
        if (limitarHuecos) {
            mensaje.append(", de los cuales ").append(evaluadas.size())
                    .append(" respetan el límite de ").append(limiteHuecos).append(" hora(s) libre(s) al día");
        }
        mensaje.append(", y se seleccionaron ").append(mejores.size()).append(" con el mejor puntaje.");
        if (!materiasSinGruposDisponibles.isEmpty()) {
            mensaje.append("\n\nNo se pudieron incluir estas materias (sin grupos disponibles):\n");
            for (String nombre : materiasSinGruposDisponibles) mensaje.append("• ").append(nombre).append("\n");
        }
        if (contadorExploracion[0] >= LIMITE_EXPLORACION_OPTIMO) {
            mensaje.append("\n\n⚠ Se alcanzó el límite de exploración (").append(LIMITE_EXPLORACION_OPTIMO)
                    .append(" combinaciones). El resultado es muy bueno, pero podría no ser el óptimo absoluto entre TODAS las combinaciones posibles.");
        }
        JOptionPane.showMessageDialog(this, mensaje.toString(), "Horario Óptimo",
                materiasSinGruposDisponibles.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Enumera TODAS las combinaciones válidas (un grupo disponible por materia, sin
     * choques de horario) hasta un límite de seguridad, para poder evaluarlas y
     * elegir la(s) de menor costo. A diferencia del Modo Rápido, aquí no se detiene
     * en la primera combinación encontrada: recorre el árbol completo de posibilidades.
     */
    private void enumerarCombinacionesValidas(List<Materia> materias, int indice, List<Grupo> actual,
                                               List<List<Grupo>> resultados, int[] contadorExploracion) {
        if (contadorExploracion[0] >= LIMITE_EXPLORACION_OPTIMO) return;

        if (indice == materias.size()) {
            resultados.add(new ArrayList<>(actual));
            contadorExploracion[0]++;
            return;
        }

        for (Grupo g : materias.get(indice).getGrupos()) {
            if (!g.isDisponible()) continue;

            boolean choca = false;
            for (Grupo existente : actual) {
                if (existente.chocaCon(g)) { choca = true; break; }
            }
            if (!choca) {
                actual.add(g);
                enumerarCombinacionesValidas(materias, indice + 1, actual, resultados, contadorExploracion);
                actual.remove(actual.size() - 1);
                if (contadorExploracion[0] >= LIMITE_EXPLORACION_OPTIMO) return;
            }
        }
    }

    private int calcularDificultadTotal(List<Grupo> combinacion, FuenteDificultad fuente) {
        int total = 0;
        for (Grupo g : combinacion) {
            switch (fuente) {
                case GRUPO:
                    total += g.getDificultad();
                    break;
                case MATERIA:
                    total += g.getMateriaPadre().getDificultad();
                    break;
                case AMBAS:
                    total += g.getDificultad() + g.getMateriaPadre().getDificultad();
                    break;
            }
        }
        return total;
    }

    private Comparator<CombinacionEvaluada> construirComparadorOptimo(EstrategiaBalance estrategia) {
        switch (estrategia) {
            case DIFICULTAD_PRIMERO:
                return Comparator.<CombinacionEvaluada>comparingInt(c -> c.dificultad)
                        .thenComparingInt(c -> c.huecos);
            case INTERMEDIO:
                // Punto intermedio: huecos y dificultad pesan igual en el puntaje total
                return Comparator.comparingInt(c -> (c.huecos + c.dificultad));
            case HUECOS_PRIMERO:
            default:
                return Comparator.<CombinacionEvaluada>comparingInt(c -> c.huecos)
                        .thenComparingInt(c -> c.dificultad);
        }
    }

    private void crearMateria() {
        String nombre = txtNombreMateria.getText().trim();
        if (nombre.isEmpty()) return;

        Materia m = new Materia(nombre, colorSeleccionado);
        m.setDificultad((Integer) spinnerDificultadMateria.getValue());
        materiasRegistradas.add(m);
        GestorPersistencia.guardar(materiasRegistradas, borradores);

        txtNombreMateria.setText("");
        spinnerDificultadMateria.setValue(3);
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
        nuevoGrupo.setDificultad((Integer) spinnerDificultadGrupo.getValue());
        materiaSeleccionada.agregarGrupo(nuevoGrupo);

        GestorPersistencia.guardar(materiasRegistradas, borradores);
        actualizarCombosYListas();

        txtClaveGrupo.setText("");
        txtProfesor.setText("");
        spinnerDificultadGrupo.setValue(3);
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

                String textoBase = m != null
                        ? m.getNombre() + " [" + g.getClaveGrupo() + "] - " + g.getProfesor()
                            + " (" + g.getHoraInicio() + ":00 hrs) - Dif.Grupo:" + g.getDificultad() + " Dif.Materia:" + m.getDificultad()
                        : g.toString();

                if (!g.isDisponible()) {
                    setText("<html><strike>" + textoBase + "</strike> &nbsp;<b>[LLENO]</b></html>");
                    if (!isSelected) {
                        c.setBackground(new Color(255, 210, 210));
                        c.setForeground(new Color(150, 30, 30));
                    }
                } else {
                    setText(textoBase);
                    if (!isSelected && m != null) {
                        c.setBackground(colorSuave(m.getColor()));
                        c.setForeground(Color.DARK_GRAY);
                    }
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