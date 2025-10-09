package com.example.wikison;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        LinearLayout contenedor = findViewById(R.id.contenedor_edittexts);

        // Lista de textos diferentes
        String[] hints = {"Nombre", "rol", "caracteristica","url", "frase"};

        for (String hint : hints) {
            EditText editText = new EditText(this);

            // Atributos iguales a los del XML original
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    400, //
                    200
            );
            params.setMargins(0, 25, 0, 0);
            editText.setLayoutParams(params);

            editText.setHint(hint);
            editText.setBackgroundResource(R.drawable.edittext_border);
            editText.setPadding(16, 16, 16, 16);
            editText.setTextSize(16);
            editText.setTypeface(editText.getTypeface(), android.graphics.Typeface.BOLD);
            editText.setTextColor(getColor(android.R.color.black));

            // Agregamos al contenedor
            contenedor.addView(editText);
        }
    }
}