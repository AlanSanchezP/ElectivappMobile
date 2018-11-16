package mx.ipn.upiicsa.electivapp.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private String token;
    @SerializedName("user_id")
    private int user;
    @SerializedName("evento_id")
    private int evento;

    public LoginResponse(String token, int user, int evento) {
        this.token = token;
        this.user = user;
        this.evento = evento;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getEvento() {
        return evento;
    }

    public void setEvento(int evento) {
        this.evento = evento;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "{"
        + "token: " + this.token + ","
        + "user: " + this.user + ","
        + "evento: " + this.evento
        + "}";
    }
}
