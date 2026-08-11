package persistencia;

import modelo.DiaSemana;
import modelo.Grupo;
import modelo.HorarioBorrador;
import modelo.Materia;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class CargadorDatos {

    public static void main(String[] args) {
        List<Materia> catalogo = new ArrayList<>();
        
        // Se preservan los borradores existentes al reescribir el catálogo
        List<HorarioBorrador> borradores = GestorPersistencia.cargarBorradores();

        // =========================================================================
        // 1. CALCULO VECTORIAL (Azul Marino / Azul Oscuro) - Semestre 3
        // =========================================================================
        Materia calculoVectorial = new Materia("CALC. VECTORIAL", new Color(41, 128, 185), 3);
        calculoVectorial.setDificultad(3);
        calculoVectorial.setCreditos(5);

        calculoVectorial.agregarGrupo(crearGrupo("3SA", "-", 7, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        calculoVectorial.agregarGrupo(crearGrupo("3SB", "HERNANDEZ RODRIGUEZ JAVIER", 8, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        calculoVectorial.agregarGrupo(crearGrupo("3SC", "HERNANDEZ RODRIGUEZ JAVIER", 14, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        calculoVectorial.agregarGrupo(crearGrupo("3SD", "BERNABÉ ANDRÉS ALICIA", 10, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        calculoVectorial.agregarGrupo(crearGrupo("3SE", "PEREZ MENDOZA FERNANDO", 13, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        calculoVectorial.agregarGrupo(crearGrupo("3SF", "-", 15, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));

        catalogo.add(calculoVectorial);

        // =========================================================================
        // 2. SISTEMAS OPERATIVOS (Cian / Turquesa) - Semestre 3
        // =========================================================================
        Materia sistemasOperativos = new Materia("SIST. OPERATIVOS", new Color(22, 160, 133), 3);
        sistemasOperativos.setDificultad(2);
        sistemasOperativos.setCreditos(4);

        sistemasOperativos.agregarGrupo(crearGrupo("3SA", "JIMENEZ HALLA JOHANN FRANCISCO", 12, 4, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        sistemasOperativos.agregarGrupo(crearGrupo("3SB", "JIMENEZ HALLA JOHANN FRANCISCO", 13, 4, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        sistemasOperativos.agregarGrupo(crearGrupo("3SD", "-", 8, 3, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        sistemasOperativos.agregarGrupo(crearGrupo("3SE", "-", 12, 5, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        sistemasOperativos.agregarGrupo(crearGrupo("3SF", "JIMENEZ HALLA JOHANN FRANCISCO", 19, 4, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        sistemasOperativos.agregarGrupo(crearGrupo("3SR", "-", 13, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));

        catalogo.add(sistemasOperativos);

        // =========================================================================
        // 3. CULTURA EMPRESARIAL (Naranja Ámbar) - Semestre 3
        // =========================================================================
        Materia culturaEmpresarial = new Materia("CULTURA EMPRESARIAL", new Color(230, 126, 34), 3);
        culturaEmpresarial.setDificultad(2);
        culturaEmpresarial.setCreditos(4);

        culturaEmpresarial.agregarGrupo(crearGrupo("3SA", "-", 13, 3, DiaSemana.LUNES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        culturaEmpresarial.agregarGrupo(crearGrupo("3SB", "-", 14, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        culturaEmpresarial.agregarGrupo(crearGrupo("3SC", "-", 12, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        culturaEmpresarial.agregarGrupo(crearGrupo("3SD", "-", 11, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.VIERNES));
        culturaEmpresarial.agregarGrupo(crearGrupo("3SE", "-", 8, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.VIERNES));

        catalogo.add(culturaEmpresarial);

        // =========================================================================
        // 4. INVESTIGACION DE OPERACIONES (Verde Esmeralda) - Semestre 3
        // =========================================================================
        Materia invOperaciones = new Materia("INV. DE OPERACIONES", new Color(46, 204, 113), 3);
        invOperaciones.setDificultad(2);
        invOperaciones.setCreditos(4);

        invOperaciones.agregarGrupo(crearGrupo("3SA", "-", 11, 3, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        invOperaciones.agregarGrupo(crearGrupo("3SB", "-", 12, 3, DiaSemana.LUNES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        invOperaciones.agregarGrupo(crearGrupo("3SC", "-", 10, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        invOperaciones.agregarGrupo(crearGrupo("3SD", "-", 13, 3, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        invOperaciones.agregarGrupo(crearGrupo("3SE", "-", 9, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        invOperaciones.agregarGrupo(crearGrupo("3SF", "-", 17, 3, DiaSemana.LUNES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));

        catalogo.add(invOperaciones);
        
        // =========================================================================
        // 5. ESTRUCTURA DE DATOS (Azul Eléctrico) - Semestre 3
        // =========================================================================
        Materia estructuraDatos = new Materia("ESTRUCTURA DE DATOS", new Color(52, 152, 219), 3);
        estructuraDatos.setDificultad(3);
        estructuraDatos.setCreditos(5);

        estructuraDatos.agregarGrupo(crearGrupo("3SA", "-", 9, 2, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        estructuraDatos.agregarGrupo(crearGrupo("3SB", "ALONSO MARTINEZ CARLOS", 11, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        estructuraDatos.agregarGrupo(crearGrupo("3SC", "ARAGON SORROZA ARTURO ARMANDO", 13, 2, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        estructuraDatos.agregarGrupo(crearGrupo("3SD", "ALONSO MARTINEZ CARLOS", 12, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        estructuraDatos.agregarGrupo(crearGrupo("3SE", "ARAGON SORROZA ARTURO ARMANDO", 15, 2, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));

        catalogo.add(estructuraDatos);

        // =========================================================================
        // 6. FISICA GENERAL (Azul Oscuro / Marino) - Semestre 3
        // =========================================================================
        Materia fisicaGeneral = new Materia("FISICA GENERAL", new Color(31, 58, 82), 3);
        fisicaGeneral.setDificultad(3);
        fisicaGeneral.setCreditos(5);

        fisicaGeneral.agregarGrupo(crearGrupo("3SA", "JIMENEZ CABRERA ROBERTO", 8, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        fisicaGeneral.agregarGrupo(crearGrupo("3SB", "-", 7, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        fisicaGeneral.agregarGrupo(crearGrupo("3SC", "JIMENEZ CABRERA ROBERTO", 15, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        fisicaGeneral.agregarGrupo(crearGrupo("3SD", "-", 9, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        fisicaGeneral.agregarGrupo(crearGrupo("3SE", "JIMENEZ CABRERA ROBERTO", 11, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        fisicaGeneral.agregarGrupo(crearGrupo("3SF", "-", 16, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));

        catalogo.add(fisicaGeneral);

        // =========================================================================
        // 7. ECUACIONES DIFERENCIALES (Verde Azulado) - Semestre 4
        // =========================================================================
        Materia ecuacionesDiferenciales = new Materia("ECUACIONES DIFERENCIALES", new Color(0, 121, 107), 4);
        ecuacionesDiferenciales.setDificultad(4);
        ecuacionesDiferenciales.setCreditos(5);

        ecuacionesDiferenciales.agregarGrupo(crearGrupo("4SU", "-", 8, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        ecuacionesDiferenciales.agregarGrupo(crearGrupo("SPQ", "DOROTEO CASTILLEJOS RUBEN", 9, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));

        catalogo.add(ecuacionesDiferenciales);

        // =========================================================================
        // TOPICOS AVANZADOS DE PROGRAMACION (Púrpura / Violeta) - Semestre 4
        // =========================================================================
        Materia topicosAvanzados = new Materia("TOPICOS AV. DE PROGRAMACION", new Color(142, 68, 173), 4);
        topicosAvanzados.setDificultad(4);
        topicosAvanzados.setCreditos(5);
        topicosAvanzados.agregarGrupo(crearGrupo("4SU", "LIMON CORDERO ROGELIO NOE", 14, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        catalogo.add(topicosAvanzados);
        
        
        
        // =========================================================================
        // 1. DESARROLLO SUSTENTABLE (Verde) - Semestre 5
        // =========================================================================
        Materia devSustentable = new Materia("DESARR. SUSTENTABLE", new Color(39, 171, 95), 5);
        devSustentable.setDificultad(1);
        devSustentable.setCreditos(5);
        devSustentable.agregarGrupo(crearGrupo("5SA", "-", 12, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        devSustentable.agregarGrupo(crearGrupo("5SB", "-", 8, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        devSustentable.agregarGrupo(crearGrupo("5SC", "-", 16, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        devSustentable.agregarGrupo(crearGrupo("5SD", "-", 9, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        devSustentable.agregarGrupo(crearGrupo("5SE", "-", 17, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        devSustentable.agregarGrupo(crearGrupo("5SF", "-", 15, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        catalogo.add(devSustentable);

        // =========================================================================
        // 2. FUNDAMENTOS DE TELECOMUNICACIONES (Azul) - Semestre 5
        // =========================================================================
        Materia telecom = new Materia("FUND. DE TELECOM.", new Color(46, 133, 191), 5);
        telecom.setDificultad(4);
        telecom.setCreditos(4);
        telecom.agregarGrupo(crearGrupo("5SA", "VALVERDE JARQUIN REYNA", 11, 5, DiaSemana.LUNES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        telecom.agregarGrupo(crearGrupo("5SB", "ROBLEDO CABRERA OMAR", 12, 3, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        telecom.agregarGrupo(crearGrupo("5SC", "-", 17, 4, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        telecom.agregarGrupo(crearGrupo("5SD", "JIMENEZ HALLA JOHANN FRANCISCO", 17, 4, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        telecom.agregarGrupo(crearGrupo("5SE", "VALVERDE JARQUIN REYNA", 9, 5, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.VIERNES));
        telecom.agregarGrupo(crearGrupo("5SF", "ORTIZ MENDEZ VIRGINIA", 16, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        catalogo.add(telecom);

        // =========================================================================
        // 3. TALLER DE BASES DE DATOS (Naranja) - Semestre 5
        // =========================================================================
        Materia tallerBD = new Materia("TALLER DE B.D.", new Color(210, 106, 20), 5);
        tallerBD.setDificultad(3);
        tallerBD.setCreditos(4);
        tallerBD.agregarGrupo(crearGrupo("5SA", "MORALES HERNANDEZ MARICELA", 8, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        tallerBD.agregarGrupo(crearGrupo("5SB", "VELAZQUEZ HERNANDEZ MARICARMEN MONTSERRAT", 12, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        tallerBD.agregarGrupo(crearGrupo("5SC", "ARAGON SORROZA ARTURO ARMANDO", 11, 2, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        tallerBD.agregarGrupo(crearGrupo("5SD", "ALONSO HERNANDEZ LUIS ALBERTO", 15, 2, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        tallerBD.agregarGrupo(crearGrupo("5SE", "MARTINEZ NIETO ADELINA", 8, 2, DiaSemana.LUNES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        catalogo.add(tallerBD);

        // =========================================================================
        // 4. FUNDAMENTOS DE INGENIERIA DE SOFTWARE (Rojo/Coral) - Semestre 5
        // =========================================================================
        Materia ingSoftware = new Materia("FUND. DE ING. DE SOFT.", new Color(211, 56, 40), 5);
        ingSoftware.setDificultad(3);
        ingSoftware.setCreditos(4);
        ingSoftware.agregarGrupo(crearGrupo("5SA", "SANCHEZ DIAZ CLARA AURORA", 10, 2, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.VIERNES));
        ingSoftware.agregarGrupo(crearGrupo("5SB", "SANCHEZ DIAZ CLARA AURORA", 11, 2, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        ingSoftware.agregarGrupo(crearGrupo("5SC", "CASTAÑON OLGUIN EDUARDO", 14, 2, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.VIERNES));
        ingSoftware.agregarGrupo(crearGrupo("5SD", "CASTAÑON OLGUIN EDUARDO", 12, 2, DiaSemana.LUNES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        ingSoftware.agregarGrupo(crearGrupo("5SE", "-", 7, 2, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        catalogo.add(ingSoftware);

        // =========================================================================
        // 5. SIMULACION (Turquesa) - Semestre 5
        // =========================================================================
        Materia simulacion = new Materia("SIMULACION", new Color(16, 178, 146), 5);
        simulacion.setDificultad(3);
        simulacion.setCreditos(5);
        simulacion.agregarGrupo(crearGrupo("5SA", "MATADAMAS TORRES LORENZO ALEJANDRO", 7, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        simulacion.agregarGrupo(crearGrupo("5SB", "MATADAMAS TORRES LORENZO ALEJANDRO", 13, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        simulacion.agregarGrupo(crearGrupo("5SC", "ALONSO MARTINEZ CARLOS", 15, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        simulacion.agregarGrupo(crearGrupo("5SD", "ALONSO MARTINEZ CARLOS", 18, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        catalogo.add(simulacion);
        
        // =========================================================================
        // 6. ARQUITECTURA DE COMPUTADORAS (Morado / Índigo) - Semestre 5
        // =========================================================================
        Materia arqComputadoras = new Materia("ARQ. DE COMP.", new Color(103, 58, 183), 5);
        arqComputadoras.setDificultad(4);
        arqComputadoras.setCreditos(5);

        arqComputadoras.agregarGrupo(crearGrupo("5SA", "-", 9, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        arqComputadoras.agregarGrupo(crearGrupo("5SB", "VELAZQUEZ HERNANDEZ MARICARMEN MONTSERRAT", 10, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        arqComputadoras.agregarGrupo(crearGrupo("5SC", "ARAGON LOPEZ ARMANDO", 18, 4, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        arqComputadoras.agregarGrupo(crearGrupo("5SD", "ARAGON LOPEZ ARMANDO", 16, 4, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        catalogo.add(arqComputadoras);

        // =========================================================================
        // 1. ADMINISTRACION DE BASES DE DATOS (Vino / Granate) - Semestre 6
        // =========================================================================
        Materia adminBD = new Materia("ADMIN. DE B.D.", new Color(136, 14, 79), 6);
        adminBD.setDificultad(3);
        adminBD.setCreditos(5);
        adminBD.agregarGrupo(crearGrupo("6SU", "-", 15, 3, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        catalogo.add(adminBD);

        // Guardado de persistencia
        GestorPersistencia.guardar(catalogo, borradores);

        System.out.println("==================================================");
        System.out.println("Materias guardadas: " + catalogo.size());
        System.out.println("Borradores preservados: " + borradores.size());
        System.out.println("Archivo de salida: materias.dat / borradores.dat");
        System.out.println("==================================================");
    }

    private static Grupo crearGrupo(String clave, String profesor, int horaInicio, int dificultad, DiaSemana... dias) {
        Set<DiaSemana> conjuntoDias = EnumSet.noneOf(DiaSemana.class);
        for (DiaSemana d : dias) {
            conjuntoDias.add(d);
        }
        Grupo g = new Grupo(clave, profesor, horaInicio, conjuntoDias);
        g.setDificultad(dificultad);
        return g;
    }
}