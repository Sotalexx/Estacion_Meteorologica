package com.example.estacion_meteorologica;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.estacion_meteorologica.adapters.InformeAdapter;
import com.example.estacion_meteorologica.models.RegistroClima;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class ReportsFragment extends Fragment {

    private DatabaseReference databaseReference;
    private RecyclerView recyclerInformes;
    private InformeAdapter adapter;
    private Button btnSeleccionarFecha, btnExportarPdf;
    private TextView fechaSeleccionadaa;
    private List<RegistroClima> registrosDelDia = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        //Find elementoss
        recyclerInformes = view.findViewById(R.id.recyclerInformes);
        recyclerInformes.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InformeAdapter(new ArrayList<>());
        recyclerInformes.setAdapter(adapter);
        btnSeleccionarFecha = view.findViewById(R.id.btnSeleccionarFecha);
        fechaSeleccionadaa = view.findViewById(R.id.tvFechaSeleccionada);
        btnExportarPdf = view.findViewById(R.id.btnExportarPdf);


        btnExportarPdf.setOnClickListener(v -> {
            if (!registrosDelDia.isEmpty()) {
                exportarInformeAPdf(registrosDelDia);
            } else {
                Toast.makeText(getContext(), "Primero selecciona una fecha con datos", Toast.LENGTH_SHORT).show();
            }
        });


        //Seleccionar fecha
        btnSeleccionarFecha.setOnClickListener(v -> mostrarSelectorFecha());
        //Conectar Base
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("estacion")
                .child("datos");

        return view;
    }

    private void mostrarSelectorFecha() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    String fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    fechaSeleccionadaa.setText(fechaSeleccionada);
                    filtrarPorFecha(fechaSeleccionada);
                    leerRegistrosDelDia(getView(), fechaSeleccionada);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.show();
    }

    private void filtrarPorFecha(String fechaSeleccionada) {
        registrosDelDia.clear();

        databaseReference.orderByChild("fecha").equalTo(fechaSeleccionada)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(getContext(), "No hay datos para esta fecha", Toast.LENGTH_SHORT).show();
                            adapter.actualizarDatos(new ArrayList<>());
                            return;
                        }

                        List<RegistroClima> listaTemporal = new ArrayList<>();

                        for (DataSnapshot datoSnapshot : snapshot.getChildren()) {
                            RegistroClima r = obtenerDatosDeSnapshot(datoSnapshot);
                            listaTemporal.add(r);
                        }

                        // Ordenar por hora si es necesario
                        Collections.sort(listaTemporal, (a, b) -> a.hora.compareTo(b.hora));

                        registrosDelDia.addAll(listaTemporal);
                        adapter.actualizarDatos(registrosDelDia); // Si usas adapter para el RecyclerView
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error: ", error.toException());
                    }
                });
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
        r.pais = snapshot.child("ciudad").getValue(String.class);
        r.ciudad = snapshot.child("pais").getValue(String.class);
        return r;
    }

    private void leerRegistrosDelDia(View view, String fechaSeleccionada) {
        databaseReference.orderByChild("fecha").equalTo(fechaSeleccionada)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<RegistroClima> registrosDelDia = new ArrayList<>();
                        for (DataSnapshot datoSnapshot : snapshot.getChildren()) {
                            RegistroClima registro = obtenerDatosDeSnapshot(datoSnapshot);
                            if (registro != null) {
                                registrosDelDia.add(registro);
                            }
                        }

                        if (registrosDelDia.isEmpty()) {
                            Toast.makeText(getContext(), "No hay registros para la fecha seleccionada", Toast.LENGTH_SHORT).show();
                        }

                        adapter.actualizarDatos(registrosDelDia);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error al leer registros", error.toException());
                        Toast.makeText(getContext(), "Error al leer los registros", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void exportarInformeAPdf(List<RegistroClima> registros) {
        PdfDocument documento = new PdfDocument();
        Paint paint = new Paint();

        int paginaAncho = 595;  // A4 ancho en px
        int paginaAlto = 842;   // A4 alto en px
        int margen = 40;
        int y = 80; // posición vertical inicial

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(paginaAncho, paginaAlto, 1).create();
        PdfDocument.Page page = documento.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        canvas.drawText("Informe del Clima", margen, y, paint);

        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        y += 30;

        for (RegistroClima r : registros) {
            if (y > paginaAlto - 60) {
                documento.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(paginaAncho, paginaAlto, documento.getPages().size() + 1).create();
                page = documento.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 80;
            }

            canvas.drawText("Fecha: " + r.fecha + "  Hora: " + r.hora, margen, y, paint);
            y += 18;
            canvas.drawText("Temperatura: " + r.temperatura + "°C", margen, y, paint); y += 18;
            canvas.drawText("Humedad: " + r.humedad + "%", margen, y, paint); y += 18;
            canvas.drawText("Viento: " + r.viento + " km/h", margen, y, paint); y += 18;
            canvas.drawText("Presión Local: " + r.presionLocal + " hPa", margen, y, paint); y += 18;
            canvas.drawText("Lluvia: " + r.lluvia + "%", margen, y, paint); y += 30;
        }

        documento.finishPage(page);

        // === Guardar en carpeta DESCARGAS ===
        String nombreArchivo = "informe_clima_" + System.currentTimeMillis() + ".pdf";
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, nombreArchivo);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            documento.writeTo(fos);
            fos.close();
            documento.close();

            Toast.makeText(getContext(), "PDF exportado: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // === Abrir automáticamente ===
            Uri pdfUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al exportar PDF", Toast.LENGTH_SHORT).show();
        }
    }




}
