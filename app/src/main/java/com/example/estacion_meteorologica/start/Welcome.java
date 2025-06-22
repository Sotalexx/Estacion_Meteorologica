package com.example.estacion_meteorologica.start;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.estacion_meteorologica.R;
import com.example.estacion_meteorologica.models.ItemSlide;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class Welcome extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ExtendedFloatingActionButton btnNext;
    private List<ItemSlide> slideItems;
    private WelcomePagerAdapter adapter;
    private CircleIndicator3 indicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        ExtendedFloatingActionButton btnNext = findViewById(R.id.btn_next);
        CircleIndicator3 indicator = findViewById(R.id.indicator);


        List<ItemSlide> slideItems = new ArrayList<>();
        slideItems.add(new ItemSlide("Weather Nest", "Está es tu estación meteorológica personal impulsada por Arduino."));
        slideItems.add(new ItemSlide("Seguimiento de datos", "Monitorea la temperatura, la humedad de casa y más, desde cualquier lugar."));
        slideItems.add(new ItemSlide("Mantente informado", "Obten la ultima información y reportes del clima incluso sin conexión."));

        WelcomePagerAdapter adapter = new WelcomePagerAdapter(slideItems);
        viewPager.setAdapter(adapter);

        indicator.setViewPager(viewPager);

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < slideItems.size() - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                startActivity(new Intent(this, SignUp.class));
                finish();
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == slideItems.size() - 1) {
                    btnNext.setText("Comenzar");
                    btnNext.setIconResource(R.drawable.check);
                } else {
                    btnNext.setText("Siguiente");
                    btnNext.setIconResource(R.drawable.arrow);
                }
            }
        });
    }

}