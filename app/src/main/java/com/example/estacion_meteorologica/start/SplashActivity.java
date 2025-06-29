package com.example.estacion_meteorologica.start;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.estacion_meteorologica.MainActivity;
import com.example.estacion_meteorologica.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();


        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean recuerdame = prefs.getBoolean("recuerdame", false);

        Log.d("Splash", "Usuario actual: " + (currentUser != null ? currentUser.getEmail() : "null"));
        Log.d("Splash", "Recuérdame está en: " + recuerdame);

        // Intenta detectar sesión activa por Google Sign-In
        GoogleSignInAccount googleUser = GoogleSignIn.getLastSignedInAccount(this);
        if (googleUser != null && recuerdame) {
            Log.d("Splash", "Usuario de Google detectado: " + googleUser.getEmail());

            AuthCredential credential = GoogleAuthProvider.getCredential(googleUser.getIdToken(), null);
            FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d("Splash", "Autenticado en Firebase con Google: " + FirebaseAuth.getInstance().getCurrentUser().getEmail());
                            startActivity(new Intent(this, MainActivity.class));
                        } else {
                            Log.w("Splash", "Fallo al autenticar con Firebase usando Google", task.getException());
                            startActivity(new Intent(this, Login.class));
                        }
                        finish();
                    });
            return;
        }


        if (currentUser != null && recuerdame) {
            Log.d("Splash", "Usuario + recuerdame = OK. Vamos al MainActivity");
            startActivity(new Intent(this, MainActivity.class));
        } else {
            Log.d("Splash", "NO hay usuario o recuerdame desactivado. Vamos a Welcome");
            startActivity(new Intent(this, Welcome.class));
        }

        finish();
    }


}
