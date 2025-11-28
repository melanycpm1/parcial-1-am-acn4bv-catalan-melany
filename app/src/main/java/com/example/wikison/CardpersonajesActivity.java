package com.example.wikison;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CardpersonajesActivity extends AppCompatActivity {

    private ImageView imgPersonaje;
    private TextView tvNombre, tvRol, tvCaracteristica, tvFrases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardpersonajes);

        // -------------------- VIEWS --------------------
        imgPersonaje = findViewById(R.id.imgPersonaje);
        tvNombre = findViewById(R.id.tvNombre);
        tvRol = findViewById(R.id.tvRol);
        tvCaracteristica = findViewById(R.id.tvCaracteristica);
        tvFrases = findViewById(R.id.tvFrase);

        // -------------------- RECIBIR DATOS --------------------
        String nombre = getIntent().getStringExtra("nombre");
        String rol = getIntent().getStringExtra("rol");
        String caracteristica = getIntent().getStringExtra("caracteristica");
        String frases = getIntent().getStringExtra("frases");
        String img = getIntent().getStringExtra("img");

        // -------------------- SETEAR DATOS --------------------
        tvNombre.setText(nombre);
        tvRol.setText("Rol: " + rol);
        tvCaracteristica.setText(caracteristica);
        tvFrases.setText(frases);

        // -------------------- CARGAR IMAGEN --------------------
        if (img.startsWith("http://") || img.startsWith("https://")) {
            new Thread(() -> {
                try {
                    URL url = new URL(img);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    is.close();

                    runOnUiThread(() -> imgPersonaje.setImageBitmap(bitmap));
                } catch (Exception e) {
                    Log.e("Cardpersonajes", "Error cargando imagen: " + e.getMessage());
                }
            }).start();
        } else {
            int resID = getResources().getIdentifier(
                    img.replace(".jpg", "").toLowerCase(), 
                    "drawable", 
                    getPackageName()
            );
            if (resID != 0) imgPersonaje.setImageResource(resID);
        }
    }
}
