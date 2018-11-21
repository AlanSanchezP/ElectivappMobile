package mx.ipn.upiicsa.electivapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import mx.ipn.upiicsa.electivapp.models.APIGenericResponse;
import mx.ipn.upiicsa.electivapp.models.Evento;
import mx.ipn.upiicsa.electivapp.models.LoginResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Retrofit retrofit;
    private Service service;
    public static final String PREFS_NAME = "ElectivappFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.clear().commit();

        final List<Evento> spinnerArray = new ArrayList<>();

        retrofit = new Retrofit.Builder()
                .baseUrl(Service.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(Service.class);
        Call<List<Evento>> call = service.listaEventos();

        call.enqueue(new Callback<List<Evento>>() {
            @Override
            public void onResponse(Call<List<Evento>> call, Response<List<Evento>> response) {
                setSpinner(spinnerArray, response);
            }

            @Override
            public void onFailure(Call<List<Evento>>call, Throwable t) {
                Toast.makeText(MainActivity.this, "No se pudo conectar a la base de datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSpinner(List<Evento> spinnerArray, Response<List<Evento>> response) {
        spinnerArray.addAll(response.body());

        Spinner eventos = findViewById(R.id.evento);
        Button botonLogin = findViewById(R.id.login);

        ArrayAdapter<Evento> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                spinnerArray
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventos.setAdapter(adapter);
        eventos.setSelection(0);
        eventos.setOnItemSelectedListener(this);
        botonLogin.setEnabled(true);
    }

    public void login(View v) {
        EditText boleta = findViewById(R.id.boleta);
        EditText password = findViewById(R.id.password);
        Spinner eventos = findViewById(R.id.evento);
        Evento evento = (Evento) eventos.getSelectedItem();

        Call<LoginResponse> call = service.tokenAuth(boleta.getText().toString(), password.getText().toString(), evento.getId());

        call.enqueue(new Callback<LoginResponse>() {
            @Override
           public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.code() == 200) {
                    LoginResponse _response = response.body();
                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("token", _response.getToken());
                    editor.putInt("user", _response.getUser());
                    editor.putInt("evento", _response.getEvento());
                    editor.apply();

                    startActivity(new Intent(MainActivity.this, ResponsableActivity.class));
                } else {
                    Converter<ResponseBody, APIGenericResponse> converter =
                            retrofit.responseBodyConverter(APIGenericResponse.class, new Annotation[0]);
                    APIGenericResponse error;

                    try {
                        error = converter.convert(response.errorBody());
                        Toast.makeText(MainActivity.this, error.getDetail(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse>call, Throwable t) {
                Toast.makeText(MainActivity.this, "No se pudo conectar a la base de datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
