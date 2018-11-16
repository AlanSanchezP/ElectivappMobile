package mx.ipn.upiicsa.electivapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import mx.ipn.upiicsa.electivapp.models.APIGenericResponse;
import mx.ipn.upiicsa.electivapp.models.Carrera;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FormularioActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Retrofit retrofit;
    private Service service;
    private String token;
    private int user;
    private int evento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);

        token = prefs.getString("token", null);
        user = prefs.getInt("user", -1);
        evento = prefs.getInt("evento", -1);

        final List<Carrera> spinnerArray = new ArrayList<>();

        retrofit = new Retrofit.Builder()
                .baseUrl(Service.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(Service.class);
        Call<List<Carrera>> call = service.listaCarreras();

        call.enqueue(new Callback<List<Carrera>>() {
            @Override
            public void onResponse(Call<List<Carrera>> call, Response<List<Carrera>> response) {
                setSpinner(spinnerArray, response);
            }

            @Override
            public void onFailure(Call<List<Carrera>>call, Throwable t) {
                Toast.makeText(FormularioActivity.this, "No se pudo conectar a la base de datos", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setSpinner(List<Carrera> spinnerArray, Response<List<Carrera>> response) {
        spinnerArray.addAll(response.body());

        Spinner carreras = findViewById(R.id.carrera);
        ArrayAdapter<Carrera> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                spinnerArray
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        carreras.setAdapter(adapter);
        carreras.setSelection(0);
        carreras.setOnItemSelectedListener(this);
    }

    public void enviarDatos(View v) {
        EditText boleta = findViewById(R.id.boleta);
        EditText nombre = findViewById(R.id.nombre);
        Spinner carreras = findViewById(R.id.carrera);
        Carrera carrera = (Carrera) carreras.getSelectedItem();

        Call<APIGenericResponse> call = service.asistenciaForm("Token "+token,
                user,
                evento,
                boleta.getText().toString(),
                nombre.getText().toString(),
                carrera.getCodigo());

        call.enqueue(new Callback<APIGenericResponse>() {
            @Override
            public void onResponse(Call<APIGenericResponse> call, Response<APIGenericResponse> response) {
                if (response.code() == 200) {
                    Toast.makeText(FormularioActivity.this, response.body().getDetail(), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Converter<ResponseBody, APIGenericResponse> converter =
                            retrofit.responseBodyConverter(APIGenericResponse.class, new Annotation[0]);
                    APIGenericResponse error;

                    try {
                        error = converter.convert(response.errorBody());
                        int code = error.getCode();

                        Toast.makeText(FormularioActivity.this, error.getDetail(), Toast.LENGTH_LONG).show();
                        if (code == 302 || code == 101 || code == 102 || code == 103 || code == 0) {
                            Intent intent = new Intent(FormularioActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else if (code == 203 || code == 204) {
                            finish();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<APIGenericResponse>call, Throwable t) {
                Toast.makeText(FormularioActivity.this, "Ocurrió un error inesperado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
