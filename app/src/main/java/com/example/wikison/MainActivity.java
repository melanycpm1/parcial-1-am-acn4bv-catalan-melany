package com.example.wikison;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    LinearLayout contenedorEdittexts, contenedorPersonajes;
    Button btnAgregar;
    String[] hints = {"Nombre", "Rol", "Característica", "URL", "Frase"};
    JSONArray personajes;

    private LinearLayout contenedorEdittextsLugares;
    private LinearLayout contenedorLugares;

    private Button btnAgregarLugar;

    private JSONArray lugares;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        contenedorEdittextsLugares = findViewById(R.id.contenedorEdittextsLugares);
        contenedorLugares = findViewById(R.id.contenedorLugares);
        btnAgregarLugar = findViewById(R.id.btnAgregarLugar);

        lugares = new JSONArray(); // Inicializo vacío para agregar manualmente

        contenedorEdittexts = findViewById(R.id.contenedor_edittexts);
        contenedorPersonajes = findViewById(R.id.contenedor_personajes);
        btnAgregar = findViewById(R.id.btn_agregar);

        String[] hintsLugares = {"Nombre del lugar", "URL de imagen"};

        // Crear EditTexts para agregar lugares
        for (String hint : hintsLugares) {
            EditText edit = new EditText(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    600,
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

        // Botón agregar lugar manualmente
        btnAgregarLugar.setOnClickListener(v -> {
            try {
                String nombre = ((EditText) contenedorEdittextsLugares.getChildAt(0)).getText().toString();
                String url = ((EditText) contenedorEdittextsLugares.getChildAt(1)).getText().toString();

                if (url.isEmpty()) return; // No agregar si no hay imagen

                JSONObject lugar = new JSONObject();
                lugar.put("nombre", nombre);
                lugar.put("img", url);

                lugares.put(lugares.length(), lugar);

                agregarCardLugar(lugar);

                // Limpiar campos
                ((EditText) contenedorEdittextsLugares.getChildAt(0)).setText("");
                ((EditText) contenedorEdittextsLugares.getChildAt(1)).setText("");

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 🔹 EditTexts personajes (sin cambios)
        for (String hint : hints) {
            EditText editText = new EditText(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    600,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 25, 0, 0);
            editText.setLayoutParams(params);
            editText.setHint(hint);
            editText.setBackgroundResource(R.drawable.edittext_border);
            editText.setPadding(16, 16, 16, 16);
            editText.setTextSize(16);
            editText.setTextColor(getColor(android.R.color.black));
            contenedorEdittexts.addView(editText);
        }

        // Hilo para obtener personajes y lugares desde la API
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

        btnAgregar.setOnClickListener(v -> mostrarMasPersonajes());
    }

    // --------------------------- LUGARES --------------------------------

    private void mostrarLugaresIniciales() {
        contenedorLugares.removeAllViews();
        try {
            int mostrar = Math.min(2, lugares.length());
            for (int i = 0; i < mostrar; i++) {
                JSONObject lugar = lugares.getJSONObject(i);
                agregarCardLugar(lugar);
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
                // Cargar imagen desde internet
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
                        Log.d("LUGAR", "Error cargando imagen de internet: " + e.getMessage());
                    }
                }).start();
            } else {
                // Intentar cargar imagen desde drawable
                String nombreImagen = imgStr.replace(".jpg", "").toLowerCase().replace(" ", "_");
                int resID = getResources().getIdentifier(nombreImagen, "drawable", getPackageName());
                if (resID == 0) {
                    // Si no hay imagen válida, no mostramos la card
                    return;
                }
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

            // Icono eliminar
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

    // --------------------------- PERSONAJES --------------------------------
    private void mostrarPersonajesIniciales() {
        try {
            contenedorPersonajes.removeAllViews();
            for (int i = 0; i < Math.min(2, personajes.length()); i++) {
                agregarCard(personajes.getJSONObject(i));
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
        try {
            contenedorPersonajes.removeAllViews();
            for (int i = 0; i < personajes.length(); i++) {
                agregarCard(personajes.getJSONObject(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void agregarCard(JSONObject personaje) {
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
            int resID = getResources().getIdentifier(
                    personaje.getString("img").replace(".jpg", ""),
                    "drawable",
                    getPackageName()
            );
            if (resID != 0) imagen.setImageResource(resID);
            else return; // Si no hay imagen en drawable, no mostramos la card

            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(200, 200);
            imagen.setLayoutParams(imgParams);

            LinearLayout textoLayout = new LinearLayout(this);
            textoLayout.setOrientation(LinearLayout.VERTICAL);
            textoLayout.setPadding(16, 0, 0, 0);
            LinearLayout.LayoutParams textoParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            textoLayout.setLayoutParams(textoParams);

            TextView nombre = new TextView(this);
            nombre.setText(personaje.getString("nombre"));
            nombre.setTextSize(18);
            nombre.setTextColor(getColor(android.R.color.black));
            nombre.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView rol = new TextView(this);
            rol.setText("Rol: " + personaje.getString("rol"));
            rol.setTextSize(16);

            TextView caracteristica = new TextView(this);
            caracteristica.setText(personaje.getString("caracteristica"));
            caracteristica.setTextSize(14);

            textoLayout.addView(nombre);
            textoLayout.addView(rol);
            textoLayout.addView(caracteristica);

            ImageView eliminar = new ImageView(this);
            eliminar.setImageResource(R.drawable.delete);
            LinearLayout.LayoutParams eliminarParams =
                    new LinearLayout.LayoutParams(80, 80);
            eliminarParams.setMargins(40, 0, 0, 0);
            eliminar.setLayoutParams(eliminarParams);
            eliminar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            eliminar.setOnClickListener(view -> contenedorPersonajes.removeView(card));

            layout.addView(imagen);
            layout.addView(textoLayout);
            layout.addView(eliminar);

            card.addView(layout);
            contenedorPersonajes.addView(card);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
