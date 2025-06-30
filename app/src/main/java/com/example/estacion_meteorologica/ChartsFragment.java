package com.example.estacion_meteorologica;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.estacion_meteorologica.models.RegistroClima;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;


public class ChartsFragment extends Fragment {

    Button btnAplicarFiltro, btnFechaInicio, btnFechaFinal;
    private Date fechaInicio, fechaFin;
    private BarChart chartTemperatura;
    private LineChart chartVariacionDiaria;
    private PieChart chartContaminacionAire;
    private final SimpleDateFormat formatoFirebase = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private TextView tvDia, tvMes, tvAnio, tvDia1, tvMes1, tvAnio1;
    private DatabaseReference databaseReference;

    interface DataCallback {
        void onDatosPromedioListos(
                Map<String, Float> temp,
                Map<String, Float> humedad,
                Map<String, Float> sensacion,
                int lpg, int co, int humo
        );
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_charts, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnFechaInicio = view.findViewById(R.id.btnFechaInicio);
        btnFechaFinal = view.findViewById(R.id.btnFechaFinal);
        btnAplicarFiltro = view.findViewById(R.id.btnAplicarFiltro);
        chartTemperatura = view.findViewById(R.id.chartTemperatura);
        chartVariacionDiaria = view.findViewById(R.id.chartVariacionDiaria);
        chartContaminacionAire = view.findViewById(R.id.chartContaminacionAire);
        tvDia = view.findViewById(R.id.tvDia);
        tvMes = view.findViewById(R.id.tvMes);
        tvAnio = view.findViewById(R.id.tvAnio);
        tvDia1 = view.findViewById(R.id.tvDia1);
        tvMes1 = view.findViewById(R.id.tvMes1);
        tvAnio1 = view.findViewById(R.id.tvAnio1);

        btnFechaInicio.setOnClickListener(v -> mostrarSelectorFecha(tvAnio1, tvMes1, tvDia1, true));
        btnFechaFinal.setOnClickListener(v -> mostrarSelectorFecha(tvAnio, tvMes, tvDia, false));


        databaseReference = FirebaseDatabase.getInstance()
                .getReference("estacion")
                .child("datos");
        btnAplicarFiltro.setOnClickListener(v -> {
            if (fechaInicio != null && fechaFin != null) {
                consultarPromediosMultiplesVariables(fechaInicio, fechaFin, (temp, humedad, sensacion, lpg, co, humo) -> {
                    mostrarGraficoLineas(temp, sensacion, humedad);
                    mostrarGraficoDesdeMapa(temp);
                    mostrarPieChartContaminantes(chartContaminacionAire, lpg, co, humo);
                });
            } else {
                Toast.makeText(getContext(), "Selecciona una fecha primero", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void consultarPromediosMultiplesVariables(Date fechaInicio, Date fechaFin, DataCallback callback) {
        List<String> fechasRango = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaInicio);
        while (!cal.getTime().after(fechaFin)) {
            fechasRango.add(formatoFirebase.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        String fechaUltima = formatoFirebase.format(fechaFin); // Para gases contaminantes

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Map<String, List<Double>>> datos = new HashMap<>();
                String[] variables = {"temperatura", "humedad", "sensacion_termica"};

                for (String var : variables) {
                    datos.put(var, new HashMap<>());
                    for (String fecha : fechasRango) {
                        datos.get(var).put(fecha, new ArrayList<>());
                    }
                }

                // Acumuladores de contaminantes
                int lpg = 0, co = 0, humo = 0;

                for (DataSnapshot datoSnapshot : snapshot.getChildren()) {
                    String fecha = datoSnapshot.child("fecha").getValue(String.class);
                    if (fecha != null && fechasRango.contains(fecha)) {
                        for (String var : variables) {
                            Double valor = datoSnapshot.child(var).getValue(Double.class);
                            if (valor != null) {
                                datos.get(var).get(fecha).add(valor);
                            }
                        }

                        // Contaminantes solo si la fecha es la última
                        if (fecha.equals(fechaUltima)) {
                            Integer lpgVal = datoSnapshot.child("lpg_ppm").getValue(Integer.class);
                            Integer coVal = datoSnapshot.child("co_ppm").getValue(Integer.class);
                            Integer humoVal = datoSnapshot.child("smoke_ppm").getValue(Integer.class);

                            if (lpgVal != null) lpg += lpgVal;
                            if (coVal != null) co += coVal;
                            if (humoVal != null) humo += humoVal;
                        }
                    }
                }

                Map<String, Float> promedioTemp = new LinkedHashMap<>();
                Map<String, Float> promedioHumedad = new LinkedHashMap<>();
                Map<String, Float> promedioSensacion = new LinkedHashMap<>();

                for (String fecha : fechasRango) {
                    promedioTemp.put(fecha, calcularPromedio(datos.get("temperatura").get(fecha)));
                    promedioHumedad.put(fecha, calcularPromedio(datos.get("humedad").get(fecha)));
                    promedioSensacion.put(fecha, calcularPromedio(datos.get("sensacion_termica").get(fecha)));
                }

                callback.onDatosPromedioListos(promedioTemp, promedioHumedad, promedioSensacion, lpg, co, humo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al consultar datos", Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Error al consultar", error.toException());
            }
        });
    }

    private void mostrarPieChartContaminantes(PieChart pieChart, int lpg, int co, int humo) {
        List<PieEntry> entries = new ArrayList<>();

        if (lpg == 0 && co == 0 && humo == 0) {
            pieChart.clear();
            pieChart.setNoDataText("No hay contaminación registrada.");
            return;
        }

        if (lpg > 0) entries.add(new PieEntry(lpg, "Gas LPG"));
        if (co > 0) entries.add(new PieEntry(co, "Monóxido CO"));
        if (humo > 0) entries.add(new PieEntry(humo, "Humo"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{0xFFC3E2FE, 0xFF00BCD4, 0xFFF9D5E5});
        dataSet.setSliceSpace(3f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setCenterText("Última lectura");
        pieChart.getDescription().setEnabled(false);

        pieChart.getLegend().setForm(Legend.LegendForm.SQUARE);
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        pieChart.setExtraBottomOffset(10f);

        pieChart.animateY(1000, Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

    private float calcularPromedio(List<Double> lista) {
        if (lista == null || lista.isEmpty()) return 0f;
        double suma = 0;
        for (double v : lista) suma += v;
        return (float) (suma / lista.size());
    }

    private void mostrarGrafico(List<BarEntry> entries, List<String> etiquetas) {
        BarDataSet dataSet = new BarDataSet(entries, "Temperatura promedio (°C)");
        dataSet.setFormSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        // Eje X personalizado con las fechas
        XAxis xAxis = chartTemperatura.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(etiquetasDiaSolo(etiquetas)));

        // Estilo general del gráfico
        chartTemperatura.getAxisRight().setEnabled(false);
        chartTemperatura.getDescription().setEnabled(false);
        chartTemperatura.setTouchEnabled(false);
        chartTemperatura.setDragEnabled(false);
        chartTemperatura.setScaleEnabled(false);
        chartTemperatura.setHighlightPerTapEnabled(false);

        dataSet.setColors(new int[]{0xFFC3E2FE, 0xFF00BCD4, 0xFFF9D5E5, 0xFFE3EEFA, 0xFFD5F4E6});
        chartTemperatura.getLegend().setForm(Legend.LegendForm.SQUARE);
        chartTemperatura.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        chartTemperatura.setExtraBottomOffset(10f);

        // Eje Y izquierdo con símbolo de grados
        YAxis leftAxis = chartTemperatura.getAxisLeft();
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0f°", value);
            }
        });

        // Animación
        chartTemperatura.animateY(1000, Easing.EaseInOutQuad);
        chartTemperatura.animateXY(1000, 1000);

        // Cargar datos
        chartTemperatura.setData(barData);
        chartTemperatura.invalidate(); // refrescar
    }

    private void mostrarGraficoDesdeMapa(Map<String, Float> promedios) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Float> entry : promedios.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            etiquetas.add(entry.getKey());
            index++;
        }

        mostrarGrafico(entries, etiquetas);
    }

    private void mostrarGraficoLineas(Map<String, Float> temperaturaMap, Map<String, Float> sensacionMap, Map<String, Float> humedadMap) {

        List<Entry> tempEntries = new ArrayList<>();
        List<Entry> sensacionEntries = new ArrayList<>();
        List<Entry> humedadEntries = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();

        int index = 0;
        for (String fecha : temperaturaMap.keySet()) {
            tempEntries.add(new Entry(index, temperaturaMap.get(fecha)));
            sensacionEntries.add(new Entry(index, sensacionMap.get(fecha)));
            humedadEntries.add(new Entry(index, humedadMap.get(fecha)));
            etiquetas.add(fecha);
            index++;
        }

        LineDataSet tempSet = new LineDataSet(tempEntries, "Temperatura (°C)");
        tempSet.setColor(0xFF00BCD4);
        tempSet.setCircleColor(0xFF00BCD4);
        tempSet.setLineWidth(2f);

        LineDataSet sensacionSet = new LineDataSet(sensacionEntries, "Sensación térmica (°C)");
        sensacionSet.setColor(0xFFD5F4E6);
        sensacionSet.setCircleColor(0xFFD5F4E6);
        sensacionSet.setLineWidth(2f);

        LineDataSet humedadSet = new LineDataSet(humedadEntries, "Humedad (%)");
        humedadSet.setColor(0xFFF9D5E5);
        humedadSet.setCircleColor(0xFFF9D5E5);
        humedadSet.setLineWidth(2f);

        LineData data = new LineData(tempSet, sensacionSet, humedadSet);
        chartVariacionDiaria.setData(data);

        // Eje X personalizado con las fechas
        XAxis xAxis = chartVariacionDiaria.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(etiquetasDiaSolo(etiquetas)));

        // Estilo general del gráfico
        chartVariacionDiaria.getAxisRight().setEnabled(false);
        chartVariacionDiaria.getDescription().setEnabled(false);
        chartVariacionDiaria.setTouchEnabled(false);
        chartVariacionDiaria.setDragEnabled(false);
        chartVariacionDiaria.setScaleEnabled(false);
        chartVariacionDiaria.setHighlightPerTapEnabled(false);


        chartVariacionDiaria.getLegend().setForm(Legend.LegendForm.SQUARE);
        chartVariacionDiaria.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        chartVariacionDiaria.setExtraBottomOffset(10f);

        // Animación
        chartVariacionDiaria.animateY(1000, Easing.EaseInOutQuad);
        chartVariacionDiaria.animateXY(1000, 1000);

        // Cargar datos
        chartVariacionDiaria.invalidate(); // refrescar
    }

    private List<String> etiquetasDiaSolo(List<String> fechas) {
        List<String> dias = new ArrayList<>();
        for (String fecha : fechas) {
            String[] partes = fecha.split("-");
            dias.add(partes[2]);
        }
        return dias;
    }

    private void mostrarSelectorFecha(TextView tvAnio, TextView tvMes, TextView tvDia, boolean esInicio) {
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now());

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTheme(R.style.ThemeOverlay_MaterialDatePicker_WhiteBG)
                .setCalendarConstraints(constraintsBuilder.build())
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            String fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date fechaaa = sdf.parse(fechaSeleccionada);
                if (esInicio) {
                    fechaInicio = fechaaa;
                } else {
                    fechaFin = fechaaa;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }



            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", new Locale("es", "ES"));
            String nombreMes = monthFormat.format(calendar.getTime());

            tvAnio.setText(String.valueOf(year));
            tvMes.setText(nombreMes.toUpperCase());
            tvDia.setText(String.valueOf(day));

            Log.d("FechaSeleccionada", (esInicio ? "Inicio: " : "Fin: ") + calendar.getTime().toString());
        });
    }


}