package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HorarioBorrador implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre; // Ej. "Opción A", "Opción B"
    private List<Grupo> gruposActivos;

    public HorarioBorrador(String nombre) {
        this.nombre = nombre;
        this.gruposActivos = new ArrayList<>();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public List<Grupo> getGruposActivos() { return gruposActivos; }

    @Override
    public String toString() {
        return nombre;
    }
}