package com.example.estacion_meteorologica.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estacion_meteorologica.R;
import com.example.estacion_meteorologica.models.RegistroClima;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InformeAdapter extends RecyclerView.Adapter<InformeAdapter.InformeViewHolder> {

    private List<RegistroClima> listaInformes;

    public InformeAdapter(List<RegistroClima> listaInformes) {
        this.listaInformes = listaInformes;
    }

    @NonNull
    @Override
    public InformeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new InformeViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull InformeViewHolder holder, int position) {
        RegistroClima r = listaInformes.get(position);

        // Alternar colores pastel
        int color1 = Color.parseColor("#C3E2FE");
        int color2 = Color.parseColor("#E3EEFA");

        int[] coloresPastel = {
                Color.parseColor("#C3E2FE"),
                Color.parseColor("#F9D5E5"),
                Color.parseColor("#D5F4E6"),
                Color.parseColor("#E3EEFA"),
                Color.parseColor("#FFFACD"),
                Color.parseColor("#8CFFFFFF")
        };

        int color = coloresPastel[position % coloresPastel.length];
        holder.cardView.setBackgroundColor(color);

        if (holder.tvTemperatura == null) {
            Log.e("InformeAdapter", "tvTemperatura es null en posición: " + position);
        }
        else
        {
            Log.e("InformeAdapter No es NULL", "tvTemperatura es null en posición: " + position);
        }

        SimpleDateFormat entradaHora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        SimpleDateFormat salidaHora = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        try {
            Date hora = entradaHora.parse(r.hora);
            holder.tvHora.setText(salidaHora.format(hora));
        } catch (ParseException e) {
            e.printStackTrace();
            holder.tvHora.setText(r.hora);
        }

        holder.tvHora.setText("Hora: " + r.hora);
        holder.tvTemperatura.setText("Temperatura: " + r.temperatura + "°C");
        holder.tvRocio.setText("Punto de rocío: " + r.rocio + "°C");
        holder.tvSensacion.setText("Sensación térmica: " + r.sensacionTermica + "°C");
        holder.tvIndiceCalor.setText("Índice de calor: " + r.indiceCalor);
        holder.tvHumedad.setText("Humedad: " + r.humedad + "%");
        holder.tvLluvia.setText("Lluvia: " + r.lluvia + "%");
        holder.tvHumedadSuelo.setText("Suelo: " + r.humedadSuelo + "%");
        holder.tvPresionMar.setText("Presión mar: " + r.presionMar + " hPa");
        holder.tvPresionLocal.setText("Presión local: " + r.presionLocal + " hPa");
        holder.tvViento.setText("Viento: " + r.viento + " km/h");
        holder.tvTendenciaPresion.setText("Tendencia: " + r.tendencia + " hPa");
        holder.tvAltitud.setText("Altitud: " + r.altitud + " m");
        holder.tvGasLPG.setText("LPG: " + r.gas + " ppm");
        holder.tvCO.setText("CO: " + r.monoxido + " ppm");
        holder.tvHumo.setText("Humo: " + r.humo + " ppm");

    }

    @Override
    public int getItemCount() {
        return listaInformes.size();
    }

    public static class InformeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvHora, tvTemperatura, tvRocio, tvSensacion, tvIndiceCalor,
                tvHumedad, tvLluvia, tvHumedadSuelo, tvPresionMar, tvPresionLocal,
                tvViento, tvTendenciaPresion, tvAltitud, tvGasLPG, tvCO, tvHumo;


        public InformeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHora = itemView.findViewById(R.id.tvHora);
            tvTemperatura = itemView.findViewById(R.id.tvTemperatura);
            tvRocio = itemView.findViewById(R.id.tvRocio);
            tvSensacion = itemView.findViewById(R.id.tvSensacion);
            tvIndiceCalor = itemView.findViewById(R.id.tvIndiceCalor);
            tvHumedad = itemView.findViewById(R.id.tvHumedad);
            tvLluvia = itemView.findViewById(R.id.tvLluvia);
            tvHumedadSuelo = itemView.findViewById(R.id.tvHumedadSuelo);
            tvPresionMar = itemView.findViewById(R.id.tvPresionMar);
            tvPresionLocal = itemView.findViewById(R.id.tvPresionLocal);
            tvViento = itemView.findViewById(R.id.tvViento);
            tvTendenciaPresion = itemView.findViewById(R.id.tvTendenciaPresion);
            tvAltitud = itemView.findViewById(R.id.tvAltitud);
            tvGasLPG = itemView.findViewById(R.id.tvGasLPG);
            tvCO = itemView.findViewById(R.id.tvCO);
            tvHumo = itemView.findViewById(R.id.tvHumo);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    public void actualizarDatos(List<RegistroClima> nuevosDatos) {
        listaInformes = nuevosDatos;
        notifyDataSetChanged();
    }
}

