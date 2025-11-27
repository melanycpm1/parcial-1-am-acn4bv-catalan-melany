package com.example.wikison;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
    int cantidadVisible = 2;

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
                String desc = ((EditText) contenedorEdittextsLugares.getChildAt(1)).getText().toString();
                String url = ((EditText) contenedorEdittextsLugares.getChildAt(2)).getText().toString();

                JSONObject lugar = new JSONObject();
                lugar.put("nombre", nombre);
                lugar.put("img", url);

                lugares.put(lugares.length(), lugar);
                agregarCardLugar(lugar);

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

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(jsonBuilder.toString());
                personajes = json.getJSONArray("personajes");

                // Corregido: revisamos si existe "lugares"
                if (json.has("lugares")) {
                    lugares = json.getJSONArray("lugares");
                } else {
                    lugares = new JSONArray(); // Si no hay, dejamos vacío
                }

                runOnUiThread(() -> {
                    mostrarPersonajesIniciales(); // Función original
                    mostrarLugaresIniciales();    // Función corregida
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        btnAgregar.setOnClickListener(v -> mostrarMasPersonajes());
    }

    // --------------------------- LUGARES --------------------------------

    private void mostrarLugaresIniciales() {
        try {
            contenedorLugares.removeAllViews();

            int mostrar = Math.min(2, lugares.length());
            for (int i = 0; i < mostrar; i++) {
                JSONObject lugar = lugares.getJSONObject(i);
                Log.d("LUGAR", lugar.toString()); // Debug
                agregarCardLugar(lugar);
            }

            // Si hay más de 2, agregamos "Ver más"
            if (lugares.length() > 2) {
                Button btnVerMas = new Button(this);
                btnVerMas.setText(R.string.ver_mas);
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
        try {
            contenedorLugares.removeAllViews();
            for (int i = 0; i < lugares.length(); i++) {
                agregarCardLugar(lugares.getJSONObject(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void agregarCardLugar(JSONObject lugar) {
    try {
        // Obtener el nombre de la imagen
        String nombreImagen = lugar.optString("img", "")
                .replace(".jpg", "")   // quitar extensión
                .toLowerCase()         // minúsculas
                .replace(" ", "_");    // espacios por guiones bajos

        int resID = getResources().getIdentifier(nombreImagen, "drawable", getPackageName());

        // Si no existe la imagen, no hacemos nada
        if (resID == 0) {
            Log.d("LUGAR", "No se encontró imagen para: " + lugar.optString("nombre"));
            return;
        }

        // ------------------ Crear la Card normalmente ------------------
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
        imagen.setImageResource(resID);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(200, 200);
        imagen.setLayoutParams(imgParams);
        imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);
        layout.addView(imagen);

        // Layout de texto
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
    //Mantengo todas las funciones originales de personajes
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
            imagen.setImageResource(resID);
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