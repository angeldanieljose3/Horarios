package modelo;

import java.io.Serializable;
import java.util.Set;

public class Grupo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String claveGrupo; // Ej. "4A", "G1"
    private String profesor;
    private int horaInicio;
    private Set<DiaSemana> dias;
    private Materia materiaPadre; // Referencia a la materia a la que pertenece
    private boolean disponible = true; // false = grupo lleno / cupo agotado
    private int dificultad = 3; // 1 (fácil) a 5 (difícil), calificación de ese profesor/grupo
    private boolean prioritario = false; // true = el generador intenta usar este grupo antes que otros de la misma materia

    public Grupo(String claveGrupo, String profesor, int horaInicio, Set<DiaSemana> dias) {
        this.claveGrupo = claveGrupo;
        this.profesor = profesor;
        this.horaInicio = horaInicio;
        this.dias = dias;
    }

    public String getClaveGrupo() { return claveGrupo; }
    public void setClaveGrupo(String claveGrupo) { this.claveGrupo = claveGrupo; }
    public String getProfesor() { return profesor; }
    public void setProfesor(String profesor) { this.profesor = profesor; }
    public int getHoraInicio() { return horaInicio; }
    public void setHoraInicio(int horaInicio) { this.horaInicio = horaInicio; }
    public Set<DiaSemana> getDias() { return dias; }
    public void setDias(Set<DiaSemana> dias) { this.dias = dias; }
    public Materia getMateriaPadre() { return materiaPadre; }
    public void setMateriaPadre(Materia materiaPadre) { this.materiaPadre = materiaPadre; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public int getDificultad() { return dificultad; }
    public void setDificultad(int dificultad) { this.dificultad = dificultad; }
    public boolean isPrioritario() { return prioritario; }
    public void setPrioritario(boolean prioritario) { this.prioritario = prioritario; }

    public boolean chocaCon(Grupo otro) {
        if (this.horaInicio != otro.horaInicio) return false;
        for (DiaSemana d : this.dias) {
            if (otro.getDias().contains(d)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String nombreMat = (materiaPadre != null) ? materiaPadre.getNombre() : "Materia";
        return nombreMat + " [" + claveGrupo + "] - " + profesor + " (" + horaInicio + ":00 hrs)";
    }
}