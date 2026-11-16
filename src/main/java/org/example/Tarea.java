package org.example;

public class Tarea {

    private String titulo;
    private String estado;
    private String colorHex;
    private String notas;

    public Tarea(String titulo, String estado, String colorHex, String notas) {
        this.titulo = titulo;
        this.estado = estado;
        this.colorHex = colorHex;
        this.notas = notas;
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}