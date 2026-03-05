package com.example.wikison;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    LinearLayout contenedorEdittexts, contenedorPersonajes;
    Button btnAgregar;
    String[] hints = {"Nombre", "Rol", "Característica", "URL", "Frase"};
    JSONArray personajes;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private LinearLayout contenedorEdittextsLugares;
    private LinearLayout contenedorLugares;
    private Button btnAgregarLugar;
    private JSONArray lugares;
    private String[] hintsLugares = {"Nombre del lugar", "URL de imagen"};

    // ---------- NAVBAR ----------
    private ImageView imgMenu;
    private LinearLayout horizontalNavbar;
    private Button btnPersonajes;
    private Button btnLugares;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        // ---------- INICIALIZAR FIREBASE ----------
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ---------- INICIALIZAR VISTAS ----------
        contenedorEdittexts = findViewById(R.id.contenedor_edittexts);
        contenedorPersonajes = findViewById(R.id.contenedor_personajes);
        btnAgregar = findViewById(R.id.btn_agregar);
        contenedorEdittextsLugares = findViewById(R.id.contenedorEdittextsLugares);
        contenedorLugares = findViewById(R.id.contenedorLugares);
        btnAgregarLugar = findViewById(R.id.btnAgregarLugar);
        imgMenu = findViewById(R.id.img_menu);
        horizontalNavbar = findViewById(R.id.horizontal_navbar);
        btnPersonajes = findViewById(R.id.btn_personajes);
        btnLugares = findViewById(R.id.btn_lugares);

        personajes = new JSONArray();
        lugares = new JSONArray();

        // ---------- BIENVENIDA USUARIO ----------
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("usuarios").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        if (documentSnapshot.exists()) {

                            String nombre = documentSnapshot.getString("nombre");

                            Toast.makeText(MainActivity.this,
                                    "Bienvenido " + nombre,
                                    Toast.LENGTH_SHORT).show();

                        } else {

                            Toast.makeText(MainActivity.this,
                                    "No existe usuario en Firestore",
                                    Toast.LENGTH_SHORT).show();
                        }

                    });

        } else {

            Toast.makeText(MainActivity.this,
                    "No hay usuario logueado",
                    Toast.LENGTH_SHORT).show();
        }

        // -------------------- EDITTEXTS PERSONAJES --------------------
        for (String hint : hints) {
            EditText edit = new EditText(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 25, 0, 0);
            edit.setLayoutParams(params);
            edit.setHint(hint);
            edit.setBackgroundResource(R.drawable.edittext_border);
            edit.setPadding(16, 16, 16, 16);
            edit.setTextSize(16);
            contenedorEdittexts.addView(edit);
        }

        // -------------------- EDITTEXTS LUGARES --------------------
        for (String hint : hintsLugares) {
            EditText edit = new EditText(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 25, 0, 0);
            edit.setLayoutParams(params);
            edit.setHint(hint);
            edit.setBackgroundResource(R.drawable.edittext_border);
            edit.setPadding(16, 16, 16, 16);
            edit.setTextSize(16);
            contenedorEdittextsLugares.addView(edit);
        }


        // -------------------- BOTÓN AGREGAR LUGAR --------------------
        btnAgregarLugar.setOnClickListener(v -> {
            try {
                String nombre = ((EditText) contenedorEdittextsLugares.getChildAt(0)).getText().toString();
                String url = ((EditText) contenedorEdittextsLugares.getChildAt(1)).getText().toString();

                if (url.isEmpty()) return;

                JSONObject lugar = new JSONObject();
                lugar.put("nombre", nombre);
                lugar.put("img", url);

                agregarCardLugar(lugar);
                lugares.put(lugares.length(), lugar);

                ((EditText) contenedorEdittextsLugares.getChildAt(0)).setText("");
                ((EditText) contenedorEdittextsLugares.getChildAt(1)).setText("");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // -------------------- EDITTEXTS PERSONAJES --------------------
        for (String hint : hints) {
            EditText editText = new EditText(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(600, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 25, 0, 0);
            editText.setLayoutParams(params);
            editText.setHint(hint);
            editText.setBackgroundResource(R.drawable.edittext_border);
            editText.setPadding(16, 16, 16, 16);
            editText.setTextSize(16);
            editText.setTextColor(getColor(android.R.color.black));
            contenedorEdittexts.addView(editText);
        }

        // -------------------- BOTÓN AGREGAR PERSONAJE --------------------
        btnAgregar.setOnClickListener(v -> {
            try {
                String nombre = ((EditText) contenedorEdittexts.getChildAt(0)).getText().toString();
                String rol = ((EditText) contenedorEdittexts.getChildAt(1)).getText().toString();
                String caracteristica = ((EditText) contenedorEdittexts.getChildAt(2)).getText().toString();
                String url = ((EditText) contenedorEdittexts.getChildAt(3)).getText().toString();
                String frase = ((EditText) contenedorEdittexts.getChildAt(4)).getText().toString();

                if (url.isEmpty()) return;

                JSONObject personaje = new JSONObject();
                personaje.put("nombre", nombre);
                personaje.put("rol", rol);
                personaje.put("caracteristica", caracteristica);
                personaje.put("img", url);
                personaje.put("frase", frase);

                personajes.put(personajes.length(), personaje);
                agregarCardPersonaje(personaje);

                for (int i = 0; i < contenedorEdittexts.getChildCount(); i++) {
                    ((EditText) contenedorEdittexts.getChildAt(i)).setText("");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // -------------------- TOGGLE NAVBAR --------------------
        imgMenu.setOnClickListener(v -> {
            if (horizontalNavbar.getVisibility() == View.GONE) {
                horizontalNavbar.setVisibility(View.VISIBLE);
            } else {
                horizontalNavbar.setVisibility(View.GONE);
            }
        });

        // -------------------- BOTONES PERSONAJES / LUGARES --------------------
        btnPersonajes.setOnClickListener(v -> {
            contenedorPersonajes.setVisibility(View.VISIBLE);
            contenedorLugares.setVisibility(View.GONE);
        });

        btnLugares.setOnClickListener(v -> {
            contenedorPersonajes.setVisibility(View.GONE);
            contenedorLugares.setVisibility(View.VISIBLE);
        });

        // -------------------- CARGA DESDE API --------------------
        new Thread(() -> {
            try {
                URL url = new URL("https://api.npoint.io/7b58b3db38938174a228");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                InputStream is = conn.getInputStream();
                StringBuilder jsonBuilder = new StringBuilder();
                int ch;
                while ((ch = is.read()) != -1) {
                    jsonBuilder.append((char) ch);
                }
                is.close();

                JSONObject json = new JSONObject(jsonBuilder.toString());
                personajes = json.getJSONArray("personajes");

                if (json.has("lugares")) {
                    lugares = json.getJSONArray("lugares");
                }

                runOnUiThread(() -> {
                    mostrarPersonajesIniciales();
                    mostrarLugaresIniciales();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // -------------------- LUGARES --------------------
    private void mostrarLugaresIniciales() {
        contenedorLugares.removeAllViews();
        try {
            int mostrar = Math.min(2, lugares.length());
            for (int i = 0; i < mostrar; i++) {
                agregarCardLugar(lugares.getJSONObject(i));
            }

            if (lugares.length() > 2) {
                Button btnVerMas = new Button(this);
                btnVerMas.setText("Ver más");
                btnVerMas.setBackgroundTintList(getColorStateList(R.color.azul));
                btnVerMas.setTextColor(getColor(android.R.color.white));
                btnVerMas.setOnClickListener(v -> mostrarMasLugares());
                contenedorLugares.addView(btnVerMas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarMasLugares() {
        contenedorLugares.removeAllViews();
        try {
            for (int i = 0; i < lugares.length(); i++) {
                agregarCardLugar(lugares.getJSONObject(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

    private void agregarCardLugar(JSONObject lugar) {
        try {
            CardView card = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 20, 0, 0);
            card.setLayoutParams(cardParams);
            card.setRadius(25f);
            card.setCardElevation(8f);
            card.setUseCompatPadding(true);
            card.setContentPadding(16, 16, 16, 16);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setGravity(Gravity.CENTER_VERTICAL);
            layout.setPadding(8, 8, 8, 8);

            ImageView imagen = new ImageView(this);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(200, 200);
            imagen.setLayoutParams(imgParams);
            imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);

            String imgStr = lugar.optString("img", "");
            if (imgStr.startsWith("http://") || imgStr.startsWith("https://")) {
                new Thread(() -> {
                    try {
                        URL url = new URL(imgStr);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        is.close();

                        runOnUiThread(() -> imagen.setImageBitmap(bitmap));
                    } catch (Exception e) {
                        Log.d("LUGAR", "Error cargando imagen: " + e.getMessage());
                    }
                }).start();
            } else {
                String nombreImagen = imgStr.replace(".jpg", "").toLowerCase().replace(" ", "_");
                int resID = getResources().getIdentifier(nombreImagen, "drawable", getPackageName());
                if (resID == 0) return;
                imagen.setImageResource(resID);
            }

            layout.addView(imagen);

            LinearLayout textoLayout = new LinearLayout(this);
            textoLayout.setOrientation(LinearLayout.VERTICAL);
            textoLayout.setPadding(16, 0, 0, 0);
            LinearLayout.LayoutParams textoParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            textoLayout.setLayoutParams(textoParams);

            TextView nombre = new TextView(this);
            nombre.setText(lugar.optString("nombre", "Sin nombre"));
            nombre.setTextSize(18);
            nombre.setTypeface(null, android.graphics.Typeface.BOLD);
            textoLayout.addView(nombre);

            layout.addView(textoLayout);

            ImageView eliminar = new ImageView(this);
            eliminar.setImageResource(R.drawable.delete);
            LinearLayout.LayoutParams eliminarParams = new LinearLayout.LayoutParams(80, 80);
            eliminarParams.setMargins(16, 0, 0, 0);
            eliminar.setLayoutParams(eliminarParams);
            eliminar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            eliminar.setOnClickListener(v -> contenedorLugares.removeView(card));
            layout.addView(eliminar);

            card.addView(layout);
            contenedorLugares.addView(card);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------- PERSONAJES --------------------
    private void mostrarPersonajesIniciales() {
        contenedorPersonajes.removeAllViews();
        try {
            for (int i = 0; i < Math.min(2, personajes.length()); i++) {
                agregarCardPersonaje(personajes.getJSONObject(i));
            }

            if (personajes.length() > 2) {
                Button btnVerMas = new Button(this);
                btnVerMas.setText("Ver más");
                btnVerMas.setTextSize(18);
                btnVerMas.setBackgroundTintList(getColorStateList(R.color.azul));
                btnVerMas.setTextColor(getColor(android.R.color.white));
                btnVerMas.setOnClickListener(v -> mostrarMasPersonajes());
                contenedorPersonajes.addView(btnVerMas);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarMasPersonajes() {
        contenedorPersonajes.removeAllViews();
        try {
            for (int i = 0; i < personajes.length(); i++) {
                agregarCardPersonaje(personajes.getJSONObject(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void agregarCardPersonaje(JSONObject personaje) {
        try {
            CardView card = new CardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 20, 0, 0);
            card.setLayoutParams(params);
            card.setRadius(25f);
            card.setCardElevation(8f);
            card.setUseCompatPadding(true);
            card.setContentPadding(16, 16, 16, 16);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(8, 8, 8, 8);
            layout.setGravity(Gravity.CENTER_VERTICAL);

            ImageView imagen = new ImageView(this);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(200, 200);
            imagen.setLayoutParams(imgParams);
            imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);

            String imgStr = personaje.optString("img", "");
            if (imgStr.startsWith("http://") || imgStr.startsWith("https://")) {
                new Thread(() -> {
                    try {
                        URL url = new URL(imgStr);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        is.close();

                        runOnUiThread(() -> imagen.setImageBitmap(bitmap));
                    } catch (Exception e) {
                        Log.d("PERSONAJE", "Error cargando imagen: " + e.getMessage());
                    }
                }).start();
            } else {
                String nombreImagen = imgStr.replace(".jpg", "").toLowerCase().replace(" ", "_");
                int resID = getResources().getIdentifier(nombreImagen, "drawable", getPackageName());
                if (resID != 0) {
                    imagen.setImageResource(resID);
                }
            }

            layout.addView(imagen);

            LinearLayout textoLayout = new LinearLayout(this);
            textoLayout.setOrientation(LinearLayout.VERTICAL);
            textoLayout.setPadding(16, 0, 0, 0);
            LinearLayout.LayoutParams textoParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            textoLayout.setLayoutParams(textoParams);

            TextView nombre = new TextView(this);
            nombre.setText(personaje.optString("nombre", "Sin nombre"));
            nombre.setTextSize(18);
            nombre.setTypeface(null, android.graphics.Typeface.BOLD);
            textoLayout.addView(nombre);

            TextView rol = new TextView(this);
            rol.setText("Rol: " + personaje.optString("rol", ""));
            rol.setTextSize(16);
            textoLayout.addView(rol);

            TextView caracteristica = new TextView(this);
            caracteristica.setText(personaje.optString("caracteristica", ""));
            caracteristica.setTextSize(14);
            textoLayout.addView(caracteristica);

            layout.addView(textoLayout);

            ImageView eliminar = new ImageView(this);
            eliminar.setImageResource(R.drawable.delete);
            LinearLayout.LayoutParams eliminarParams = new LinearLayout.LayoutParams(80, 80);
            eliminarParams.setMargins(16, 0, 0, 0);
            eliminar.setLayoutParams(eliminarParams);
            eliminar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            eliminar.setOnClickListener(v -> contenedorPersonajes.removeView(card));
            layout.addView(eliminar);

            card.addView(layout);
            contenedorPersonajes.addView(card);

            // ------------------ ABRIR DETALLE ------------------
            card.setOnClickListener(v -> {
    try {
        Intent intent = new Intent(MainActivity.this, CardpersonajesActivity.class);
        intent.putExtra("nombre", personaje.optString("nombre", ""));
        intent.putExtra("rol", personaje.optString("rol", ""));
        intent.putExtra("caracteristica", personaje.optString("caracteristica", ""));
        intent.putExtra("img", personaje.optString("img", ""));

        // Tomar todas las frases y mandarlas como String
        JSONArray frasesArray = personaje.optJSONArray("frases");
        String frasesConcatenadas = "";
        if (frasesArray != null) {
            for (int i = 0; i < frasesArray.length(); i++) {
                frasesConcatenadas += frasesArray.getString(i);
                if (i != frasesArray.length() - 1) frasesConcatenadas += "\n"; // salto de línea
            }
        }
        intent.putExtra("frases", frasesConcatenadas);

        startActivity(intent);
    } catch (Exception e) {
        e.printStackTrace();
    }
});

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
