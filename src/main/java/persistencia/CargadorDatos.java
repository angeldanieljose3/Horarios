package persistencia;

import modelo.DiaSemana;
import modelo.Grupo;
import modelo.HorarioBorrador; // Importación nueva necesaria
import modelo.Materia;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class CargadorDatos {

    public static void main(String[] args) {
        List<Materia> catalogo = new ArrayList<>();
        
        // 1. CAMBIO AQUÍ: Se carga la lista de HorarioBorrador en lugar de la lista simple de grupos
        List<HorarioBorrador> borradores = GestorPersistencia.cargarBorradores();

        // =========================================================================
        // 1. DESARROLLO SUSTENTABLE (Verde)
        // =========================================================================
        Materia devSustentable = new Materia("DESARR. SUSTENTABLE", new Color(39, 171, 95));
        devSustentable.agregarGrupo(crearGrupo("5SA", "-", 12, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        devSustentable.agregarGrupo(crearGrupo("5SB", "-", 8, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        devSustentable.agregarGrupo(crearGrupo("5SC", "-", 16, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        devSustentable.agregarGrupo(crearGrupo("5SD", "-", 9, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        devSustentable.agregarGrupo(crearGrupo("5SE", "-", 17, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        devSustentable.agregarGrupo(crearGrupo("5SF", "-", 15, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        catalogo.add(devSustentable);

        // =========================================================================
        // 2. FUNDAMENTOS DE TELECOMUNICACIONES (Azul)
        // =========================================================================
        Materia telecom = new Materia("FUND. DE TELECOM.", new Color(46, 133, 191));
        telecom.agregarGrupo(crearGrupo("5SA", "VALVERDE JARQUIN REYNA", 11, DiaSemana.LUNES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        telecom.agregarGrupo(crearGrupo("5SB", "ROBLEDO CABRERA OMAR", 12, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        telecom.agregarGrupo(crearGrupo("5SC", "ARAGON LOPEZ ARMANDO", 17, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        telecom.agregarGrupo(crearGrupo("5SD", "JIMENEZ HALLA JOHANN FRANCISCO", 17, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        telecom.agregarGrupo(crearGrupo("5SE", "VALVERDE JARQUIN REYNA", 9, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.VIERNES));
        telecom.agregarGrupo(crearGrupo("5SF", "ORTIZ MENDEZ VIRGINIA", 16, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        catalogo.add(telecom);

        // =========================================================================
        // 3. TALLER DE BASES DE DATOS (Naranja)
        // =========================================================================
        Materia tallerBD = new Materia("TALLER DE B.D.", new Color(210, 106, 20));
        tallerBD.agregarGrupo(crearGrupo("5SA", "MORALES HERNANDEZ MARICELA", 8, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        tallerBD.agregarGrupo(crearGrupo("5SB", "VELAZQUEZ HERNANDEZ MARICARMEN MONTSERRAT", 12, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        tallerBD.agregarGrupo(crearGrupo("5SC", "ARAGON SORROZA ARTURO ARMANDO", 11, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        tallerBD.agregarGrupo(crearGrupo("5SD", "ALONSO HERNANDEZ LUIS ALBERTO", 15, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        tallerBD.agregarGrupo(crearGrupo("5SE", "SILVA MARTINEZ DALIA", 8, DiaSemana.LUNES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        catalogo.add(tallerBD);

        // =========================================================================
        // 4. FUNDAMENTOS DE INGENIERIA DE SOFTWARE (Rojo/Coral)
        // =========================================================================
        Materia ingSoftware = new Materia("FUND. DE ING. DE SOFT.", new Color(211, 56, 40));
        ingSoftware.agregarGrupo(crearGrupo("5SA", "SANCHEZ DIAZ CLARA AURORA", 10, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.VIERNES));
        ingSoftware.agregarGrupo(crearGrupo("5SB", "SANCHEZ DIAZ CLARA AURORA", 11, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES));
        ingSoftware.agregarGrupo(crearGrupo("5SC", "CASTAÑON OLGUIN EDUARDO", 14, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.VIERNES));
        ingSoftware.agregarGrupo(crearGrupo("5SD", "CASTAÑON OLGUIN EDUARDO", 12, DiaSemana.LUNES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        catalogo.add(ingSoftware);

        /*
        // =========================================================================
        // 5. ARQUITECTURA DE COMPUTADORAS (Morado)
        // =========================================================================
        Materia arqComp = new Materia("ARQ. DE COMP.", new Color(135, 69, 162));
        arqComp.agregarGrupo(crearGrupo("5SA", "ARAGON LOPEZ ARMANDO", 9, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        arqComp.agregarGrupo(crearGrupo("5SB", "ARAGON LOPEZ ARMANDO", 10, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        arqComp.agregarGrupo(crearGrupo("5SC", "ARAGON LOPEZ ARMANDO", 18, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        arqComp.agregarGrupo(crearGrupo("5SD", "-", 16, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        catalogo.add(arqComp); 
        */

        // =========================================================================
        // 6. SIMULACION (Turquesa)
        // =========================================================================
        Materia simulacion = new Materia("SIMULACION", new Color(16, 178, 146));
        simulacion.agregarGrupo(crearGrupo("5SA", "-", 7, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        simulacion.agregarGrupo(crearGrupo("5SB", "MATADAMAS TORRES LORENZO ALEJANDRO", 13, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        simulacion.agregarGrupo(crearGrupo("5SC", "ALONSO MARTINEZ CARLOS", 15, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        simulacion.agregarGrupo(crearGrupo("5SD", "ALONSO MARTINEZ CARLOS", 18, DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES));
        catalogo.add(simulacion);

        // 2. CAMBIO AQUÍ: Guardamos la lista de borradores en lugar de horarioActivo
        GestorPersistencia.guardar(catalogo, borradores);

        System.out.println("==================================================");
        System.out.println("Materias guardadas: " + catalogo.size());
        System.out.println("Borradores preservados: " + borradores.size());
        System.out.println("Archivo de salida: materias.dat / borradores.dat");
        System.out.println("==================================================");
    }

    private static Grupo crearGrupo(String clave, String profesor, int horaInicio, DiaSemana... dias) {
        Set<DiaSemana> conjuntoDias = EnumSet.noneOf(DiaSemana.class);
        for (DiaSemana d : dias) {
            conjuntoDias.add(d);
        }
        return new Grupo(clave, profesor, horaInicio, conjuntoDias);
    }
}