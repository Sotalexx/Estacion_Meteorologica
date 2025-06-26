package com.example.estacion_meteorologica;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference estacionRef;

/*    LineChart chartTemperatura;*/
    private CircularProgressIndicator gaugeHumedad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        estacionRef = FirebaseDatabase.getInstance()
                .getReference("estacion")
                .child("datos");
        leerUltimoRegistro();



        CircularProgressIndicator circularProgress = findViewById(R.id.gaugePresionMar);

        gaugeHumedad = findViewById(R.id.gaugePresionLocal);

        // Establece el valor de humedad
        float humedadActual = 88;
        float humedad = 130;
        gaugeHumedad.setProgress(humedadActual, humedad);

// you can set max and current progress values individually
        circularProgress.setMaxProgress(100);
        circularProgress.setCurrentProgress(88);
// or all at once
        circularProgress.setProgress(100, 130);

// you can get progress values using following getters
        circularProgress.getProgress(); // returns 5000
        circularProgress.getMaxProgress(); // returns 10000

/*        chartTemperatura = findViewById(R.id.chartTemperatura);
        simularGrafica();*/


    }


    private void leerUltimoRegistro() {
        estacionRef.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot datoSnapshot : snapshot.getChildren()) {
                    String fecha = datoSnapshot.child("fecha").getValue(String.class);
                    String hora = datoSnapshot.child("hora").getValue(String.class);
                    Double temperatura = datoSnapshot.child("temperatura").getValue(Double.class);
                    Double humedad = datoSnapshot.child("humedad").getValue(Double.class);
                    Double sensacionTermica = datoSnapshot.child("sensacion_termica").getValue(Double.class);
                    Double presionBarometrica = datoSnapshot.child("presion_local").getValue(Double.class);
                    Double presionNivelMar = datoSnapshot.child("presion_nivel_mar").getValue(Double.class);
                    Double altitud = datoSnapshot.child("altitud_calculada").getValue(Double.class);
                    Double viento = datoSnapshot.child("viento_kmh").getValue(Double.class);
                    Integer lluvia = datoSnapshot.child("lluvia").getValue(Integer.class);
                    Integer gas = datoSnapshot.child("lpg_ppm").getValue(Integer.class);
                    Integer tendencia = datoSnapshot.child("presion_tendencia").getValue(Integer.class);
                    Integer monoxido = datoSnapshot.child("co_ppm").getValue(Integer.class);
                    Integer humo = datoSnapshot.child("smoke_ppm").getValue(Integer.class);
                    Integer rocio = datoSnapshot.child("punto_rocio").getValue(Integer.class);
                    Integer indiceCalor = datoSnapshot.child("indice_calor").getValue(Integer.class);
                    Integer sueloHumedad = datoSnapshot.child("suelo_humedad").getValue(Integer.class);

                    TextView tvFecha = findViewById(R.id.tvDate);
                    TextView tvHora = findViewById(R.id.tvTime);
                    TextView tvTemperatura = findViewById(R.id.tvTemperature);
                    TextView tvSensasion = findViewById(R.id.tvRealFeel);
                    TextView tvLluvia = findViewById(R.id.tvRainProbability);
                    TextView tvTemp = findViewById(R.id.tvTempCard1);
                    TextView tvViento = findViewById(R.id.tvWindCard1);
                    TextView tvHumedad = findViewById(R.id.tvHumedadCard1);
                    TextView tvPresionL = findViewById(R.id.tvPresionLocal);
                    TextView tvPresionM = findViewById(R.id.tvPresionMar);
                    TextView tvAltitud = findViewById(R.id.tvAltitud);
                    TextView tvTendencia = findViewById(R.id.tvTendenciaPresion);
                    TextView tvGas = findViewById(R.id.tvGasLPG);
                    TextView tvMonoxido = findViewById(R.id.tvMonoxido);
                    TextView tvHumo = findViewById(R.id.tvHumo);
                    TextView tvHumedadSuelo = findViewById(R.id.tvHumedadSuelo);
                    TextView tvHumeda = findViewById(R.id.tvHumedad);
                    TextView tvRocio = findViewById(R.id.tvPuntoRocio);
                    TextView tvIndice = findViewById(R.id.tvIndiceCalor);
                    TextView tvVient = findViewById(R.id.tvViento);


                    tvFecha.setText(fecha);
                    tvHora.setText(hora);
                    tvTemp.setText(temperatura + "°C");
                    tvTemperatura.setText(temperatura + "°C");
                    tvSensasion.setText(sensacionTermica + "°C");
                    tvLluvia.setText(lluvia + "%");
                    tvViento.setText(viento + " km/h");
                    tvVient.setText(viento + " km/h");
                    tvLluvia.setText(lluvia + "%");
                    tvHumedad.setText(humedad + "%");
                    tvPresionL.setText(presionBarometrica + " hPa");
                    tvPresionM.setText(presionNivelMar + " hPa");
                    tvTendencia.setText(tendencia + " hPa");
                    tvGas.setText(gas + " ppm");
                    tvMonoxido.setText(monoxido + " ppm");
                    tvHumo.setText(humo + " ppm");
                    tvHumedadSuelo.setText(sueloHumedad + " %");
                    tvHumeda.setText(humedad + " %");
                    tvRocio.setText(rocio + "°");
                    tvIndice.setText(String.valueOf(indiceCalor));
                    tvAltitud.setText(altitud + " m");



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error al leer", error.toException());
            }
        });
    }



}


/*    private void simularGrafica() {
        ArrayList<Entry> entradas = new ArrayList<>();
        entradas.add(new Entry(0, 26f));
        entradas.add(new Entry(1, 26f));
        entradas.add(new Entry(2, 25f));
        entradas.add(new Entry(3, 24f));
        entradas.add(new Entry(4, 24f));

        LineDataSet dataSet = new LineDataSet(entradas, "Temperatura");
        dataSet.setColor(Color.YELLOW);
        dataSet.setCircleColor(Color.WHITE);
        dataSet.setLineWidth(2f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#FFBB33"));
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chartTemperatura.setData(lineData);
        chartTemperatura.getDescription().setEnabled(false);
        chartTemperatura.getLegend().setEnabled(false);

        chartTemperatura.invalidate(); // Actualizar gráfico
    }*/


