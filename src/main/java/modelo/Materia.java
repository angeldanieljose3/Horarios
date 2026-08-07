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

    public Materia(String nombre, Color color) {
        this.nombre = nombre;
        this.color = color;
        this.grupos = new ArrayList<>();
    }

    public String getNombre() { return nombre; }
    public Color getColor() { return color; }
    public List<Grupo> getGrupos() { return grupos; }

    public void agregarGrupo(Grupo g) {
        g.setMateriaPadre(this);
        grupos.add(g);
    }

    @Override
    public String toString() {
        return nombre + " (" + grupos.size() + " grupos)";
    }
}