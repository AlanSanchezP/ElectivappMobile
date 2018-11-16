package mx.ipn.upiicsa.electivapp;

import java.util.List;

import mx.ipn.upiicsa.electivapp.models.APIGenericResponse;
import mx.ipn.upiicsa.electivapp.models.Carrera;
import mx.ipn.upiicsa.electivapp.models.Evento;
import mx.ipn.upiicsa.electivapp.models.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface Service {
    public static final String BASE_URL = "http://192.168.0.13:8000/api/";

    @GET("carreras")
    Call<List<Carrera>> listaCarreras();

    @GET("eventos")
    Call<List<Evento>> listaEventos();

    @FormUrlEncoded
    @POST("token-auth/")
    Call<LoginResponse> tokenAuth(@Field("username") String username,
                                  @Field("password") String password,
                                  @Field("evento") int evento);

    @FormUrlEncoded
    @POST("eventos/asistencia/qr")
    Call<APIGenericResponse> asistenciaQR(@Header("Authorization") String token,
                                          @Field("user") int user,
                                          @Field("evento") int evento,
                                          @Field("url") String url);

    @FormUrlEncoded
    @POST("eventos/asistencia/form")
    Call<APIGenericResponse> asistenciaForm(@Header("Authorization") String token,
                                          @Field("user") int user,
                                          @Field("evento") int evento,
                                          @Field("boleta") String boleta,
                                          @Field("nombre") String nombre,
                                          @Field("carrera") String carrera);
}
