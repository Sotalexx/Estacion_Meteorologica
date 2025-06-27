package com.example.estacion_meteorologica;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ekn.gruzer.gaugelibrary.ArcGauge;
import com.ekn.gruzer.gaugelibrary.HalfGauge;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import com.ekn.gruzer.gaugelibrary.Range;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference estacionRef;
    private SwipeRefreshLayout swipeRefreshLayout;
    BarChart barChart;
    private CircularProgressIndicator gaugePresionL, gaugePresionMar, gaugeHumedad;
    private HalfGauge gaugeViento;
    private ArcGauge gaugeIndiceCalor;
    private TextView textoEstadoComodidad, textoDetalleComodidad;
    private ImageView iconoComodidad;

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

        findViews();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            leerUltimoRegistro();
        });

        swipeRefreshLayout.setColorSchemeResources(
                R.color.azul, R.color.aqua, R.color.morado);

        estacionRef = FirebaseDatabase.getInstance()
                .getReference("estacion")
                .child("datos");
        leerUltimoRegistro();

    }

    private void leerUltimoRegistro() {
        estacionRef.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot datoSnapshot : snapshot.getChildren()) {
                    RegistroClima registro = obtenerDatosDeSnapshot(datoSnapshot);
                    mostrarDatosEnTextViews(registro);
                    mostrarGraficas(registro);
                    configurarGaugeViento(registro.viento);
                    configurarGaugeIndiceCalor(registro.indiceCalor);
                    mostrarIndiceDeComodidad(registro);
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error al leer", error.toException());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    class RegistroClima {
        String fecha;
        String hora;
        Double temperatura;
        Double humedad;
        Double sensacionTermica;
        Double presionLocal;
        Double presionMar;
        Double altitud;
        Double viento;
        Integer lluvia;
        Integer gas;
        Integer tendencia;
        Integer monoxido;
        Integer humo;
        Integer rocio;
        Integer indiceCalor;
        Integer humedadSuelo;
    }

    private void findViews(){
        gaugePresionMar = findViewById(R.id.gaugePresionMar);
        gaugePresionL = findViewById(R.id.gaugePresionLocal);
        gaugeHumedad = findViewById(R.id.gaugeHumedad);
        gaugeViento = findViewById(R.id.gaugeVientos);
        gaugeIndiceCalor = findViewById(R.id.gaugeIndiceCalor);
        barChart = findViewById(R.id.barChart);
        textoEstadoComodidad = findViewById(R.id.textoEstadoComodidad);
        textoDetalleComodidad = findViewById(R.id.textoDetalleComodidad);
        iconoComodidad = findViewById(R.id.iconoComodidad);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private RegistroClima obtenerDatosDeSnapshot(DataSnapshot snapshot) {
        RegistroClima r = new RegistroClima();
        r.fecha = snapshot.child("fecha").getValue(String.class);
        r.hora = snapshot.child("hora").getValue(String.class);
        r.temperatura = snapshot.child("temperatura").getValue(Double.class);
        r.humedad = snapshot.child("humedad").getValue(Double.class);
        r.sensacionTermica = snapshot.child("sensacion_termica").getValue(Double.class);
        r.presionLocal = snapshot.child("presion_local").getValue(Double.class);
        r.presionMar = snapshot.child("presion_nivel_mar").getValue(Double.class);
        r.altitud = snapshot.child("altitud_calculada").getValue(Double.class);
        r.viento = snapshot.child("viento_kmh").getValue(Double.class);
        r.lluvia = snapshot.child("lluvia_porcentaje").getValue(Integer.class);
        r.gas = snapshot.child("lpg_ppm").getValue(Integer.class);
        r.tendencia = snapshot.child("presion_tendencia").getValue(Integer.class);
        r.monoxido = snapshot.child("co_ppm").getValue(Integer.class);
        r.humo = snapshot.child("smoke_ppm").getValue(Integer.class);
        r.rocio = snapshot.child("punto_rocio").getValue(Integer.class);
        r.indiceCalor = snapshot.child("indice_calor").getValue(Integer.class);
        r.humedadSuelo = snapshot.child("suelo_humedad").getValue(Integer.class);
        return r;
    }

    private void mostrarDatosEnTextViews(RegistroClima r) {
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

        SimpleDateFormat entradaFecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat salidaFecha = new SimpleDateFormat("dd MMMM, yyyy", new Locale("es", "ES"));
        SimpleDateFormat entradaHora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        SimpleDateFormat salidaHora = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        try {
            Date fecha = entradaFecha.parse(r.fecha);
            tvFecha.setText(salidaFecha.format(fecha));
        } catch (ParseException e) {
            e.printStackTrace();
            tvFecha.setText(r.fecha);
        }

        try {
            Date hora = entradaHora.parse(r.hora);
            tvHora.setText(salidaHora.format(hora));
        } catch (ParseException e) {
            e.printStackTrace();
            tvHora.setText(r.hora);
        }

        tvTemp.setText(r.temperatura + "°C");
        tvTemperatura.setText(r.temperatura + "°C");
        tvSensasion.setText(r.sensacionTermica + "°C");
        tvLluvia.setText("Lluvia: " + r.lluvia + "%");
        tvViento.setText(r.viento + " km/h");
        tvVient.setText(r.viento + " km/h");
        tvHumedad.setText(r.humedad + "%");
        tvPresionL.setText(r.presionLocal + " hPa");
        tvPresionM.setText(r.presionMar + " hPa");
        tvTendencia.setText(r.tendencia + " hPa");
        tvGas.setText(r.gas + " ppm");
        tvMonoxido.setText(r.monoxido + " ppm");
        tvHumo.setText(r.humo + " ppm");
        tvHumedadSuelo.setText(r.humedadSuelo + " %");
        tvHumeda.setText(r.humedad + " %");
        tvRocio.setText(r.rocio + "°");
        tvIndice.setText(String.valueOf(r.indiceCalor));
        tvAltitud.setText(r.altitud + " m");
    }

    private void mostrarIndiceDeComodidad(RegistroClima r) {
        String estado = "";
        String detalle = "";
        int iconoResId = R.drawable.sunny_cloud;

        if (r.temperatura >= 20 && r.temperatura <= 26 && r.humedad >= 40 && r.humedad <= 65) {
            estado = "Ambiente templado y húmedo";
            detalle = "Condiciones agradables para actividades al aire libre.";

            // Considera viento suave o fuerte
            if (r.viento > 40) {
                detalle += " Viento fuerte, toma precauciones si haces actividades al aire libre.";
            } else if (r.viento > 20) {
                detalle += "Brisa agradable que refresca el ambiente.";
            }

            // Considera humedad suelo
            if (r.humedadSuelo < 10) {
                detalle += " Suelo muy seco, riego recomendado para plantas.";
            }

            iconoResId = R.drawable.sunny_cloud;

        } else if (r.temperatura > 26 && r.humedad > 65) {
            estado = "Ambiente caluroso y húmedo";
            detalle = "Puede sentirse sofocante y pegajoso, hidrátate bien.";

            if (r.viento > 30) {
                detalle += " Algo de viento ayuda a refrescar.";
            }
            if (r.humedadSuelo < 20) {
                detalle += " Suelo seco, cuidado con la sequía en plantas y jardines.";
            }

            iconoResId = R.drawable.arrow;

        } else if (r.temperatura < 20 && r.humedad > 65) {
            estado = "Ambiente fresco y húmedo";
            detalle = "El aire puede sentirse frío y húmedo, lleva ropa adecuada.";

            if (r.viento > 25) {
                detalle += " Viento frío podría aumentar la sensación de frío.";
            }
            if (r.humedadSuelo > 60) {
                detalle += " Suelo húmedo, condiciones ideales para cultivo.";
            }

            iconoResId = R.drawable.google;

        } else if (r.temperatura > 28 && r.humedad < 40) {
            estado = "Ambiente seco y caluroso";
            detalle = "Riesgo alto de deshidratación, evita esfuerzo físico y protege tu piel.";

            if (r.viento > 30) {
                detalle += " Viento seco y fuerte puede aumentar la sensación de calor.";
            }
            if (r.humedadSuelo < 15) {
                detalle += " Suelo muy seco, riego urgente necesario.";
            }

            iconoResId = R.drawable.ic_humedad_suelo;

        } else if (r.temperatura < 18 && r.humedad < 40) {
            estado = "Ambiente seco y frío";
            detalle = "El aire seco puede resecar la piel y vías respiratorias, usa hidratantes.";

            if (r.viento > 20) {
                detalle += " Viento frío puede incrementar la sensación de frío.";
            }
            if (r.humedadSuelo < 20) {
                detalle += " Suelo seco, condiciones no ideales para cultivos.";
            }

            iconoResId = R.drawable.check;

        } else {
            estado = "Condiciones variables";
            detalle = "No se detecta un patrón claro, mantente atento a los cambios del clima.";

            if (r.viento > 30) {
                detalle += " Viento fuerte puede afectar la sensación térmica.";
            }
            iconoResId = R.drawable.ic_home;
        }

        // Actualizar la UI
        textoEstadoComodidad.setText(estado);
        textoDetalleComodidad.setText(detalle);
        iconoComodidad.setImageResource(iconoResId);
    }


    private void mostrarGraficas(RegistroClima r) {
        float presionMax = 1084.8f;
        float humedadMax = 100f;
        gaugePresionL.setProgress(r.presionLocal, presionMax);
        gaugePresionMar.setProgress(r.presionMar, presionMax);
        gaugeHumedad.setProgress(r.humedad, humedadMax);

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, r.temperatura.floatValue()));
        entries.add(new BarEntry(1, r.rocio.floatValue()));

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(new int[]{0xFF348DF1, 0xFF00BCD4});
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setHighlightPerTapEnabled(false);
        barChart.getXAxis().setEnabled(false);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);

        barChart.getLegend().setEnabled(false);
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);
        barChart.setFitBars(true);
        barChart.invalidate();
    }

    private void configurarGaugeViento(Double viento) {
        agregarRangoGauge(gaugeViento, 0.0, 5.0, "#00BCD4");
        agregarRangoGauge(gaugeViento, 5.0, 25.0, "#348DF1");
        agregarRangoGauge(gaugeViento, 25.0, 38.0, "#3F51B5");
        agregarRangoGauge(gaugeViento, 38.0, 60.0, "#B71C1C");
        gaugeViento.setMinValue(0.0);
        gaugeViento.setMaxValue(60.0);
        gaugeViento.setValue(viento);
    }

    private void configurarGaugeIndiceCalor(Integer indice) {
        agregarRangoGauge(gaugeIndiceCalor, 0.0, 27.0, "#4CAF50");
        agregarRangoGauge(gaugeIndiceCalor, 27.0, 32.0, "#FFEB3B");
        agregarRangoGauge(gaugeIndiceCalor, 32.0, 39.0, "#FF9800");
        agregarRangoGauge(gaugeIndiceCalor, 39.0, 51.0, "#F1A134");
        agregarRangoGauge(gaugeIndiceCalor, 51.0, 60.0, "#B71C1C");
        gaugeIndiceCalor.setMinValue(0.0);
        gaugeIndiceCalor.setMaxValue(60.0);
        gaugeIndiceCalor.setValue(indice);
    }

    private void agregarRangoGauge(Object gauge, double from, double to, String colorHex) {
        Range rango = new Range();
        rango.setFrom(from);
        rango.setTo(to);
        rango.setColor(Color.parseColor(colorHex));

        if (gauge instanceof HalfGauge) {
            ((HalfGauge) gauge).addRange(rango);
        } else if (gauge instanceof ArcGauge) {
            ((ArcGauge) gauge).addRange(rango);
        }
    }

}