package com.example.estacion_meteorologica;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.estacion_meteorologica.adapters.InformeAdapter;
import com.example.estacion_meteorologica.models.RegistroClima;
import com.example.estacion_meteorologica.start.Welcome;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class ReportsFragment extends Fragment {

    private DatabaseReference databaseReference;
    private RecyclerView recyclerInformes;
    private InformeAdapter adapter;
    private Button btnSeleccionarFecha;
    private MaterialButton btnExportarPdf;
    private TextView tvDia, tvMes, tvAnio, tvSinRegistros;;
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
        tvDia = view.findViewById(R.id.tvDia);
        tvMes = view.findViewById(R.id.tvMes);
        tvAnio = view.findViewById(R.id.tvAnio);
        btnExportarPdf = view.findViewById(R.id.btnExportarPdf);
        tvSinRegistros = view.findViewById(R.id.tvSinRegistros);
        Button btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        Button btnPerfil = view.findViewById(R.id.btnPerfil);
        TextView tvLocation = view.findViewById(R.id.tvLocation);



        btnExportarPdf.setOnClickListener(v -> {
            if (!registrosDelDia.isEmpty()) {
                exportarInformeAPdf(registrosDelDia);
            } else {
                Toast.makeText(getContext(), "Primero selecciona una fecha con datos", Toast.LENGTH_SHORT).show();
            }
        });

        //Seleccionar fecha
        btnSeleccionarFecha.setOnClickListener(v -> mostrarSelectorFecha());

        // Botón de cerrar sesión
        btnCerrarSesion.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Cerrar sesión")
                    .setMessage("¿Estás seguro que deseas cerrar sesión?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();

                        SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.remove("recuerdame");
                        editor.remove("fechaPrefs");
                        editor.remove("fechaPrefs2");
                        editor.apply();

                        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(),
                                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(getString(R.string.client_id))
                                        .requestEmail()
                                        .build());
                        googleSignInClient.signOut();

                        Intent intent = new Intent(requireActivity(), Welcome.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Botón de perfil
        btnPerfil.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_perfil, null);

                ImageView imgPerfil = dialogView.findViewById(R.id.imgPerfil);
                TextView tvNombre = dialogView.findViewById(R.id.tvNombrePerfil);
                TextView tvCorreo = dialogView.findViewById(R.id.tvCorreoPerfil);

                tvNombre.setText(user.getDisplayName() != null ? user.getDisplayName() : "Nombre no disponible");
                tvCorreo.setText(user.getEmail());

                Uri photoUri = user.getPhotoUrl();
                if (photoUri != null) {
                    Glide.with(this)
                            .load(photoUri)
                            .circleCrop()
                            .placeholder(R.drawable.ic_user_placeholder)
                            .into(imgPerfil);
                } else {
                    imgPerfil.setImageResource(R.drawable.ic_user_placeholder);
                }

                new AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .setPositiveButton("Cerrar", null)
                        .show();
            } else {
                Toast.makeText(requireContext(), "No hay usuario en sesión", Toast.LENGTH_SHORT).show();
            }
        });

        //Conectar Base
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("estacion")
                .child("datos");

        SharedPreferences prefs = requireContext().getSharedPreferences("fechaPrefs", Context.MODE_PRIVATE);
        String fechaGuardada = prefs.getString("fechaSeleccionada", null);
        if (fechaGuardada != null) {
            try {
                String[] partes = fechaGuardada.split("-");
                int anio = Integer.parseInt(partes[0]);
                int mes = Integer.parseInt(partes[1]);
                int dia = Integer.parseInt(partes[2]);

                Calendar calendar = Calendar.getInstance();
                calendar.set(anio, mes - 1, dia);

                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", new Locale("es", "ES"));
                String nombreMes = monthFormat.format(calendar.getTime());

                tvAnio.setText(String.valueOf(anio));
                tvMes.setText(nombreMes.toUpperCase());
                tvDia.setText(String.valueOf(dia));

                filtrarPorFecha(fechaGuardada);
                leerRegistrosDelDia(view, fechaGuardada);
            } catch (Exception e) {
                Log.e("ReportsFragment", "Error al analizar fecha guardada", e);
            }
        }

        if (registrosDelDia.isEmpty() ) {
            tvSinRegistros.setVisibility(View.VISIBLE);
        } else {
            tvSinRegistros.setVisibility(View.GONE);
        }

        return view;
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
                        if (registrosDelDia.isEmpty() ) {
                            tvSinRegistros.setVisibility(View.VISIBLE);
                        } else {
                            tvSinRegistros.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error al leer registros", error.toException());
                        Toast.makeText(getContext(), "Error al leer los registros", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarSelectorFecha() {
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

            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", new Locale("es", "ES"));
            String nombreMes = monthFormat.format(calendar.getTime());

            tvAnio.setText(String.valueOf(year));
            tvMes.setText(nombreMes.toUpperCase());
            tvDia.setText(String.valueOf(day));

            String fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            filtrarPorFecha(fechaSeleccionada);
            leerRegistrosDelDia(getView(), fechaSeleccionada);
            if (registrosDelDia.isEmpty()) {
                tvSinRegistros.setVisibility(View.VISIBLE);
            } else {
                tvSinRegistros.setVisibility(View.GONE);
            }

            SharedPreferences prefs = requireContext().getSharedPreferences("fechaPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("fechaSeleccionada", fechaSeleccionada).apply();

        });

    }

    private void filtrarPorFecha(String fechaSeleccionada) {
        registrosDelDia.clear();

        databaseReference.orderByChild("fecha").equalTo(fechaSeleccionada)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            adapter.actualizarDatos(new ArrayList<>());
                            return;
                        }

                        List<RegistroClima> listaTemporal = new ArrayList<>();

                        for (DataSnapshot datoSnapshot : snapshot.getChildren()) {
                            RegistroClima r = obtenerDatosDeSnapshot(datoSnapshot);
                            listaTemporal.add(r);
                        }

                        Collections.sort(listaTemporal, (a, b) -> a.hora.compareTo(b.hora));

                        registrosDelDia.addAll(listaTemporal);
                        adapter.actualizarDatos(registrosDelDia);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error: ", error.toException());
                    }
                });
    }

    private void exportarInformeAPdf(List<RegistroClima> registros) {
        if (registros == null || registros.isEmpty()) return;

        PdfDocument documento = new PdfDocument();
        Paint paint = new Paint();
        Paint divider = new Paint();

        divider.setColor(Color.LTGRAY);
        divider.setStrokeWidth(1f);

        int margen = 57;
        int paginaAncho = 595;
        int paginaAlto = 842;
        int y = 80;

        int registrosPorPagina = 2;
        int contadorRegistros = 0;
        int colorGris = ContextCompat.getColor(requireContext(), R.color.gris);
        int colorNegro = ContextCompat.getColor(requireContext(), R.color.black);

        String fechaTitulo = registros.get(0).fecha;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(paginaAncho, paginaAlto, 1).create();
        PdfDocument.Page page = documento.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Título principal
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        String titulo = "📊 Informe del Clima";
        String subtitulo = fechaTitulo;
        float textoAncho = paint.measureText(titulo);
        float xCentrado = (paginaAncho - textoAncho) / 2;
        canvas.drawText(titulo, xCentrado, y, paint);
        y += 22;
        paint.setColor(colorGris);
        textoAncho = paint.measureText(subtitulo);
        xCentrado = (paginaAncho - textoAncho) / 2;
        canvas.drawText(subtitulo, xCentrado, y, paint);
        y += 50;

        paint.setTextSize(12);
        paint.setFakeBoldText(false);

        for (RegistroClima r : registros) {
            if (contadorRegistros == registrosPorPagina) {
                documento.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(paginaAncho, paginaAlto, documento.getPages().size() + 1).create();
                page = documento.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 80;
                contadorRegistros = 0;
            }

            paint.setColor(colorNegro);
            // --- Hora ---
            canvas.drawText("🕓 Hora: " + r.hora, margen, y, paint);
            y += 20;

            int col1X = margen;
            int col2X = paginaAncho / 2 + 10;

            // --- Clima General ---
            paint.setFakeBoldText(true);
            canvas.drawText("Clima General", margen, y, paint);
            paint.setFakeBoldText(false); y += 18;
            canvas.drawText("Temperatura: " + r.temperatura + "°C", col1X, y, paint);
            canvas.drawText("Sensación térmica: " + r.sensacionTermica + "°C", col2X, y, paint);
            y += 18;
            canvas.drawText("Punto de rocío: " + r.rocio + "°C", col1X, y, paint);
            canvas.drawText("Índice de calor: " + r.indiceCalor + "°C", col2X, y, paint);
            y += 25;

            // --- Humedad y Lluvia ---
            paint.setFakeBoldText(true);
            canvas.drawText("Humedad y Lluvia", margen, y, paint);
            paint.setFakeBoldText(false); y += 18;
            canvas.drawText("Humedad: " + r.humedad + "%", col1X, y, paint);
            canvas.drawText("Lluvia: " + r.lluvia + "%", col2X, y, paint);
            y += 18;
            canvas.drawText("Humedad del suelo: " + r.humedadSuelo + "%", col1X, y, paint);
            y += 25;

            // --- Viento y Presión ---
            paint.setFakeBoldText(true);
            canvas.drawText("Viento y Presión", margen, y, paint);
            paint.setFakeBoldText(false); y += 18;
            canvas.drawText("Viento: " + r.viento + " km/h", col1X, y, paint);
            canvas.drawText("Presión local: " + r.presionLocal + " hPa", col2X, y, paint);
            y += 18;
            canvas.drawText("Presión mar: " + r.presionMar + " hPa", col1X, y, paint);
            canvas.drawText("Tendencia: " + r.tendencia + " hPa", col2X, y, paint);
            y += 18;
            canvas.drawText("Altitud: " + r.altitud + " m", col1X, y, paint);
            y += 25;

            // --- Gases ---
            paint.setFakeBoldText(true);
            canvas.drawText("Gases", margen, y, paint);
            paint.setFakeBoldText(false); y += 18;
            canvas.drawText("LPG: " + r.gas + " ppm", col1X, y, paint);
            canvas.drawText("Monóxido: " + r.monoxido + " ppm", col2X, y, paint);
            y += 18;
            canvas.drawText("Humo: " + r.humo + " ppm", col1X, y, paint);
            y += 20;


            if (contadorRegistros == 0) {
                // Línea divisoria
                canvas.drawLine(margen, y, paginaAncho - margen, y, divider);
                y += 25;
            }

            contadorRegistros++;
        }

        documento.finishPage(page);

        // Guardar PDF
        String nombreArchivo = "informe_clima_" + System.currentTimeMillis() + ".pdf";
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, nombreArchivo);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            documento.writeTo(fos);
            fos.close();
            documento.close();

            Toast.makeText(getContext(), "PDF exportado en Descargas", Toast.LENGTH_LONG).show();

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

}
