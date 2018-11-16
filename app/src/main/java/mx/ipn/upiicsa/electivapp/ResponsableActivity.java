package mx.ipn.upiicsa.electivapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class ResponsableActivity extends AppCompatActivity {
    private String token;
    private int user;
    private int evento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responsable);

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        token = prefs.getString("token", null);
        user = prefs.getInt("user", -1);
        evento = prefs.getInt("evento", -1);
    }

    public void abrirCamara(View v) {
        startActivity(new Intent(ResponsableActivity.this, LectorActivity.class));
    }
}
