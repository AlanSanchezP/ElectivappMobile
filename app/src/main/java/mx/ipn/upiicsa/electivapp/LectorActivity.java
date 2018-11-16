package mx.ipn.upiicsa.electivapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.annotation.Annotation;

import mx.ipn.upiicsa.electivapp.models.APIGenericResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LectorActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private String previousToken = "";
    private String currentToken = "";
    private Retrofit retrofit;
    private Service service;
    private String token;
    private int user;
    private int evento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lector);

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);

        token = prefs.getString("token", null);
        user = prefs.getInt("user", -1);
        evento = prefs.getInt("evento", -1);

        surfaceView = findViewById(R.id.camara);
        retrofit = new Retrofit.Builder()
                .baseUrl(Service.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(Service.class);
        activarCamara();
    }

    public void activarCamara() {
        BarcodeDetector barcodeDetector = new BarcodeDetector
                .Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (ActivityCompat.checkSelfPermission(LectorActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA));
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                } else {
                    iniciarCamara();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) { }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {}

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() > 0) {
                    currentToken = barcodes.valueAt(0).displayValue.toString();
                    if (!currentToken.equals(previousToken)) {
                        previousToken = currentToken;
                        if (URLUtil.isValidUrl(currentToken) && currentToken.contains("www.dae.ipn.mx")) {
                            registrarAsistencia(currentToken);
                        }
                    }
                }
            }
        });
    }

    public void iniciarCamara() {
        try {
            cameraSource.start(surfaceView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registrarAsistencia(String url) {
        Call<APIGenericResponse> call = service.asistenciaQR("Token "+token, user, evento, url);

        call.enqueue(new Callback<APIGenericResponse>() {
            @Override
            public void onResponse(Call<APIGenericResponse> call, Response<APIGenericResponse> response) {
                if (response.code() == 200) {
                    Toast.makeText(LectorActivity.this, response.body().getDetail(), Toast.LENGTH_LONG).show();
                } else {
                    Converter<ResponseBody, APIGenericResponse> converter =
                            retrofit.responseBodyConverter(APIGenericResponse.class, new Annotation[0]);
                    APIGenericResponse error;

                    try {
                        error = converter.convert(response.errorBody());
                        int code = error.getCode();

                        Toast.makeText(LectorActivity.this, error.getDetail(), Toast.LENGTH_LONG).show();
                        if (code == 302 || code == 101 || code == 102 || code == 103 || code == 0) {
                            Intent intent = new Intent(LectorActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else if (code == 204 || code == 205 || code == 206) {
                            startActivity(new Intent(LectorActivity.this, FormularioActivity.class));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                finish();
            }

            @Override
            public void onFailure(Call<APIGenericResponse>call, Throwable t) {
                Toast.makeText(LectorActivity.this, "Ocurrió un error inesperado", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LectorActivity.this, FormularioActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println(requestCode);
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.CAMERA)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        iniciarCamara();
                    } else {
                        finish();
                    }
                }
            }
        }
    }
}