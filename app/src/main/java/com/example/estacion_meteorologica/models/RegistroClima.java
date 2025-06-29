package com.example.estacion_meteorologica.models;

public class RegistroClima {
    public String fecha;
    public String hora;
    public String ciudad;
    public String pais;
    public Double temperatura;
    public Double humedad;
    public Double sensacionTermica;
    public Double presionLocal;
    public  Double presionMar;
    public Double altitud;
    public Double viento;
    public Integer lluvia;
    public Integer gas;
    public Integer tendencia;
    public Integer monoxido;
    public Integer humo;
    public Integer rocio;
    public  Integer indiceCalor;
    public Integer humedadSuelo;

    public RegistroClima(String fecha, String hora, String ciudad, String pais, Double temperatura, Double humedad, Double sensacionTermica, Double presionLocal, Double presionMar, Double altitud, Double viento, Integer lluvia, Integer gas, Integer tendencia, Integer monoxido, Integer humo, Integer rocio, Integer indiceCalor, Integer humedadSuelo) {
        this.fecha = fecha;
        this.hora = hora;
        this.ciudad = ciudad;
        this.pais = pais;
        this.temperatura = temperatura;
        this.humedad = humedad;
        this.sensacionTermica = sensacionTermica;
        this.presionLocal = presionLocal;
        this.presionMar = presionMar;
        this.altitud = altitud;
        this.viento = viento;
        this.lluvia = lluvia;
        this.gas = gas;
        this.tendencia = tendencia;
        this.monoxido = monoxido;
        this.humo = humo;
        this.rocio = rocio;
        this.indiceCalor = indiceCalor;
        this.humedadSuelo = humedadSuelo;
    }

    public RegistroClima() {
    }
}
