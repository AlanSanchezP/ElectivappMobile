package mx.ipn.upiicsa.electivapp.models;

import java.util.Date;

public class Evento {
    private int id;
    private String nombre;
    private Date fecha;

    public Evento(int id, String nombre, Date fecha) {
        this.nombre = nombre;
        this.id = id;
        this.fecha = fecha;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.nombre;
    }
}
