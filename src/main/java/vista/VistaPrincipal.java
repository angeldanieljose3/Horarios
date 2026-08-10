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
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
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
    private JComboBox<Integer> cbSemestreMateria;
    private JSpinner spinnerCreditosMateria;
    private JComboBox<String> cbPrerrequisitoMateria;

    // Formulario Grupos
    private JComboBox<Integer> cbFiltroSemestreGrupo;
    private JComboBox<Materia> cbMateriasExistentes;
    private JTextField txtClaveGrupo, txtProfesor;
    private JComboBox<String> cbHoraInicio;
    private JCheckBox chkLunes, chkMartes, chkMiercoles, chkJueves, chkViernes;
    private JSpinner spinnerDificultadGrupo;

    // Listado Catalogo
    private DefaultListModel<Grupo> modelListaGrupos;
    private JList<Grupo> listGrupos;
    private JComboBox<Integer> cbFiltroSemestreCatalogo;

    // Componente de Pestañas (Tabs)
    private JTabbedPane tabbedPaneHorarios;

    // Retícula: si está activa, se avisa (sin bloquear) cuando eliges una materia
    // cuyo prerrequisito no has marcado como cursado.
    private boolean aplicarReticula;
    private JCheckBox chkValidarReticula;

    public VistaPrincipal() {
        materiasRegistradas = GestorPersistencia.cargarMaterias();
        borradores = GestorPersistencia.cargarBorradores();
        aplicarReticula = GestorPersistencia.cargarConfiguracionReticula();

        setTitle("Simulador de Horarios Universitarios - Múltiples Borradores");
        setSize(1250, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                crearPanelIzquierdo(),
                crearPanelDerecho()
        );
        splitPane.setDividerLocation(480);
        add(splitPane, BorderLayout.CENTER);

        actualizarCombosYListas();
        reconstruirPestanias();

        configurarAtajoDeshacer();
    }

    // =======================================================
    // DESHACER (Ctrl+Z) — pila básica de últimas acciones destructivas
    // =======================================================

    private static class AccionDeshacer {
        final String descripcion;
        final Runnable comoDeshacer;
        AccionDeshacer(String descripcion, Runnable comoDeshacer) {
            this.descripcion = descripcion;
            this.comoDeshacer = comoDeshacer;
        }
    }

    private final Deque<AccionDeshacer> pilaDeshacer = new ArrayDeque<>();
    private static final int MAX_ACCIONES_DESHACER = 10;

    private void configurarAtajoDeshacer() {
        KeyStroke ctrlZ = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlZ, "deshacerAccion");
        getRootPane().getActionMap().put("deshacerAccion", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { deshacerUltimaAccion(); }
        });
    }

    /** Guarda cómo revertir una acción destructiva. Se llama justo ANTES de aplicar el cambio. */
    private void registrarParaDeshacer(String descripcion, Runnable comoDeshacer) {
        pilaDeshacer.push(new AccionDeshacer(descripcion, comoDeshacer));
        while (pilaDeshacer.size() > MAX_ACCIONES_DESHACER) pilaDeshacer.removeLast();
    }

    private void deshacerUltimaAccion() {
        if (pilaDeshacer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay ninguna acción reciente para deshacer.",
                    "Deshacer", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        AccionDeshacer accion = pilaDeshacer.pop();
        accion.comoDeshacer.run();
        GestorPersistencia.guardar(materiasRegistradas, borradores);
        reconstruirPestanias();
        JOptionPane.showMessageDialog(this, "Se deshizo: " + accion.descripcion, "Deshacer", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Punto 3: historial visible de las últimas acciones destructivas (reutiliza la misma pila de Deshacer). */
    private void mostrarHistorialAcciones() {
        JDialog dialogo = new JDialog(this, "Historial de Acciones Recientes", true);
        dialogo.setLayout(new BorderLayout(10, 10));
        FrutigerAeroUI.PanelCielo fondo = new FrutigerAeroUI.PanelCielo(new BorderLayout(10, 10));
        fondo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialogo.setContentPane(fondo);

        DefaultListModel<String> modelHistorial = new DefaultListModel<>();
        if (pilaDeshacer.isEmpty()) {
            modelHistorial.addElement("(Todavía no se ha registrado ninguna acción en esta sesión)");
        } else {
            int numero = pilaDeshacer.size();
            for (AccionDeshacer accion : pilaDeshacer) {
                modelHistorial.addElement(numero + ". " + accion.descripcion + (numero == pilaDeshacer.size() ? "  (más reciente)" : ""));
                numero--;
            }
        }

        JList<String> listaHistorial = new JList<>(modelHistorial);
        listaHistorial.setFont(FrutigerAeroUI.FUENTE_NORMAL);
        JScrollPane scroll = new JScrollPane(listaHistorial);
        scroll.setBorder(FrutigerAeroUI.bordeTitulado("Últimas " + MAX_ACCIONES_DESHACER + " acciones (la más reciente arriba)"));
        scroll.setPreferredSize(new Dimension(480, 260));
        dialogo.add(scroll, BorderLayout.CENTER);

        JLabel lblNota = new JLabel("<html>Nota: solo se listan acciones que se pueden deshacer (vaciar borrador, "
                + "reemplazar/inscribir grupo, eliminar o importar un borrador, etc).</html>");
        lblNota.setFont(FrutigerAeroUI.FUENTE_NORMAL);
        lblNota.setForeground(FrutigerAeroUI.TEXTO_OSCURO);
        lblNota.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        dialogo.add(lblNota, BorderLayout.NORTH);

        JButton btnCerrar = new FrutigerAeroUI.BotonGlossy("Cerrar", FrutigerAeroUI.AGUA_PROFUNDA);
        btnCerrar.addActionListener(e -> dialogo.dispose());
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        panelBotones.add(btnCerrar);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    // =======================================================
    // RETÍCULA: materias cursadas + aviso de prerrequisitos
    // =======================================================

    /** Diálogo de checklist para marcar qué materias ya cursó el usuario. */
    private void mostrarDialogoMateriasCursadas() {
        if (materiasRegistradas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todavía no has registrado ninguna materia.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialogo = new JDialog(this, "Materias ya cursadas", true);
        FrutigerAeroUI.PanelCielo fondo = new FrutigerAeroUI.PanelCielo(new BorderLayout(10, 10));
        fondo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialogo.setContentPane(fondo);
        dialogo.setLayout(new BorderLayout(10, 10));

        JPanel panelLista = new JPanel();
        panelLista.setOpaque(false);
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));

        List<Materia> ordenadas = new ArrayList<>(materiasRegistradas);
        ordenadas.sort(Comparator.comparingInt(Materia::getSemestre).thenComparing(Materia::getNombre));

        List<JCheckBox> checks = new ArrayList<>();
        for (Materia m : ordenadas) {
            JCheckBox chk = new JCheckBox("Sem." + m.getSemestre() + " - " + m.getNombre(), m.isCursada());
            chk.setOpaque(false);
            chk.putClientProperty("materia", m);
            checks.add(chk);
            panelLista.add(chk);
        }

        JScrollPane scroll = new JScrollPane(panelLista);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.setPreferredSize(new Dimension(420, 320));
        scroll.setBorder(FrutigerAeroUI.bordeTitulado("Marca las materias que ya aprobaste"));
        dialogo.add(scroll, BorderLayout.CENTER);

        JButton btnGuardar = new FrutigerAeroUI.BotonGlossy("Guardar", FrutigerAeroUI.VERDE_HOJA);
        JButton btnCancelar = new FrutigerAeroUI.BotonGlossy("Cancelar", FrutigerAeroUI.ROJO_CORAL);
        btnCancelar.addActionListener(e -> dialogo.dispose());
        btnGuardar.addActionListener(e -> {
            for (JCheckBox chk : checks) {
                ((Materia) chk.getClientProperty("materia")).setCursada(chk.isSelected());
            }
            GestorPersistencia.guardar(materiasRegistradas, borradores);
            dialogo.dispose();
        });
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    /**
     * Revisa la lista de materias que el usuario está por seleccionar y devuelve, para cada
     * una que tenga un prerrequisito definido y ese prerrequisito NO esté marcado como cursado,
     * una línea descriptiva. Lista vacía = todo en regla (o la retícula está desactivada).
     */
    private List<String> materiasConPrerrequisitoFaltante(List<Materia> seleccionadas) {
        List<String> faltantes = new ArrayList<>();
        if (!aplicarReticula) return faltantes;

        for (Materia m : seleccionadas) {
            String prereqNombre = m.getPrerrequisito();
            if (prereqNombre == null || prereqNombre.trim().isEmpty()) continue;

            Materia prereqMateria = buscarMateriaPorNombre(prereqNombre);
            boolean prereqCumplido = (prereqMateria != null && prereqMateria.isCursada());
            if (!prereqCumplido) {
                faltantes.add(m.getNombre() + "  →  requiere antes: " + prereqNombre);
            }
        }
        return faltantes;
    }

    private Materia buscarMateriaPorNombre(String nombre) {
        for (Materia m : materiasRegistradas) {
            if (m.getNombre().equalsIgnoreCase(nombre.trim())) return m;
        }
        return null;
    }

    /**
     * Muestra el aviso de prerrequisitos faltantes (si la retícula está activa y hay alguno) y
     * pregunta si se desea continuar de todas formas. true = continuar, false = cancelar la acción.
     */
    private boolean confirmarPrerrequisitos(List<Materia> seleccionadas) {
        List<String> faltantes = materiasConPrerrequisitoFaltante(seleccionadas);
        if (faltantes.isEmpty()) return true;

        StringBuilder msg = new StringBuilder("Estás por elegir materia(s) sin haber marcado como cursado su prerrequisito:\n\n");
        for (String f : faltantes) msg.append("• ").append(f).append("\n");
        msg.append("\n¿Deseas continuar de todas formas?");

        int resultado = JOptionPane.showConfirmDialog(this, msg.toString(), "Aviso de prerrequisitos",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return resultado == JOptionPane.YES_OPTION;
    }

    // =======================================================
    // EXPORTAR / IMPORTAR UN SOLO BORRADOR
    // =======================================================

    private void exportarBorradorActual() {
        int index = tabbedPaneHorarios.getSelectedIndex();
        if (index < 0) {
            JOptionPane.showMessageDialog(this, "No hay ningún borrador seleccionado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        HorarioBorrador borrador = borradores.get(index);

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportar borrador \"" + borrador.getNombre() + "\"");
        chooser.setSelectedFile(new File(sanitizarNombreArchivo(borrador.getNombre()) + ".horario"));
        int resultado = chooser.showSaveDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) return;

        File destino = chooser.getSelectedFile();
        try {
            GestorPersistencia.exportarBorradorIndividual(borrador, destino);
            JOptionPane.showMessageDialog(this, "Borrador exportado a:\n" + destino.getAbsolutePath(),
                    "Exportación exitosa", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo exportar el borrador:\n" + ex.getMessage(),
                    "Error al exportar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importarBorradorDesdeArchivo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Importar borrador");
        int resultado = chooser.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) return;

        File origen = chooser.getSelectedFile();
        try {
            HorarioBorrador importado = GestorPersistencia.importarBorradorIndividual(origen);

            // Evitar nombres duplicados entre borradores ya abiertos
            String nombreBase = importado.getNombre();
            String nombreFinal = nombreBase;
            int sufijo = 2;
            Set<String> nombresExistentes = new HashSet<>();
            for (HorarioBorrador b : borradores) nombresExistentes.add(b.getNombre());
            while (nombresExistentes.contains(nombreFinal)) {
                nombreFinal = nombreBase + " (" + sufijo + ")";
                sufijo++;
            }
            importado.setNombre(nombreFinal);

            borradores.add(importado);
            final HorarioBorrador importadoFinal = importado;
            registrarParaDeshacer("Importar borrador \"" + nombreFinal + "\"", () -> borradores.remove(importadoFinal));

            GestorPersistencia.guardar(materiasRegistradas, borradores);
            reconstruirPestanias();
            tabbedPaneHorarios.setSelectedIndex(borradores.size() - 1);

            JOptionPane.showMessageDialog(this, "Borrador \"" + nombreFinal + "\" importado con " +
                    importado.getGruposActivos().size() + " grupo(s).", "Importación exitosa", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo importar el archivo seleccionado:\n" + ex.getMessage(),
                    "Error al importar", JOptionPane.ERROR_MESSAGE);
        }
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

        txtNombreMateria = new JTextField(9);
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

        Integer[] semestres = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        cbSemestreMateria = new JComboBox<>(semestres);

        JButton btnGuardarMateria = new FrutigerAeroUI.BotonGlossy("Guardar", FrutigerAeroUI.VERDE_HOJA);
        btnGuardarMateria.addActionListener(e -> crearMateria());

        spinnerDificultadMateria = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        spinnerCreditosMateria = new JSpinner(new SpinnerNumberModel(5, 0, 15, 1));
        cbPrerrequisitoMateria = new JComboBox<>();

        panelMateria.add(new JLabel("Nombre:"));
        panelMateria.add(txtNombreMateria);
        panelMateria.add(btnElegirColor);
        panelMateria.add(new JLabel("Sem:"));
        panelMateria.add(cbSemestreMateria);
        panelMateria.add(new JLabel("Dif:"));
        panelMateria.add(spinnerDificultadMateria);
        panelMateria.add(new JLabel("Créd:"));
        panelMateria.add(spinnerCreditosMateria);
        panelMateria.add(new JLabel("Prerrequisito:"));
        panelMateria.add(cbPrerrequisitoMateria);
        panelMateria.add(btnGuardarMateria);

        // --- SUBFORMULARIO 2: GRUPOS ---
        JPanel panelGrupo = new JPanel(new GridBagLayout());
        panelGrupo.setOpaque(false);
        panelGrupo.setBorder(FrutigerAeroUI.bordeTitulado("2. Añadir Grupos a Materia"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbFiltroSemestreGrupo = new JComboBox<>(semestres);
        cbFiltroSemestreGrupo.addActionListener(e -> actualizarMateriasPorSemestre());

        cbMateriasExistentes = new JComboBox<>();
        txtClaveGrupo = new JTextField(10);
        txtProfesor = new JTextField(10);

        String[] horas = new String[13];
        for (int i = 0; i < 13; i++) horas[i] = String.format("%02d:00 hrs", i + 7);
        cbHoraInicio = new JComboBox<>(horas);

        chkLunes = new JCheckBox(htmlDia("L")); chkMartes = new JCheckBox(htmlDia("M")); chkMiercoles = new JCheckBox(htmlDia("X"));
        chkJueves = new JCheckBox(htmlDia("J")); chkViernes = new JCheckBox(htmlDia("V"));
        for (JCheckBox chkDia : new JCheckBox[]{chkLunes, chkMartes, chkMiercoles, chkJueves, chkViernes}) {
            chkDia.setForeground(FrutigerAeroUI.TEXTO_OSCURO);
            chkDia.setOpaque(false);
        }
        JPanel panelDias = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        panelDias.setOpaque(false);
        panelDias.add(chkLunes); panelDias.add(chkMartes); panelDias.add(chkMiercoles);
        panelDias.add(chkJueves); panelDias.add(chkViernes);

        gbc.gridx = 0; gbc.gridy = 0; panelGrupo.add(new JLabel("Semestre:"), gbc);
        gbc.gridx = 1; panelGrupo.add(cbFiltroSemestreGrupo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panelGrupo.add(new JLabel("Materia:"), gbc);
        gbc.gridx = 1; panelGrupo.add(cbMateriasExistentes, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panelGrupo.add(new JLabel("Cod. Grupo:"), gbc);
        gbc.gridx = 1; panelGrupo.add(txtClaveGrupo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panelGrupo.add(new JLabel("Profesor:"), gbc);
        gbc.gridx = 1; panelGrupo.add(txtProfesor, gbc);

        gbc.gridx = 0; gbc.gridy = 4; panelGrupo.add(new JLabel("Hora Inicio:"), gbc);
        gbc.gridx = 1; panelGrupo.add(cbHoraInicio, gbc);

        gbc.gridx = 0; gbc.gridy = 5; panelGrupo.add(new JLabel("Días:"), gbc);
        gbc.gridx = 1; panelGrupo.add(panelDias, gbc);

        spinnerDificultadGrupo = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        gbc.gridx = 0; gbc.gridy = 6; panelGrupo.add(new JLabel("Dificultad (1-5):"), gbc);
        gbc.gridx = 1; panelGrupo.add(spinnerDificultadGrupo, gbc);

        JButton btnGuardarGrupo = new FrutigerAeroUI.BotonGlossy("+ Registrar Grupo", FrutigerAeroUI.VERDE_HOJA);
        btnGuardarGrupo.addActionListener(e -> agregarGrupoAMateria());
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        panelGrupo.add(btnGuardarGrupo, gbc);

        panelFormularios.add(panelMateria, BorderLayout.NORTH);
        panelFormularios.add(panelGrupo, BorderLayout.CENTER);
        panel.add(panelFormularios, BorderLayout.NORTH);

        // --- CATÁLOGO ---
        modelListaGrupos = new DefaultListModel<>();
        listGrupos = new JList<>(modelListaGrupos);
        listGrupos.setCellRenderer(new RenderizadorGrupoLista());

        cbFiltroSemestreCatalogo = crearComboFiltroSemestre();
        cbFiltroSemestreCatalogo.addActionListener(e -> actualizarListaGrupos());

        JPanel panelFiltroCatalogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelFiltroCatalogo.setOpaque(false);
        JLabel lblFiltroCatalogo = new JLabel("Ver semestre:");
        lblFiltroCatalogo.setForeground(FrutigerAeroUI.TEXTO_OSCURO);
        panelFiltroCatalogo.add(lblFiltroCatalogo);
        panelFiltroCatalogo.add(cbFiltroSemestreCatalogo);

        JPanel panelCatalogo = new JPanel(new BorderLayout(0, 5));
        panelCatalogo.setOpaque(false);
        panelCatalogo.add(panelFiltroCatalogo, BorderLayout.NORTH);
        panelCatalogo.add(new JScrollPane(listGrupos), BorderLayout.CENTER);

        panel.add(panelCatalogo, BorderLayout.CENTER);

        JButton btnEditar = new FrutigerAeroUI.BotonGlossy("✏️ Editar Selección", FrutigerAeroUI.CIELO_MEDIO.darker());
        btnEditar.addActionListener(e -> mostrarDialogoEditarSeleccion());

        JButton btnToggleDisponible = new FrutigerAeroUI.BotonGlossy("⛔ Marcar/Desmarcar como Lleno", FrutigerAeroUI.NARANJA_SOL);
        btnToggleDisponible.addActionListener(e -> toggleDisponibilidadGrupoSeleccionado());

        JButton btnInscribir = new FrutigerAeroUI.BotonGlossy("► Inscribir en Borrador Activo", FrutigerAeroUI.AGUA_PROFUNDA);
        btnInscribir.addActionListener(e -> inscribirGrupoEnBorradorSeleccionado());

        JPanel panelBotonesCatalogo = new JPanel(new GridLayout(3, 1, 0, 3));
        panelBotonesCatalogo.setOpaque(false);
        panelBotonesCatalogo.add(btnEditar);
        panelBotonesCatalogo.add(btnToggleDisponible);
        panelBotonesCatalogo.add(btnInscribir);
        panel.add(panelBotonesCatalogo, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelDerecho() {
    JPanel panel = new FrutigerAeroUI.PanelCielo(new BorderLayout(5, 5));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // =======================================================
    // BARRA DE HERRAMIENTAS (JToolBar)
    // =======================================================
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false); // Evita que el usuario la desclave si no lo deseas
    toolBar.setOpaque(false);
    toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));

    // --- GRUPO 1: Borradores ---
    JButton btnNuevoBorrador = new FrutigerAeroUI.BotonGlossy("+ Nuevo", FrutigerAeroUI.AGUA_PROFUNDA);
    btnNuevoBorrador.addActionListener(e -> crearNuevoBorrador());

    JButton btnEliminarBorrador = new FrutigerAeroUI.BotonGlossy("Eliminar", FrutigerAeroUI.ROJO_CORAL);
    btnEliminarBorrador.addActionListener(e -> eliminarBorradorActual());

    toolBar.add(btnNuevoBorrador);
    toolBar.add(btnEliminarBorrador);
    toolBar.addSeparator();

    // --- GRUPO 2: Algoritmos / Generación ---
    JButton btnModoRapido = new FrutigerAeroUI.BotonGlossy("⚡ Modo Rápido", FrutigerAeroUI.NARANJA_SOL);
    btnModoRapido.addActionListener(e -> mostrarDialogoModoRapido());

    JButton btnHorarioOptimo = new FrutigerAeroUI.BotonGlossy("🏆 Horario Óptimo", FrutigerAeroUI.VERDE_HOJA);
    btnHorarioOptimo.addActionListener(e -> mostrarDialogoHorarioOptimo());

    toolBar.add(btnModoRapido);
    toolBar.add(btnHorarioOptimo);
    toolBar.addSeparator();

    // --- GRUPO 3: Utilidades (Menú Desplegable "Herramientas") ---
    JPopupMenu menuHerramientas = new JPopupMenu();
    
    JMenuItem itemComparar = new JMenuItem("⚖ Comparar Borradores");
    itemComparar.addActionListener(e -> mostrarDialogoCompararBorradores());

    JMenuItem itemHistorial = new JMenuItem("📜 Historial de Acciones");
    itemHistorial.addActionListener(e -> mostrarHistorialAcciones());

    JMenuItem itemMateriasCursadas = new JMenuItem("✓ Materias Cursadas");
    itemMateriasCursadas.addActionListener(e -> mostrarDialogoMateriasCursadas());

    JMenuItem itemBorrarTodos = new JMenuItem("🗑 Borrar Todos los Horarios");
    itemBorrarTodos.addActionListener(e -> borrarTodosLosHorarios());

    menuHerramientas.add(itemComparar);
    menuHerramientas.add(itemHistorial);
    menuHerramientas.add(itemMateriasCursadas);
    menuHerramientas.addSeparator();
    menuHerramientas.add(itemBorrarTodos);

    JButton btnHerramientas = new FrutigerAeroUI.BotonGlossy("🛠 Herramientas ▾", FrutigerAeroUI.CIELO_MEDIO.darker());
    btnHerramientas.addActionListener(e -> menuHerramientas.show(btnHerramientas, 0, btnHerramientas.getHeight()));

    toolBar.add(btnHerramientas);

    // --- GRUPO 4: Exportación / Importación (Menú Desplegable "Archivo/E-S") ---
    JPopupMenu menuExportar = new JPopupMenu();
    
    JMenuItem itemExpJPG = new JMenuItem("🖼 Exportar como JPG");
    itemExpJPG.addActionListener(e -> exportarHorariosJPG());

    JMenuItem itemExpPDF = new JMenuItem("📄 Exportar como PDF");
    itemExpPDF.addActionListener(e -> exportarHorariosPDF());

    JMenuItem itemExpBorrador = new JMenuItem("💾 Exportar Borrador (.horario)");
    itemExpBorrador.addActionListener(e -> exportarBorradorActual());

    JMenuItem itemImpBorrador = new JMenuItem("📂 Importar Borrador");
    itemImpBorrador.addActionListener(e -> importarBorradorDesdeArchivo());

    menuExportar.add(itemExpJPG);
    menuExportar.add(itemExpPDF);
    menuExportar.addSeparator();
    menuExportar.add(itemExpBorrador);
    menuExportar.add(itemImpBorrador);

    JButton btnExportarImportar = new FrutigerAeroUI.BotonGlossy("💾 Archivo ▾", FrutigerAeroUI.AGUA_PROFUNDA.darker());
    btnExportarImportar.addActionListener(e -> menuExportar.show(btnExportarImportar, 0, btnExportarImportar.getHeight()));

    toolBar.add(btnExportarImportar);
    toolBar.addSeparator();

    // --- GRUPO 5: Deshacer y Opciones rápidas ---
    JButton btnDeshacer = new FrutigerAeroUI.BotonGlossy("↩ Deshacer", FrutigerAeroUI.CIELO_MEDIO.darker());
    btnDeshacer.addActionListener(e -> deshacerUltimaAccion());

    chkValidarReticula = new JCheckBox("📚 Retícula");
    chkValidarReticula.setOpaque(false);
    chkValidarReticula.setForeground(FrutigerAeroUI.TEXTO_OSCURO);
    chkValidarReticula.setFont(FrutigerAeroUI.FUENTE_TITULO);
    chkValidarReticula.setSelected(aplicarReticula);
    chkValidarReticula.setToolTipText("Validar Retícula (avisar prerrequisitos)");
    chkValidarReticula.addActionListener(e -> {
        aplicarReticula = chkValidarReticula.isSelected();
        GestorPersistencia.guardarConfiguracionReticula(aplicarReticula);
    });

    toolBar.add(btnDeshacer);
    toolBar.add(chkValidarReticula);

    // Añadir la ToolBar en la parte superior (NORTH)
    panel.add(toolBar, BorderLayout.NORTH);

    // Tabs al centro
    tabbedPaneHorarios = new JTabbedPane();
    FrutigerAeroUI.estilizarTabbedPane(tabbedPaneHorarios);
    panel.add(tabbedPaneHorarios, BorderLayout.CENTER);

    return panel;
}

    private void reconstruirPestanias() {
        tabbedPaneHorarios.removeAll();

        for (HorarioBorrador borrador : borradores) {
            JPanel panelTab = crearPanelVistaBorrador(borrador);
            tabbedPaneHorarios.addTab(borrador.getNombre(), panelTab);
        }
    }

    private JPanel crearPanelVistaBorrador(HorarioBorrador borrador) {
        JPanel panel = new FrutigerAeroUI.PanelCielo(new BorderLayout(5, 5));

        String[] colHorario = {"Hora", "LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES"};
        DefaultTableModel modelHorario = new DefaultTableModel(colHorario, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tablaHorario = new JTable(modelHorario);
        tablaHorario.setRowHeight(32);
        tablaHorario.getTableHeader().setReorderingAllowed(false);
        tablaHorario.setDefaultRenderer(Object.class, new RenderizadorColorTabla());

        DefaultTableCellRenderer centradoRenderer = new DefaultTableCellRenderer();
        centradoRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tablaHorario.getColumnModel().getColumn(0).setCellRenderer(centradoRenderer);

        FrutigerAeroUI.estilizarHeaderTabla(tablaHorario);

        JScrollPane scrollHorario = new JScrollPane(tablaHorario);
        scrollHorario.setBorder(FrutigerAeroUI.bordeTitulado("Parrilla Semanal"));

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

        renderizarParrillaYResumen(borrador, modelHorario, modelResumen);

        JSplitPane splitVer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollHorario, scrollResumen);
        splitVer.setResizeWeight(0.80);
        splitVer.setDividerLocation(480);

        panel.add(splitVer, BorderLayout.CENTER);

        JLabel lblResumenCarga = new JLabel();
        lblResumenCarga.setName("lblResumenCarga");
        lblResumenCarga.setFont(FrutigerAeroUI.FUENTE_TITULO);
        lblResumenCarga.setForeground(FrutigerAeroUI.TEXTO_OSCURO);
        lblResumenCarga.setHorizontalAlignment(SwingConstants.CENTER);
        actualizarResumenCarga(lblResumenCarga, borrador);

        JButton btnVaciar = new FrutigerAeroUI.BotonGlossy("Vaciar " + borrador.getNombre(), FrutigerAeroUI.ROJO_CORAL);
        btnVaciar.addActionListener(e -> {
            if (borrador.getGruposActivos().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Este borrador ya está vacío.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            List<Grupo> respaldo = new ArrayList<>(borrador.getGruposActivos());
            registrarParaDeshacer("Vaciar borrador \"" + borrador.getNombre() + "\" (" + respaldo.size() + " grupo(s))", () -> {
                borrador.getGruposActivos().clear();
                borrador.getGruposActivos().addAll(respaldo);
            });
            borrador.getGruposActivos().clear();
            GestorPersistencia.guardar(materiasRegistradas, borradores);
            renderizarParrillaYResumen(borrador, modelHorario, modelResumen);
            actualizarResumenCarga(lblResumenCarga, borrador);
        });

        JButton btnRecalcular = new FrutigerAeroUI.BotonGlossy("🔁 Recalcular este Horario", FrutigerAeroUI.AGUA_PROFUNDA);
        btnRecalcular.addActionListener(e -> mostrarDialogoRecalcular(borrador));

        JPanel panelBotonesBorrador = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panelBotonesBorrador.setOpaque(false);
        panelBotonesBorrador.add(btnRecalcular);
        panelBotonesBorrador.add(btnVaciar);

        JPanel panelSurCompleto = new JPanel(new BorderLayout());
        panelSurCompleto.setOpaque(false);
        panelSurCompleto.add(lblResumenCarga, BorderLayout.NORTH);
        panelSurCompleto.add(panelBotonesBorrador, BorderLayout.SOUTH);
        panel.add(panelSurCompleto, BorderLayout.SOUTH);

        return panel;
    }

    /** Busca recursivamente el primer componente cuyo name() coincida (para actualizar la UI desde otros métodos). */
    private Component buscarComponentePorNombre(Container raiz, String nombre) {
        for (Component c : raiz.getComponents()) {
            if (nombre.equals(c.getName())) return c;
            if (c instanceof Container) {
                Component encontrado = buscarComponentePorNombre((Container) c, nombre);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }

    /** Punto 4: resumen de carga (materias, créditos y horas ocupadas por semana) de un borrador. */
    private void actualizarResumenCarga(JLabel label, HorarioBorrador borrador) {
        Set<Materia> materiasDistintas = new LinkedHashSet<>();
        int horasSemana = 0;
        for (Grupo g : borrador.getGruposActivos()) {
            if (g.getMateriaPadre() != null) materiasDistintas.add(g.getMateriaPadre());
            horasSemana += g.getDias().size(); // 1 hora por cada día que se imparte
        }
        int totalCreditos = 0;
        for (Materia m : materiasDistintas) totalCreditos += m.getCreditos();

        label.setText(String.format("📊 Carga actual: %d materia(s) · %d créditos · %d hrs/semana",
                materiasDistintas.size(), totalCreditos, horasSemana));
    }

    // =======================================================
    // COMPARAR BORRADORES LADO A LADO
    // =======================================================

    private void mostrarDialogoCompararBorradores() {
        if (borradores.size() < 2) {
            JOptionPane.showMessageDialog(this,
                    "Necesitas al menos 2 borradores para compararlos.\n" +
                    "Genera otra opción con 'Modo Rápido' u 'Horario Óptimo', o crea un borrador nuevo.",
                    "No hay suficientes borradores", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialogo = new JDialog(this, "Comparar Borradores", true);
        FrutigerAeroUI.PanelCielo fondo = new FrutigerAeroUI.PanelCielo(new BorderLayout(10, 10));
        fondo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialogo.setContentPane(fondo);
        dialogo.setLayout(new BorderLayout(10, 10));

        // --- Selectores de qué borrador va en cada lado ---
        JComboBox<HorarioBorrador> cbIzquierdo = new JComboBox<>(borradores.toArray(new HorarioBorrador[0]));
        JComboBox<HorarioBorrador> cbDerecho = new JComboBox<>(borradores.toArray(new HorarioBorrador[0]));
        cbIzquierdo.setSelectedIndex(0);
        cbDerecho.setSelectedIndex(borradores.size() > 1 ? 1 : 0);

        JPanel panelSelectores = new JPanel(new GridLayout(1, 2, 10, 0));
        panelSelectores.setOpaque(false);

        JPanel panelSelIzq = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSelIzq.setOpaque(false);
        panelSelIzq.add(new JLabel("Borrador A:"));
        panelSelIzq.add(cbIzquierdo);

        JPanel panelSelDer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSelDer.setOpaque(false);
        panelSelDer.add(new JLabel("Borrador B:"));
        panelSelDer.add(cbDerecho);

        panelSelectores.add(panelSelIzq);
        panelSelectores.add(panelSelDer);

        // --- Contenedor central: se reconstruye cada vez que cambia un selector ---
        JPanel panelComparacion = new JPanel(new GridLayout(1, 2, 10, 0));
        panelComparacion.setOpaque(false);

        Runnable actualizarComparacion = () -> {
            panelComparacion.removeAll();
            HorarioBorrador bIzq = (HorarioBorrador) cbIzquierdo.getSelectedItem();
            HorarioBorrador bDer = (HorarioBorrador) cbDerecho.getSelectedItem();
            panelComparacion.add(construirPanelComparativo(bIzq));
            panelComparacion.add(construirPanelComparativo(bDer));
            panelComparacion.revalidate();
            panelComparacion.repaint();
        };

        cbIzquierdo.addActionListener(e -> actualizarComparacion.run());
        cbDerecho.addActionListener(e -> actualizarComparacion.run());
        actualizarComparacion.run();

        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setOpaque(false);
        panelNorte.add(panelSelectores, BorderLayout.NORTH);

        dialogo.add(panelNorte, BorderLayout.NORTH);
        dialogo.add(panelComparacion, BorderLayout.CENTER);

        JButton btnCerrar = new FrutigerAeroUI.BotonGlossy("Cerrar", FrutigerAeroUI.ROJO_CORAL);
        btnCerrar.addActionListener(e -> dialogo.dispose());
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        panelBotones.add(btnCerrar);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        dialogo.setSize(1100, 650);
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    /** Panel de solo lectura con la parrilla + resumen + estadísticas rápidas de un borrador, para comparar. */
    private JPanel construirPanelComparativo(HorarioBorrador borrador) {
        JPanel panel = new FrutigerAeroUI.PanelCielo(new BorderLayout(5, 5));
        panel.setBorder(FrutigerAeroUI.bordeTitulado(borrador.getNombre()));

        String[] colHorario = {"Hora", "LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES"};
        DefaultTableModel modelHorario = new DefaultTableModel(colHorario, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaHorario = new JTable(modelHorario);
        tablaHorario.setRowHeight(28);
        tablaHorario.getTableHeader().setReorderingAllowed(false);
        tablaHorario.setDefaultRenderer(Object.class, new RenderizadorColorTabla());
        DefaultTableCellRenderer centradoRenderer = new DefaultTableCellRenderer();
        centradoRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tablaHorario.getColumnModel().getColumn(0).setCellRenderer(centradoRenderer);
        FrutigerAeroUI.estilizarHeaderTabla(tablaHorario);

        String[] colResumen = {"Materia", "Grupo", "Profesor", "Horario", "Días"};
        DefaultTableModel modelResumen = new DefaultTableModel(colResumen, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaResumen = new JTable(modelResumen);
        tablaResumen.setRowHeight(18);
        tablaResumen.getTableHeader().setReorderingAllowed(false);
        FrutigerAeroUI.estilizarHeaderTabla(tablaResumen);

        renderizarParrillaYResumen(borrador, modelHorario, modelResumen);

        JScrollPane scrollHorario = new JScrollPane(tablaHorario);
        scrollHorario.setBorder(FrutigerAeroUI.bordeTitulado("Parrilla"));
        JScrollPane scrollResumen = new JScrollPane(tablaResumen);
        scrollResumen.setBorder(FrutigerAeroUI.bordeTitulado("Resumen"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollHorario, scrollResumen);
        split.setResizeWeight(0.75);

        // --- Estadísticas rápidas para decidir entre opciones ---
        int totalMaterias = borrador.getGruposActivos().size();
        int huecos = calcularHorasMuertas(borrador.getGruposActivos());
        int dificultadTotal = calcularDificultadTotal(borrador.getGruposActivos(), FuenteDificultad.AMBAS);

        Set<Materia> materiasDistintasComp = new LinkedHashSet<>();
        int horasSemanaComp = 0;
        for (Grupo g : borrador.getGruposActivos()) {
            if (g.getMateriaPadre() != null) materiasDistintasComp.add(g.getMateriaPadre());
            horasSemanaComp += g.getDias().size();
        }
        int creditosComp = 0;
        for (Materia m : materiasDistintasComp) creditosComp += m.getCreditos();

        JLabel lblStats = new JLabel(String.format(
                "<html><b>%d</b> materia(s) &nbsp;|&nbsp; <b>%d</b> créditos &nbsp;|&nbsp; <b>%d</b> hrs/semana &nbsp;|&nbsp; "
                + "<b>%d</b> hora(s) muerta(s)/día &nbsp;|&nbsp; <b>%d</b> puntos de dificultad (grupo+materia)</html>",
                totalMaterias, creditosComp, horasSemanaComp, huecos, dificultadTotal));
        lblStats.setFont(FrutigerAeroUI.FUENTE_TITULO);
        lblStats.setForeground(FrutigerAeroUI.TEXTO_OSCURO);
        lblStats.setHorizontalAlignment(SwingConstants.CENTER);
        lblStats.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        panel.add(lblStats, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    /**
     * "Recalcular este Horario": mezcla de dos cosas —
     *  1) avisa si algún grupo YA inscrito en este borrador quedó marcado como LLENO
     *     (obsoleto) desde que se armó el horario, y
     *  2) reabre el diálogo de Modo Rápido u Horario Óptimo (a elección del usuario)
     *     con las materias de este borrador ya preseleccionadas, para que se puedan
     *     agregar o quitar materias antes de regenerar.
     */
    private void mostrarDialogoRecalcular(HorarioBorrador borrador) {
        if (borrador.getGruposActivos().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Este borrador todavía no tiene grupos inscritos; no hay nada que recalcular.\n" +
                    "Usa 'Modo Rápido' u 'Horario Óptimo' desde la barra superior para generarlo por primera vez.",
                    "Nada que recalcular", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 1) Detectar materias actuales y grupos que ya quedaron obsoletos (marcados como llenos)
        // NOTA: se guarda el NOMBRE de la materia, no el objeto Materia. Como materias.dat y
        // borradores.dat se serializan por separado (y CargadorDatos.java regenera el catálogo
        // preservando los borradores), el objeto Materia dentro de un Grupo ya guardado puede
        // dejar de ser la MISMA instancia que la de materiasRegistradas aunque represente la
        // misma materia. Comparar por nombre evita ese problema de identidad de objetos.
        Set<String> nombresMateriasEnBorrador = new LinkedHashSet<>();
        List<Grupo> gruposObsoletos = new ArrayList<>();
        for (Grupo g : borrador.getGruposActivos()) {
            if (g.getMateriaPadre() != null) nombresMateriasEnBorrador.add(g.getMateriaPadre().getNombre());
            if (!g.isDisponible()) gruposObsoletos.add(g);
        }

        if (!gruposObsoletos.isEmpty()) {
            StringBuilder msg = new StringBuilder("⚠ Este horario tiene ").append(gruposObsoletos.size())
                    .append(" grupo(s) que ahora están marcados como LLENOS:\n\n");
            for (Grupo g : gruposObsoletos) {
                msg.append("• ").append(g.getMateriaPadre().getNombre()).append(" [").append(g.getClaveGrupo()).append("]\n");
            }
            msg.append("\nAl recalcular, el sistema NO volverá a usar esos grupos (ya no están disponibles).");
            JOptionPane.showMessageDialog(this, msg.toString(), "Grupos obsoletos detectados", JOptionPane.WARNING_MESSAGE);
        }

        // 2) Elegir con qué método recalcular
        Object[] opciones = {"⚡ Modo Rápido", "🏆 Horario Óptimo", "Cancelar"};
        int eleccion = JOptionPane.showOptionDialog(this,
                "¿Con qué método quieres recalcular \"" + borrador.getNombre() + "\"?\n" +
                "Las materias que ya tenía este borrador van a quedar preseleccionadas; puedes agregar o quitar antes de generar.",
                "Recalcular Horario", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opciones, opciones[0]);

        if (eleccion == 0) {
            mostrarDialogoModoRapido(nombresMateriasEnBorrador, true);
        } else if (eleccion == 1) {
            mostrarDialogoHorarioOptimo(nombresMateriasEnBorrador, true);
        }
        // eleccion == 2 (Cancelar) o cerrar el diálogo: no hacer nada
    }

    private void renderizarParrillaYResumen(HorarioBorrador borrador, DefaultTableModel mHorario, DefaultTableModel mResumen) {
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

        mResumen.setRowCount(0);
        List<Grupo> gruposOrdenados = new ArrayList<>(borrador.getGruposActivos());
        Collections.sort(gruposOrdenados, Comparator.comparingInt(Grupo::getHoraInicio));

        for (Grupo g : gruposOrdenados) {
            String nombreMateria = (g.getMateriaPadre() != null) ? g.getMateriaPadre().getNombre() : "N/A";
            String clave = g.getClaveGrupo();
            String profesorCrudo = g.getProfesor();
            boolean sinDefinir = profesorCrudo == null || profesorCrudo.trim().isEmpty() || profesorCrudo.trim().equals("-");
            String profesor = sinDefinir ? "Por definir" : profesorCrudo;
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
    // EDITAR MATERIA / GRUPO
    // =======================================================

    // =======================================================
    // EDITAR MATERIA / GRUPO
    // =======================================================

    private void mostrarDialogoEditarSeleccion() {
        Grupo grupoSel = listGrupos.getSelectedValue();
        if (grupoSel == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un grupo del catálogo para editarlo o editar su materia.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Materia materiaSel = grupoSel.getMateriaPadre();

        JDialog dialogo = new JDialog(this, "Editar Materia / Grupo", true);
        FrutigerAeroUI.PanelCielo fondo = new FrutigerAeroUI.PanelCielo(new BorderLayout(10, 10));
        fondo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialogo.setContentPane(fondo);

        JTabbedPane tabs = new JTabbedPane();
        FrutigerAeroUI.estilizarTabbedPane(tabs);

        // -------------------------------------------------------
        // PESTAÑA 1: EDITAR MATERIA (Nombre, Semestre, Dificultad, Color)
        // -------------------------------------------------------
        JPanel panelEditMat = new JPanel(new GridBagLayout());
        panelEditMat.setOpaque(false);
        GridBagConstraints gbcM = new GridBagConstraints();
        gbcM.insets = new Insets(5, 5, 5, 5);
        gbcM.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtEditNombreMat = new JTextField(materiaSel.getNombre(), 15);
        Integer[] semestres = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        JComboBox<Integer> cbEditSemestreMat = new JComboBox<>(semestres);
        cbEditSemestreMat.setSelectedItem(materiaSel.getSemestre());

        // Dificultad de la Materia
        JSpinner spinnerEditDifMat = new JSpinner(new SpinnerNumberModel(materiaSel.getDificultad(), 1, 5, 1));

        // Créditos y prerrequisito (para el resumen de carga y la retícula)
        JSpinner spinnerEditCreditos = new JSpinner(new SpinnerNumberModel(materiaSel.getCreditos(), 0, 15, 1));

        JComboBox<String> cbEditPrerreq = new JComboBox<>();
        cbEditPrerreq.addItem("(Ninguno)");
        List<Materia> ordenadasParaPrereq = new ArrayList<>(materiasRegistradas);
        ordenadasParaPrereq.sort(Comparator.comparingInt(Materia::getSemestre).thenComparing(Materia::getNombre));
        for (Materia otra : ordenadasParaPrereq) {
            if (otra != materiaSel) cbEditPrerreq.addItem(otra.getNombre());
        }
        String prereqActual = materiaSel.getPrerrequisito();
        cbEditPrerreq.setSelectedItem((prereqActual == null || prereqActual.isEmpty()) ? "(Ninguno)" : prereqActual);

        final Color[] colorEditMateria = {materiaSel.getColor()};
        JButton btnColorEditMat = new JButton("Cambiar Color");
        btnColorEditMat.setBackground(colorEditMateria[0]);
        btnColorEditMat.setForeground(Color.WHITE);
        btnColorEditMat.setFont(FrutigerAeroUI.FUENTE_TITULO);
        btnColorEditMat.setFocusPainted(false);
        btnColorEditMat.addActionListener(e -> {
            Color c = JColorChooser.showDialog(dialogo, "Color de Materia", colorEditMateria[0]);
            if (c != null) {
                colorEditMateria[0] = c;
                btnColorEditMat.setBackground(c);
            }
        });

        gbcM.gridx = 0; gbcM.gridy = 0; panelEditMat.add(new JLabel("Nombre Materia:"), gbcM);
        gbcM.gridx = 1; panelEditMat.add(txtEditNombreMat, gbcM);
        gbcM.gridx = 0; gbcM.gridy = 1; panelEditMat.add(new JLabel("Semestre:"), gbcM);
        gbcM.gridx = 1; panelEditMat.add(cbEditSemestreMat, gbcM);
        gbcM.gridx = 0; gbcM.gridy = 2; panelEditMat.add(new JLabel("Dificultad Materia (1-5):"), gbcM);
        gbcM.gridx = 1; panelEditMat.add(spinnerEditDifMat, gbcM);
        gbcM.gridx = 0; gbcM.gridy = 3; panelEditMat.add(new JLabel("Créditos:"), gbcM);
        gbcM.gridx = 1; panelEditMat.add(spinnerEditCreditos, gbcM);
        gbcM.gridx = 0; gbcM.gridy = 4; panelEditMat.add(new JLabel("Prerrequisito:"), gbcM);
        gbcM.gridx = 1; panelEditMat.add(cbEditPrerreq, gbcM);
        gbcM.gridx = 0; gbcM.gridy = 5; gbcM.gridwidth = 2; panelEditMat.add(btnColorEditMat, gbcM);

        tabs.addTab("📘 Materia: " + materiaSel.getNombre(), panelEditMat);

        // -------------------------------------------------------
        // PESTAÑA 2: EDITAR GRUPO (Clave, Profesor, Hora, Dificultad Grupo, Días)
        // -------------------------------------------------------
        JPanel panelEditGrp = new JPanel(new GridBagLayout());
        panelEditGrp.setOpaque(false);
        GridBagConstraints gbcG = new GridBagConstraints();
        gbcG.insets = new Insets(5, 5, 5, 5);
        gbcG.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtEditClaveGrp = new JTextField(grupoSel.getClaveGrupo(), 10);
        JTextField txtEditProfesor = new JTextField(grupoSel.getProfesor(), 10);

        // Hora exacta del Grupo (7:00 a 19:00 hrs)
        String[] horas = new String[13];
        for (int i = 0; i < 13; i++) horas[i] = String.format("%02d:00 hrs", i + 7);
        JComboBox<String> cbEditHora = new JComboBox<>(horas);
        cbEditHora.setSelectedIndex(Math.max(0, grupoSel.getHoraInicio() - 7));

        // Dificultad específica del Grupo / Profesor
        JSpinner spinnerEditDifGrp = new JSpinner(new SpinnerNumberModel(grupoSel.getDificultad(), 1, 5, 1));

        JCheckBox chkL = new JCheckBox(htmlDia("L"), grupoSel.getDias().contains(DiaSemana.LUNES));
        JCheckBox chkM = new JCheckBox(htmlDia("M"), grupoSel.getDias().contains(DiaSemana.MARTES));
        JCheckBox chkX = new JCheckBox(htmlDia("X"), grupoSel.getDias().contains(DiaSemana.MIERCOLES));
        JCheckBox chkJ = new JCheckBox(htmlDia("J"), grupoSel.getDias().contains(DiaSemana.JUEVES));
        JCheckBox chkV = new JCheckBox(htmlDia("V"), grupoSel.getDias().contains(DiaSemana.VIERNES));
        for (JCheckBox chkDia : new JCheckBox[]{chkL, chkM, chkX, chkJ, chkV}) {
            chkDia.setForeground(FrutigerAeroUI.TEXTO_OSCURO);
            chkDia.setOpaque(false);
        }
        JPanel panelDiasEdit = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        panelDiasEdit.setOpaque(false);
        panelDiasEdit.add(chkL); panelDiasEdit.add(chkM); panelDiasEdit.add(chkX);
        panelDiasEdit.add(chkJ); panelDiasEdit.add(chkV);

        gbcG.gridx = 0; gbcG.gridy = 0; panelEditGrp.add(new JLabel("Cod. Grupo:"), gbcG);
        gbcG.gridx = 1; panelEditGrp.add(txtEditClaveGrp, gbcG);
        gbcG.gridx = 0; gbcG.gridy = 1; panelEditGrp.add(new JLabel("Profesor:"), gbcG);
        gbcG.gridx = 1; panelEditGrp.add(txtEditProfesor, gbcG);
        gbcG.gridx = 0; gbcG.gridy = 2; panelEditGrp.add(new JLabel("Hora Inicio Específica:"), gbcG);
        gbcG.gridx = 1; panelEditGrp.add(cbEditHora, gbcG);
        gbcG.gridx = 0; gbcG.gridy = 3; panelEditGrp.add(new JLabel("Dificultad Grupo/Profe (1-5):"), gbcG);
        gbcG.gridx = 1; panelEditGrp.add(spinnerEditDifGrp, gbcG);
        gbcG.gridx = 0; gbcG.gridy = 4; panelEditGrp.add(new JLabel("Días:"), gbcG);
        gbcG.gridx = 1; panelEditGrp.add(panelDiasEdit, gbcG);

        tabs.addTab("👥 Grupo: " + grupoSel.getClaveGrupo(), panelEditGrp);

        // -------------------------------------------------------
        // BOTONES DE ACCIÓN
        // -------------------------------------------------------
        JButton btnGuardar = new FrutigerAeroUI.BotonGlossy("Guardar Cambios", FrutigerAeroUI.VERDE_HOJA);
        JButton btnCancelar = new FrutigerAeroUI.BotonGlossy("Cancelar", FrutigerAeroUI.ROJO_CORAL);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        dialogo.add(tabs, BorderLayout.CENTER);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dialogo.dispose());
        btnGuardar.addActionListener(e -> {
            String nuevaClaveGrp = txtEditClaveGrp.getText().trim();
            String nuevoProf = txtEditProfesor.getText().trim();
            String nuevoNomMat = txtEditNombreMat.getText().trim();

            // Validación PREVIA (antes de tocar cualquier dato): la clave editada no
            // puede chocar con la de OTRO grupo ya existente en la misma materia
            // (se excluye al propio grupo que se está editando).
            if (!nuevaClaveGrp.isEmpty()) {
                for (Grupo otro : materiaSel.getGrupos()) {
                    if (otro != grupoSel && otro.getClaveGrupo().equalsIgnoreCase(nuevaClaveGrp)) {
                        JOptionPane.showMessageDialog(dialogo,
                                "Ya existe otro grupo [" + nuevaClaveGrp + "] en \"" + materiaSel.getNombre() + "\".\n" +
                                "Elige una clave distinta.",
                                "Grupo duplicado", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            // 1. Guardar cambios en la MATERIA
            if (!nuevoNomMat.isEmpty()) {
                materiaSel.setNombre(nuevoNomMat);
                materiaSel.setColor(colorEditMateria[0]);
                materiaSel.setSemestre((Integer) cbEditSemestreMat.getSelectedItem());
                materiaSel.setDificultad((Integer) spinnerEditDifMat.getValue());
                materiaSel.setCreditos((Integer) spinnerEditCreditos.getValue());
                String prereqElegido = (String) cbEditPrerreq.getSelectedItem();
                materiaSel.setPrerrequisito((prereqElegido == null || prereqElegido.equals("(Ninguno)")) ? "" : prereqElegido);
            }

            // 2. Guardar cambios en el GRUPO
            if (!nuevaClaveGrp.isEmpty()) {
                Set<DiaSemana> nuevosDias = EnumSet.noneOf(DiaSemana.class);
                if (chkL.isSelected()) nuevosDias.add(DiaSemana.LUNES);
                if (chkM.isSelected()) nuevosDias.add(DiaSemana.MARTES);
                if (chkX.isSelected()) nuevosDias.add(DiaSemana.MIERCOLES);
                if (chkJ.isSelected()) nuevosDias.add(DiaSemana.JUEVES);
                if (chkV.isSelected()) nuevosDias.add(DiaSemana.VIERNES);

                if (!nuevosDias.isEmpty()) {
                    grupoSel.setClaveGrupo(nuevaClaveGrp);
                    // Si el campo de profesor queda vacío, se guarda "-" (mismo criterio que el catálogo
                    // inicial) para que el renderizador lo muestre consistentemente como "Por definir"
                    // y la dificultad de ese grupo se mantenga en su valor neutral (por defecto 3).
                    grupoSel.setProfesor(nuevoProf.isEmpty() ? "-" : nuevoProf);
                    grupoSel.setHoraInicio(cbEditHora.getSelectedIndex() + 7); // Guarda hora exacta
                    grupoSel.setDias(nuevosDias);
                    grupoSel.setDificultad((Integer) spinnerEditDifGrp.getValue()); // Guarda dificultad específica
                }
            }

            // Persistir cambios al archivo .dat
            GestorPersistencia.guardar(materiasRegistradas, borradores);

            // Refrescar vistas
            actualizarCombosYListas();
            reconstruirPestanias();

            dialogo.dispose();
            JOptionPane.showMessageDialog(this, "Cambios guardados correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        });

        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    // =======================================================
    // ACCIONES GENERALES
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
            HorarioBorrador eliminado = borradores.get(index);
            int indiceRespaldo = index;
            registrarParaDeshacer("Eliminar borrador \"" + eliminado.getNombre() + "\"", () -> {
                int posicion = Math.min(indiceRespaldo, borradores.size());
                borradores.add(posicion, eliminado);
            });
            borradores.remove(index);
            GestorPersistencia.guardar(materiasRegistradas, borradores);
            reconstruirPestanias();
        }
    }

    /** Borra TODOS los borradores de un jalón (útil tras generar muchas opciones con Modo Rápido/Óptimo). */
    private void borrarTodosLosHorarios() {
        if (borradores.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay ningún borrador que borrar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "Vas a borrar los " + borradores.size() + " borrador(es) que tienes actualmente.\n" +
                "Se creará uno nuevo y vacío llamado \"Opción A\" para empezar de cero.\n\n" +
                "¿Confirmas? (Esto se puede deshacer con Ctrl+Z si te arrepientes)",
                "Borrar todos los horarios", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacion != JOptionPane.YES_OPTION) return;

        List<HorarioBorrador> respaldoCompleto = new ArrayList<>(borradores);
        registrarParaDeshacer("Borrar todos los horarios (" + respaldoCompleto.size() + " borrador(es))", () -> {
            borradores.clear();
            borradores.addAll(respaldoCompleto);
        });

        borradores.clear();
        borradores.add(new HorarioBorrador("Opción A"));
        GestorPersistencia.guardar(materiasRegistradas, borradores);
        reconstruirPestanias();
        tabbedPaneHorarios.setSelectedIndex(0);
    }

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

            BufferedImage imagen = capturarImagenPanel(tabbedPaneHorarios.getComponentAt(i));
            if (imagen == null) {
                errores.append("• ").append(borradores.get(i).getNombre()).append(" (tamaño inválido)\n");
                continue;
            }

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

    private void exportarHorariosPDF() {
        if (borradores.isEmpty() || tabbedPaneHorarios.getTabCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay borradores para exportar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecciona la carpeta destino para los PDF");
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

            BufferedImage imagen = capturarImagenPanel(tabbedPaneHorarios.getComponentAt(i));
            if (imagen == null) {
                errores.append("• ").append(borradores.get(i).getNombre()).append(" (tamaño inválido)\n");
                continue;
            }

            String nombreArchivo = sanitizarNombreArchivo(borradores.get(i).getNombre()) + ".pdf";
            File archivoDestino = new File(carpetaDestino, nombreArchivo);
            try {
                escribirImagenComoPDF(imagen, archivoDestino);
                exportados++;
            } catch (IOException ex) {
                errores.append("• ").append(borradores.get(i).getNombre()).append(" (").append(ex.getMessage()).append(")\n");
            }
        }

        tabbedPaneHorarios.setSelectedIndex(tabOriginal);

        StringBuilder mensaje = new StringBuilder();
        mensaje.append(exportados).append(" horario(s) exportado(s) a PDF en:\n").append(carpetaDestino.getAbsolutePath());
        if (errores.length() > 0) {
            mensaje.append("\n\nNo se pudieron exportar:\n").append(errores);
        }
        JOptionPane.showMessageDialog(this, mensaje.toString(), "Exportación de horarios (PDF)",
                errores.length() > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    /** Captura un panel (parrilla+resumen de un borrador) como imagen, igual que hace la exportación a JPG. */
    private BufferedImage capturarImagenPanel(Component panelTab) {
        int ancho = panelTab.getWidth();
        int alto = panelTab.getHeight();
        if (ancho <= 0 || alto <= 0) return null;

        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = imagen.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, ancho, alto);
        panelTab.printAll(g2);
        g2.dispose();
        return imagen;
    }

    /**
     * Escribe un PDF de una sola página que envuelve la imagen dada, SIN usar ninguna
     * librería externa (nada de iText/PDFBox que haya que descargar): construye a mano
     * los objetos mínimos del formato PDF y embebe el JPEG directamente con el filtro
     * /DCTDecode, que es justo para eso: incrustar JPEGs "tal cual" dentro de un PDF.
     */
    private void escribirImagenComoPDF(BufferedImage imagen, File destino) throws IOException {
        ByteArrayOutputStream bufferJpeg = new ByteArrayOutputStream();
        ImageIO.write(imagen, "jpg", bufferJpeg);
        byte[] datosJpeg = bufferJpeg.toByteArray();

        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        List<Integer> posiciones = new ArrayList<>(); // offset de cada objeto, en orden 1..5

        pdf.write("%PDF-1.4\n".getBytes("ISO-8859-1"));

        posiciones.add(pdf.size());
        pdf.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes("ISO-8859-1"));

        posiciones.add(pdf.size());
        pdf.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes("ISO-8859-1"));

        posiciones.add(pdf.size());
        String pagina = "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + ancho + " " + alto + "] "
                + "/Resources << /XObject << /Im0 5 0 R >> >> /Contents 4 0 R >>\nendobj\n";
        pdf.write(pagina.getBytes("ISO-8859-1"));

        // Stream de contenido: dibuja la imagen ocupando toda la página (matriz de transformación = ancho/alto)
        String contenido = "q " + ancho + " 0 0 " + alto + " 0 0 cm /Im0 Do Q";
        String obj4 = "4 0 obj\n<< /Length " + contenido.getBytes("ISO-8859-1").length + " >>\nstream\n"
                + contenido + "\nendstream\nendobj\n";
        posiciones.add(pdf.size());
        pdf.write(obj4.getBytes("ISO-8859-1"));

        // Imagen embebida directamente como JPEG (DCTDecode = "tal cual, sin recomprimir")
        posiciones.add(pdf.size());
        String encabezadoImg = "5 0 obj\n<< /Type /XObject /Subtype /Image /Width " + ancho + " /Height " + alto
                + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length " + datosJpeg.length + " >>\nstream\n";
        pdf.write(encabezadoImg.getBytes("ISO-8859-1"));
        pdf.write(datosJpeg);
        pdf.write("\nendstream\nendobj\n".getBytes("ISO-8859-1"));

        int posXref = pdf.size();
        pdf.write("xref\n0 6\n0000000000 65535 f \n".getBytes("ISO-8859-1"));
        for (int offset : posiciones) {
            pdf.write(String.format("%010d 00000 n \n", offset).getBytes("ISO-8859-1"));
        }

        pdf.write(("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + posXref + "\n%%EOF").getBytes("ISO-8859-1"));

        try (FileOutputStream fos = new FileOutputStream(destino)) {
            pdf.writeTo(fos);
        }
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

        if (seleccionado.getMateriaPadre() != null
                && !confirmarPrerrequisitos(Collections.singletonList(seleccionado.getMateriaPadre()))) {
            return; // el usuario decidió cancelar tras ver el aviso de prerrequisito
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

        if (!removidos.isEmpty()) {
            List<Grupo> removidosRespaldo = new ArrayList<>(removidos);
            Grupo agregadoRespaldo = seleccionado;
            registrarParaDeshacer("Reemplazo de " + removidosRespaldo.size() + " grupo(s) en \"" + borradorActivo.getNombre() + "\"", () -> {
                activos.remove(agregadoRespaldo);
                activos.addAll(removidosRespaldo);
            });
        }

        GestorPersistencia.guardar(materiasRegistradas, borradores);

        JPanel panelTab = (JPanel) tabbedPaneHorarios.getComponentAt(indexTab);
        JSplitPane splitVer = (JSplitPane) panelTab.getComponent(0);
        JScrollPane scrollHorario = (JScrollPane) splitVer.getTopComponent();
        JScrollPane scrollResumen = (JScrollPane) splitVer.getBottomComponent();

        JTable tablaH = (JTable) scrollHorario.getViewport().getView();
        JTable tablaR = (JTable) scrollResumen.getViewport().getView();

        renderizarParrillaYResumen(borradorActivo, (DefaultTableModel) tablaH.getModel(), (DefaultTableModel) tablaR.getModel());

        Component lblEncontrado = buscarComponentePorNombre(panelTab, "lblResumenCarga");
        if (lblEncontrado instanceof JLabel) {
            actualizarResumenCarga((JLabel) lblEncontrado, borradorActivo);
        }

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

        listGrupos.repaint();

        String estado = seleccionado.isDisponible() ? "DISPONIBLE" : "LLENO";
        JOptionPane.showMessageDialog(this,
                seleccionado.getMateriaPadre().getNombre() + " [" + seleccionado.getClaveGrupo() + "] ahora está: " + estado,
                "Disponibilidad actualizada", JOptionPane.INFORMATION_MESSAGE);
    }

    // =======================================================
    // MODO RÁPIDO & HORARIO ÓPTIMO
    // =======================================================

    private static final int MAX_MATERIAS_SELECCIONABLES = 8;
    private static final int MAX_HORARIOS_GENERABLES = 5;
    private static final int MAX_HORAS_LIBRES_PERMITIDAS = 5;
    private static final int LIMITE_EXPLORACION_OPTIMO = 200_000;
    private static final int HORA_LIMITE_MATUTINO = 14; // antes de las 14:00 = matutino, de 14:00 en adelante = vespertino

    private enum FuenteDificultad { GRUPO, MATERIA, AMBAS }
    private enum EstrategiaBalance { HUECOS_PRIMERO, DIFICULTAD_PRIMERO, INTERMEDIO }
    private enum Turno { MATUTINO, VESPERTINO, MIXTO }

    private boolean coincideConTurno(Grupo g, Turno turno) {
        if (turno == Turno.MIXTO) return true;
        boolean esMatutino = g.getHoraInicio() < HORA_LIMITE_MATUTINO;
        return turno == Turno.MATUTINO ? esMatutino : !esMatutino;
    }

    /**
     * Para cada materia (mismo orden que la lista recibida) arma la lista de
     * grupos candidatos: solo los disponibles Y que coinciden con el turno
     * elegido. El índice de la lista externa corresponde 1 a 1 con 'materias'.
     */
    private List<List<Grupo>> obtenerGruposFiltradosPorTurno(List<Materia> materias, Turno turno) {
        List<List<Grupo>> resultado = new ArrayList<>();
        for (Materia m : materias) {
            List<Grupo> candidatos = new ArrayList<>();
            for (Grupo g : m.getGrupos()) {
                if (g.isDisponible() && coincideConTurno(g, turno)) candidatos.add(g);
            }
            resultado.add(candidatos);
        }
        return resultado;
    }

    /** Construye el panel de selección de turno reutilizable para ambos diálogos. */
    private JPanel construirPanelTurno(String numeroSeccion, JRadioButton[] radiosOut) {
        JPanel panelTurno = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTurno.setOpaque(false);
        panelTurno.setBorder(FrutigerAeroUI.bordeTitulado(numeroSeccion + ". ¿Qué turno prefieres?"));
        JRadioButton radioMatutino = new JRadioButton("Matutino (antes de las " + HORA_LIMITE_MATUTINO + ":00)");
        JRadioButton radioVespertino = new JRadioButton("Vespertino (" + HORA_LIMITE_MATUTINO + ":00 en adelante)");
        JRadioButton radioMixto = new JRadioButton("Mixto (ambos)", true);
        radioMatutino.setOpaque(false);
        radioVespertino.setOpaque(false);
        radioMixto.setOpaque(false);
        ButtonGroup grupoTurno = new ButtonGroup();
        grupoTurno.add(radioMatutino);
        grupoTurno.add(radioVespertino);
        grupoTurno.add(radioMixto);
        panelTurno.add(radioMatutino);
        panelTurno.add(radioVespertino);
        panelTurno.add(radioMixto);
        radiosOut[0] = radioMatutino;
        radiosOut[1] = radioVespertino;
        radiosOut[2] = radioMixto;
        return panelTurno;
    }

    /**
     * Crea un combobox de filtro de semestre reutilizable para las listas de
     * materias con checkboxes (Modo Rápido / Horario Óptimo). El valor
     * {@code null} representa "Todos los semestres".
     */
    private JComboBox<Integer> crearComboFiltroSemestre() {
        Integer[] opciones = new Integer[11];
        for (int s = 1; s <= 10; s++) opciones[s] = s; // opciones[0] queda en null = "Todos"

        JComboBox<Integer> combo = new JComboBox<>(opciones);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "Todos los semestres" : "Semestre " + value);
                return this;
            }
        });
        return combo;
    }

    /**
     * Muestra u oculta cada checkbox de materia según el semestre elegido en
     * el filtro. {@code semestre == null} significa "mostrar todas".
     * Las materias ocultas conservan su selección (no se deseleccionan).
     */
    private void filtrarCheckboxesPorSemestre(List<JCheckBox> checkboxes, Integer semestre) {
        for (JCheckBox chk : checkboxes) {
            Materia m = (Materia) chk.getClientProperty("materia");
            boolean visible = (semestre == null) || (m != null && m.getSemestre() == semestre);
            chk.setVisible(visible);
        }
    }

    private static class CombinacionEvaluada {
        List<Grupo> grupos;
        int huecos;
        int dificultad;
    }

    private void mostrarDialogoModoRapido() {
        mostrarDialogoModoRapido(null, false);
    }

    private void mostrarDialogoModoRapido(Set<String> preseleccionar, boolean forzarUsarBorradorActual) {
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

        JPanel panelMaterias = new JPanel();
        panelMaterias.setOpaque(false);
        panelMaterias.setLayout(new BoxLayout(panelMaterias, BoxLayout.Y_AXIS));
        JLabel lblContador = new JLabel("Seleccionadas: 0 / " + MAX_MATERIAS_SELECCIONABLES);

        List<JCheckBox> checkboxes = new ArrayList<>();
        for (Materia m : materiasRegistradas) {
            JCheckBox chk = new JCheckBox(m.getNombre() + " (Sem. " + m.getSemestre() + " - " + m.getGrupos().size() + " grupos)");
            chk.setOpaque(false);
            chk.setForeground(FrutigerAeroUI.TEXTO_OSCURO);
            chk.putClientProperty("materia", m);
            if (preseleccionar != null && preseleccionar.contains(m.getNombre())) chk.setSelected(true);
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
        if (preseleccionar != null && !preseleccionar.isEmpty()) {
            lblContador.setText("Seleccionadas: " + checkboxes.stream().filter(JCheckBox::isSelected).count()
                    + " / " + MAX_MATERIAS_SELECCIONABLES);
        }

        JComboBox<Integer> cbFiltroSemestreLista = crearComboFiltroSemestre();
        cbFiltroSemestreLista.addActionListener(e ->
                filtrarCheckboxesPorSemestre(checkboxes, (Integer) cbFiltroSemestreLista.getSelectedItem()));

        JPanel panelFiltroSemestre = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelFiltroSemestre.setOpaque(false);
        panelFiltroSemestre.add(new JLabel("Filtrar por semestre:"));
        panelFiltroSemestre.add(cbFiltroSemestreLista);

        JScrollPane scrollMaterias = new JScrollPane(panelMaterias);
        scrollMaterias.getViewport().setOpaque(false);
        scrollMaterias.setOpaque(false);
        scrollMaterias.setPreferredSize(new Dimension(360, 220));
        scrollMaterias.setBorder(FrutigerAeroUI.bordeTitulado("1. ¿Qué materias piensas cursar? (máx. " + MAX_MATERIAS_SELECCIONABLES + ")"));

        JPanel panelSeleccion = new JPanel(new BorderLayout());
        panelSeleccion.setOpaque(false);
        panelSeleccion.add(panelFiltroSemestre, BorderLayout.NORTH);
        panelSeleccion.add(scrollMaterias, BorderLayout.CENTER);
        panelSeleccion.add(lblContador, BorderLayout.SOUTH);

        JRadioButton[] radiosTurnoRapido = new JRadioButton[3];
        JPanel panelTurno = construirPanelTurno("2", radiosTurnoRapido);

        JPanel panelCantidad = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCantidad.setOpaque(false);
        panelCantidad.setBorder(FrutigerAeroUI.bordeTitulado("3. ¿Cuántos horarios distintos quieres generar?"));
        JSpinner spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, MAX_HORARIOS_GENERABLES, 1));
        panelCantidad.add(new JLabel("Cantidad (máx. " + MAX_HORARIOS_GENERABLES + "):"));
        panelCantidad.add(spinnerCantidad);

        JPanel panelHuecos = new JPanel(new BorderLayout(5, 0));
        panelHuecos.setOpaque(false);
        panelHuecos.setBorder(FrutigerAeroUI.bordeTitulado("4. ¿Cuántas horas libres al día toleras como máximo?"));
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

        JPanel panelDestino = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelDestino.setOpaque(false);
        panelDestino.setBorder(FrutigerAeroUI.bordeTitulado("5. ¿Dónde quieres el/los resultado(s)?"));
        JRadioButton radioNuevo = new JRadioButton("Crear borrador(es) nuevo(s)", !forzarUsarBorradorActual);
        JRadioButton radioActual = new JRadioButton("Llenar el borrador actualmente abierto (solo 1 horario)", forzarUsarBorradorActual);
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
        panelCentro.add(panelTurno);
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

            if (!confirmarPrerrequisitos(seleccionadas)) return;

            int cantidad = (Integer) spinnerCantidad.getValue();
            boolean usarActual = radioActual.isSelected();
            if (usarActual) cantidad = 1;
            int limiteHuecos = sliderHuecos.getValue();
            Turno turno = radiosTurnoRapido[0].isSelected() ? Turno.MATUTINO
                    : radiosTurnoRapido[1].isSelected() ? Turno.VESPERTINO
                    : Turno.MIXTO;

            dialogo.dispose();
            ejecutarModoRapido(seleccionadas, cantidad, usarActual, limiteHuecos, turno);
        });

        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    private void ejecutarModoRapido(List<Materia> materiasSeleccionadas, int cantidad, boolean usarBorradorActual, int limiteHuecos, Turno turno) {
        List<String> materiasSinGruposDisponibles = new ArrayList<>();
        List<Materia> materiasConGrupos = new ArrayList<>();

        for (Materia m : materiasSeleccionadas) {
            boolean tieneDisponible = false;
            for (Grupo g : m.getGrupos()) {
                if (g.isDisponible() && coincideConTurno(g, turno)) { tieneDisponible = true; break; }
            }
            if (tieneDisponible) materiasConGrupos.add(m);
            else materiasSinGruposDisponibles.add(m.getNombre());
        }

        if (materiasConGrupos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ninguna de las materias seleccionadas tiene grupos disponibles en el turno elegido.",
                    "No se pudo generar", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<List<Grupo>> gruposPorMateria = obtenerGruposFiltradosPorTurno(materiasConGrupos, turno);
        List<List<Grupo>> combinaciones = generarCombinacionesRapidas(gruposPorMateria, cantidad, limiteHuecos);

        if (combinaciones.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No se encontró ningún horario sin choques con un máximo de " + limiteHuecos + " hora(s) libre(s) al día"
                    + (turno != Turno.MIXTO ? " en el turno seleccionado" : "") + ".\n" +
                    "Sugerencia: sube el control de 'horas libres máximas', prueba con turno Mixto, o revisa si hay grupos " +
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

    private List<List<Grupo>> generarCombinacionesRapidas(List<List<Grupo>> gruposPorMateria, int cantidadDeseada, int limiteHuecos) {
        List<List<Grupo>> resultados = new ArrayList<>();
        Set<String> firmasVistas = new HashSet<>();
        Random rnd = new Random();

        int intentosMax = 500;
        for (int intento = 0; intento < intentosMax && resultados.size() < cantidadDeseada; intento++) {
            List<Grupo> combinacion = new ArrayList<>();
            boolean encontrada = backtrackCombinacion(gruposPorMateria, 0, combinacion, rnd, limiteHuecos);
            if (encontrada) {
                String firma = firmaDeCombinacion(combinacion);
                if (firmasVistas.add(firma)) {
                    resultados.add(new ArrayList<>(combinacion));
                }
            }
        }
        return resultados;
    }

    private boolean backtrackCombinacion(List<List<Grupo>> gruposPorMateria, int indice, List<Grupo> actual, Random rnd, int limiteHuecos) {
        if (indice == gruposPorMateria.size()) {
            return calcularHorasMuertas(actual) <= limiteHuecos;
        }

        List<Grupo> candidatos = new ArrayList<>(gruposPorMateria.get(indice));
        Collections.shuffle(candidatos, rnd);

        for (Grupo g : candidatos) {
            boolean choca = false;
            for (Grupo existente : actual) {
                if (existente.chocaCon(g)) { choca = true; break; }
            }
            if (!choca) {
                actual.add(g);
                if (backtrackCombinacion(gruposPorMateria, indice + 1, actual, rnd, limiteHuecos)) return true;
                actual.remove(actual.size() - 1);
            }
        }
        return false;
    }

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

    private void mostrarDialogoHorarioOptimo() {
        mostrarDialogoHorarioOptimo(null, false);
    }

    private void mostrarDialogoHorarioOptimo(Set<String> preseleccionar, boolean forzarUsarBorradorActual) {
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

        JPanel panelMaterias = new JPanel();
        panelMaterias.setOpaque(false);
        panelMaterias.setLayout(new BoxLayout(panelMaterias, BoxLayout.Y_AXIS));
        JLabel lblContador = new JLabel("Seleccionadas: 0 / " + MAX_MATERIAS_SELECCIONABLES);

        List<JCheckBox> checkboxes = new ArrayList<>();
        for (Materia m : materiasRegistradas) {
            JCheckBox chk = new JCheckBox(m.getNombre() + " (Sem. " + m.getSemestre() + " - " + m.getGrupos().size() + " grupos, dif. materia: " + m.getDificultad() + ")");
            chk.setOpaque(false);
            chk.setForeground(FrutigerAeroUI.TEXTO_OSCURO);
            chk.putClientProperty("materia", m);
            if (preseleccionar != null && preseleccionar.contains(m.getNombre())) chk.setSelected(true);
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
        if (preseleccionar != null && !preseleccionar.isEmpty()) {
            lblContador.setText("Seleccionadas: " + checkboxes.stream().filter(JCheckBox::isSelected).count()
                    + " / " + MAX_MATERIAS_SELECCIONABLES);
        }

        JComboBox<Integer> cbFiltroSemestreLista = crearComboFiltroSemestre();
        cbFiltroSemestreLista.addActionListener(e ->
                filtrarCheckboxesPorSemestre(checkboxes, (Integer) cbFiltroSemestreLista.getSelectedItem()));

        JPanel panelFiltroSemestre = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelFiltroSemestre.setOpaque(false);
        panelFiltroSemestre.add(new JLabel("Filtrar por semestre:"));
        panelFiltroSemestre.add(cbFiltroSemestreLista);

        JScrollPane scrollMaterias = new JScrollPane(panelMaterias);
        scrollMaterias.setPreferredSize(new Dimension(400, 200));
        scrollMaterias.getViewport().setOpaque(false);
        scrollMaterias.setOpaque(false);
        scrollMaterias.setBorder(FrutigerAeroUI.bordeTitulado("1. ¿Qué materias piensas cursar? (máx. " + MAX_MATERIAS_SELECCIONABLES + ")"));

        JPanel panelSeleccion = new JPanel(new BorderLayout());
        panelSeleccion.setOpaque(false);
        panelSeleccion.add(panelFiltroSemestre, BorderLayout.NORTH);
        panelSeleccion.add(scrollMaterias, BorderLayout.CENTER);
        panelSeleccion.add(lblContador, BorderLayout.SOUTH);

        JRadioButton[] radiosTurnoOptimo = new JRadioButton[3];
        JPanel panelTurno = construirPanelTurno("2", radiosTurnoOptimo);

        JPanel panelCantidad = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCantidad.setOpaque(false);
        panelCantidad.setBorder(FrutigerAeroUI.bordeTitulado("3. ¿Cuántas de las mejores opciones quieres ver?"));
        JSpinner spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, MAX_HORARIOS_GENERABLES, 1));
        panelCantidad.add(new JLabel("Cantidad (máx. " + MAX_HORARIOS_GENERABLES + "):"));
        panelCantidad.add(spinnerCantidad);

        JPanel panelFuente = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFuente.setOpaque(false);
        panelFuente.setBorder(FrutigerAeroUI.bordeTitulado("4. ¿Dónde se toma la dificultad?"));
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

        JPanel panelEstrategia = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEstrategia.setOpaque(false);
        panelEstrategia.setBorder(FrutigerAeroUI.bordeTitulado("5. ¿Cómo se balancean huecos vs. dificultad?"));
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

        JPanel panelHuecos = new JPanel(new BorderLayout(5, 0));
        panelHuecos.setOpaque(false);
        panelHuecos.setBorder(FrutigerAeroUI.bordeTitulado("6. Límite de horas libres (opcional)"));

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

        JPanel panelDestino = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelDestino.setOpaque(false);
        panelDestino.setBorder(FrutigerAeroUI.bordeTitulado("7. ¿Dónde quieres el/los resultado(s)?"));
        JRadioButton radioNuevo = new JRadioButton("Crear borrador(es) nuevo(s)", !forzarUsarBorradorActual);
        JRadioButton radioActual = new JRadioButton("Llenar el borrador actualmente abierto (solo el mejor)", forzarUsarBorradorActual);
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
        panelCentro.add(panelTurno);
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

            if (!confirmarPrerrequisitos(seleccionadas)) return;

            int cantidad = (Integer) spinnerCantidad.getValue();
            boolean usarActual = radioActual.isSelected();
            if (usarActual) cantidad = 1;

            FuenteDificultad fuente = radioFuenteMateria.isSelected() ? FuenteDificultad.MATERIA
                    : radioFuenteAmbas.isSelected() ? FuenteDificultad.AMBAS
                    : FuenteDificultad.GRUPO;

            EstrategiaBalance estrategia = radioDificultadPrimero.isSelected() ? EstrategiaBalance.DIFICULTAD_PRIMERO
                    : radioIntermedio.isSelected() ? EstrategiaBalance.INTERMEDIO
                    : EstrategiaBalance.HUECOS_PRIMERO;

            boolean limitarHuecos = chkLimitarHuecos.isSelected();
            int limiteHuecos = sliderHuecosOptimo.getValue();
            Turno turno = radiosTurnoOptimo[0].isSelected() ? Turno.MATUTINO
                    : radiosTurnoOptimo[1].isSelected() ? Turno.VESPERTINO
                    : Turno.MIXTO;

            dialogo.dispose();
            ejecutarHorarioOptimo(seleccionadas, cantidad, usarActual, fuente, estrategia, limitarHuecos, limiteHuecos, turno);
        });

        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    private void ejecutarHorarioOptimo(List<Materia> materiasSeleccionadas, int cantidad, boolean usarBorradorActual,
                                        FuenteDificultad fuente, EstrategiaBalance estrategia,
                                        boolean limitarHuecos, int limiteHuecos, Turno turno) {
        List<String> materiasSinGruposDisponibles = new ArrayList<>();
        List<Materia> materiasConGrupos = new ArrayList<>();

        for (Materia m : materiasSeleccionadas) {
            boolean tieneDisponible = false;
            for (Grupo g : m.getGrupos()) {
                if (g.isDisponible() && coincideConTurno(g, turno)) { tieneDisponible = true; break; }
            }
            if (tieneDisponible) materiasConGrupos.add(m);
            else materiasSinGruposDisponibles.add(m.getNombre());
        }

        if (materiasConGrupos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ninguna de las materias seleccionadas tiene grupos disponibles en el turno elegido.",
                    "No se pudo generar", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<List<Grupo>> gruposPorMateria = obtenerGruposFiltradosPorTurno(materiasConGrupos, turno);
        List<List<Grupo>> todasLasCombinaciones = new ArrayList<>();
        int[] contadorExploracion = {0};
        enumerarCombinacionesValidas(gruposPorMateria, 0, new ArrayList<>(), todasLasCombinaciones, contadorExploracion);

        if (todasLasCombinaciones.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No fue posible encontrar ningún horario sin choques con las materias seleccionadas"
                    + (turno != Turno.MIXTO ? " en el turno elegido" : "") + ".\n" +
                    "Prueba quitando alguna materia, cambiando el turno a Mixto, o liberando algún grupo marcado como lleno.",
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

    private void enumerarCombinacionesValidas(List<List<Grupo>> gruposPorMateria, int indice, List<Grupo> actual,
                                               List<List<Grupo>> resultados, int[] contadorExploracion) {
        if (contadorExploracion[0] >= LIMITE_EXPLORACION_OPTIMO) return;

        if (indice == gruposPorMateria.size()) {
            resultados.add(new ArrayList<>(actual));
            contadorExploracion[0]++;
            return;
        }

        for (Grupo g : gruposPorMateria.get(indice)) {
            boolean choca = false;
            for (Grupo existente : actual) {
                if (existente.chocaCon(g)) { choca = true; break; }
            }
            if (!choca) {
                actual.add(g);
                enumerarCombinacionesValidas(gruposPorMateria, indice + 1, actual, resultados, contadorExploracion);
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

        int semestre = (Integer) cbSemestreMateria.getSelectedItem();
        Materia m = new Materia(nombre, colorSeleccionado, semestre);
        m.setDificultad((Integer) spinnerDificultadMateria.getValue());
        m.setCreditos((Integer) spinnerCreditosMateria.getValue());
        String prereqSeleccionado = (String) cbPrerrequisitoMateria.getSelectedItem();
        m.setPrerrequisito((prereqSeleccionado == null || prereqSeleccionado.equals("(Ninguno)")) ? "" : prereqSeleccionado);
        materiasRegistradas.add(m);
        GestorPersistencia.guardar(materiasRegistradas, borradores);

        txtNombreMateria.setText("");
        spinnerDificultadMateria.setValue(3);
        spinnerCreditosMateria.setValue(5);

        cbFiltroSemestreGrupo.setSelectedItem(semestre);
        actualizarCombosYListas();
    }

    private void agregarGrupoAMateria() {
        Materia materiaSeleccionada = (Materia) cbMateriasExistentes.getSelectedItem();
        if (materiaSeleccionada == null) return;

        String clave = txtClaveGrupo.getText().trim();
        String prof = txtProfesor.getText().trim();
        if (clave.isEmpty()) return;

        // Validación: no permitir el mismo grupo (misma clave) dos veces en la misma materia
        for (Grupo existente : materiaSeleccionada.getGrupos()) {
            if (existente.getClaveGrupo().equalsIgnoreCase(clave)) {
                JOptionPane.showMessageDialog(this,
                        "El grupo [" + clave + "] ya está registrado en \"" + materiaSeleccionada.getNombre() + "\".\n" +
                        "Usa '⚙ Editar Selección' si quieres modificar sus datos, o registra este con otra clave.",
                        "Grupo duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        Set<DiaSemana> dias = EnumSet.noneOf(DiaSemana.class);
        if (chkLunes.isSelected()) dias.add(DiaSemana.LUNES);
        if (chkMartes.isSelected()) dias.add(DiaSemana.MARTES);
        if (chkMiercoles.isSelected()) dias.add(DiaSemana.MIERCOLES);
        if (chkJueves.isSelected()) dias.add(DiaSemana.JUEVES);
        if (chkViernes.isSelected()) dias.add(DiaSemana.VIERNES);

        if (dias.isEmpty()) return;

        int hInicio = cbHoraInicio.getSelectedIndex() + 7;
        // Si el profesor se deja en blanco, se guarda como "-" (mismo criterio que el catálogo
        // inicial y que la edición posterior): el catálogo lo mostrará como "Por definir" y,
        // como no hay ningún profesor sobre el que juzgar, la dificultad se queda en su
        // valor neutral por defecto (3 = media) salvo que el usuario la cambie a mano.
        Grupo nuevoGrupo = new Grupo(clave, prof.isEmpty() ? "-" : prof, hInicio, dias);
        nuevoGrupo.setDificultad((Integer) spinnerDificultadGrupo.getValue());
        materiaSeleccionada.agregarGrupo(nuevoGrupo);

        GestorPersistencia.guardar(materiasRegistradas, borradores);
        actualizarCombosYListas();

        txtClaveGrupo.setText("");
        txtProfesor.setText("");
        spinnerDificultadGrupo.setValue(3);
    }

    private void actualizarMateriasPorSemestre() {
        if (cbFiltroSemestreGrupo == null || cbFiltroSemestreGrupo.getSelectedItem() == null) return;

        int semestreSeleccionado = (Integer) cbFiltroSemestreGrupo.getSelectedItem();
        cbMateriasExistentes.removeAllItems();

        for (Materia m : materiasRegistradas) {
            if (m.getSemestre() == semestreSeleccionado) {
                cbMateriasExistentes.addItem(m);
            }
        }
    }

    private void actualizarCombosYListas() {
        actualizarMateriasPorSemestre();
        actualizarListaGrupos();
        actualizarComboPrerrequisitos();
    }

    /** Repuebla el combo de "Prerrequisito" del formulario de Crear Materia con los nombres actuales. */
    private void actualizarComboPrerrequisitos() {
        if (cbPrerrequisitoMateria == null) return;
        Object seleccionPrevia = cbPrerrequisitoMateria.getSelectedItem();
        cbPrerrequisitoMateria.removeAllItems();
        cbPrerrequisitoMateria.addItem("(Ninguno)");
        List<Materia> ordenadas = new ArrayList<>(materiasRegistradas);
        ordenadas.sort(Comparator.comparingInt(Materia::getSemestre).thenComparing(Materia::getNombre));
        for (Materia m : ordenadas) cbPrerrequisitoMateria.addItem(m.getNombre());
        if (seleccionPrevia != null) cbPrerrequisitoMateria.setSelectedItem(seleccionPrevia);
    }

    /**
     * Repuebla el catálogo de grupos (listGrupos) respetando el filtro de
     * semestre elegido en cbFiltroSemestreCatalogo. Si el filtro está en
     * "Todos los semestres" (null), se muestran los grupos de todas las materias.
     */
    private void actualizarListaGrupos() {
        Integer semestreFiltro = (cbFiltroSemestreCatalogo != null)
                ? (Integer) cbFiltroSemestreCatalogo.getSelectedItem()
                : null;

        modelListaGrupos.clear();
        for (Materia m : materiasRegistradas) {
            if (semestreFiltro != null && m.getSemestre() != semestreFiltro) continue;
            for (Grupo g : m.getGrupos()) {
                modelListaGrupos.addElement(g);
            }
        }
    }

    /**
     * Envuelve la letra de un día (L, M, X, J, V) en HTML con color forzado.
     * Esto pinta el texto del JCheckBox usando el renderer de HTML de Swing,
     * que respeta el color indicado sin importar el Look & Feel activo
     * (a diferencia de setForeground(), que algunos Look & Feel basados en
     * Synth —p. ej. GTK en Linux— pueden ignorar).
     */
    private String htmlDia(String letra) {
        return "<html><font color='#193C55'>" + letra + "</font></html>";
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
            c.setFont(c.getFont().deriveFont(Font.PLAIN));

            if (value instanceof Grupo) {
                Grupo g = (Grupo) value;
                Materia m = g.getMateriaPadre();

                boolean profesorSinDefinir = g.getProfesor() == null || g.getProfesor().trim().isEmpty()
                        || g.getProfesor().trim().equals("-");
                String nombreProfesor = profesorSinDefinir ? "Por definir" : g.getProfesor();

                String textoBase = m != null
                        ? m.getNombre() + " (Sem." + m.getSemestre() + ") [" + g.getClaveGrupo() + "] - " + nombreProfesor
                            + " (" + g.getHoraInicio() + ":00 hrs) - Dif.Grupo:" + g.getDificultad() + " Dif.Mat:" + m.getDificultad()
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
            // Se usa el Look & Feel "cross-platform" (Metal) en vez del del sistema:
            // en Linux con temas GTK, el Look & Feel del sistema es "Synth" y puede
            // IGNORAR setForeground() en componentes como JCheckBox (pinta el texto
            // según el tema del SO, no según el componente), causando texto invisible
            // (blanco sobre blanco). Metal siempre respeta los colores que definimos.
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}
            new VistaPrincipal().setVisible(true);
        });
    }
}