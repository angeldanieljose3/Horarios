package modelo;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Materia implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private Color color;
    private List<Grupo> grupos;
    private int dificultad = 3; // 1 (fácil) a 5 (difícil)
    private int semestre = 1;   // Semestre por defecto (1 a 10)

    public Materia(String nombre, Color color) {
        this.nombre = nombre;
        this.color = color;
        this.grupos = new ArrayList<>();
    }

    public Materia(String nombre, Color color, int semestre) {
        this(nombre, color);
        this.semestre = semestre;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    public List<Grupo> getGrupos() { return grupos; }
    public int getDificultad() { return dificultad; }
    public void setDificultad(int dificultad) { this.dificultad = dificultad; }

    public int getSemestre() { return semestre; }
    public void setSemestre(int semestre) { this.semestre = semestre; }

    public void agregarGrupo(Grupo g) {
        g.setMateriaPadre(this);
        grupos.add(g);
    }

    @Override
    public String toString() {
        return nombre + " (" + grupos.size() + " grupos)";
    }
}