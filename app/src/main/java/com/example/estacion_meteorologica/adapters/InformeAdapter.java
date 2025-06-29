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

import java.util.List;

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
                Color.parseColor("#E3EEFA"),
                Color.parseColor("#F9D5E5"),
                Color.parseColor("#D5F4E6"),
                Color.parseColor("#FFFACD")
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

        holder.tvFecha.setText("Fecha: " + r.hora);
        holder.tvTemperatura.setText("Temperatura: " + r.temperatura + "°C");
        holder.tvHumedad.setText("Humedad: " + r.humedad + "%");
        holder.tvSensacion.setText("Sensación térmica: " + r.sensacionTermica + "°C");
        holder.tvLluvia.setText("Lluvia: " + r.lluvia + "%");
        holder.tvViento.setText("Viento: " + r.viento + " km/h");
        holder.tvPresion.setText("Presión: " + r.presionLocal + " hPa");
        holder.tvGases.setText("Gases: CO " + r.monoxido + " ppm, LPG " + r.gas + " ppm, Humo " + r.humo + " ppm");
    }

    @Override
    public int getItemCount() {
        return listaInformes.size();
    }

    public static class InformeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvFecha, tvTemperatura, tvHumedad, tvSensacion,
                tvLluvia, tvViento, tvPresion, tvGases;

        public InformeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvTemperatura = itemView.findViewById(R.id.tvTemperatura);
            tvHumedad = itemView.findViewById(R.id.tvHumedad);
            tvSensacion = itemView.findViewById(R.id.tvSensacion);
            tvLluvia = itemView.findViewById(R.id.tvLluvia);
            tvViento = itemView.findViewById(R.id.tvViento);
            tvPresion = itemView.findViewById(R.id.tvPresionLocal);
            tvGases = itemView.findViewById(R.id.tvGasLPG);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    public void actualizarDatos(List<RegistroClima> nuevosDatos) {
        listaInformes = nuevosDatos;
        notifyDataSetChanged();
    }
}

