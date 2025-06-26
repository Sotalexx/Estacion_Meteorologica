package com.example.estacion_meteorologica;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;

public class MainActivity extends AppCompatActivity {

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


}