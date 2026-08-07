package persistencia;

import modelo.HorarioBorrador;
import modelo.Materia;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestorPersistencia {
    private static final String ARCHIVO_MATERIAS = "materias.dat";
    private static final String ARCHIVO_BORRADORES = "borradores.dat";

    public static void guardar(List<Materia> materias, List<HorarioBorrador> borradores) {
        guardarObjeto(materias, ARCHIVO_MATERIAS);
        guardarObjeto(borradores, ARCHIVO_BORRADORES);
    }

    @SuppressWarnings("unchecked")
    public static List<Materia> cargarMaterias() {
        Object obj = cargarObjeto(ARCHIVO_MATERIAS);
        return (obj != null) ? (List<Materia>) obj : new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static List<HorarioBorrador> cargarBorradores() {
        Object obj = cargarObjeto(ARCHIVO_BORRADORES);
        if (obj != null) {
            return (List<HorarioBorrador>) obj;
        } else {
            // Si no hay borradores, creamos la Opción A por defecto
            List<HorarioBorrador> inicial = new ArrayList<>();
            inicial.add(new HorarioBorrador("Opción A"));
            return inicial;
        }
    }

    private static void guardarObjeto(Object o, String ruta) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ruta))) {
            oos.writeObject(o);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static Object cargarObjeto(String ruta) {
        File f = new File(ruta);
        if (!f.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return ois.readObject();
        } catch (Exception e) { return null; }
    }
}