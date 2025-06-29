package com.example.estacion_meteorologica.start;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.estacion_meteorologica.MainActivity;
import com.example.estacion_meteorologica.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private static final String TAG = "Login";

    private LinearLayout btnGoogle;
    private TextView btnIrSignUp, tvOlvidasteContrasena;
    private Button btnLogin, btnBack;
    private TextInputLayout email, password;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    MaterialCheckBox checkBoxRecuerdame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnBack = findViewById(R.id.btnBack);
        btnLogin = findViewById(R.id.btn_login);
        btnIrSignUp = findViewById(R.id.btnIrLogin);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btnGoogle = findViewById(R.id.btnGoogle);
        checkBoxRecuerdame = findViewById(R.id.checkBox);
        tvOlvidasteContrasena = findViewById(R.id.tvOlvidasteContrasena);

        auth = FirebaseAuth.getInstance();

        // Configurar Login con Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id)) //
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        //Boton de recuperar contraseña
        tvOlvidasteContrasena.setOnClickListener(v -> {
            String userEmail = email.getEditText().getText().toString().trim();

            if (userEmail.isEmpty()) {
                Toast.makeText(Login.this, "Ingresa tu correo para restablecer la contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(Login.this)
                    .setTitle("Restablecer contraseña")
                    .setMessage("¿Deseas restablecer tu contraseña?\nSe enviará un correo a:\n\n" + userEmail)
                    .setPositiveButton("Sí", (dialog, which) -> {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(userEmail)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Login.this, "Correo de recuperación enviado", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(Login.this, "No se pudo enviar el correo. Verifica el correo ingresado", Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        //Boton de regresar
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Boton de ir a SignUp
        btnIrSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, SignUp.class);
            startActivity(intent);
            finish();
        });

        //Boton de Iniciar Sesion
        btnLogin.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

            String userEmail = email.getEditText().getText().toString().trim();
            String userPassword = password.getEditText().getText().toString().trim();

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(Login.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                Log.d("Login", "Usuario iniciado: " + user.getEmail());

                                SharedPreferences.Editor editor = prefs.edit();

                                if (checkBoxRecuerdame.isChecked()) {
                                    editor.putBoolean("recuerdame", true);
                                    Log.d("Login", "Recuérdame activado. Preferencia guardada.");
                                } else {
                                    editor.remove("recuerdame");
                                    Log.d("Login", "Recuérdame desactivado. Preferencia eliminada.");
                                }
                                editor.apply();

                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                Log.d("Login", "Usuario tras login manual: " + (currentUser != null ? currentUser.getEmail() : "null"));
                                startActivity(new Intent(Login.this, MainActivity.class));
                                finish();

                            } else {
                                Toast.makeText(Login.this, "Verifica tu correo electrónico", Toast.LENGTH_LONG).show();
                            }
                        }



                    });
        });

        //Iniciar sesion con google
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Fallo el login con Google", e);
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        Toast.makeText(Login.this, "Bienvenido " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

                        // Guardar preferencia "Recuérdame"
                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        if (checkBoxRecuerdame != null && checkBoxRecuerdame.isChecked()) {
                            editor.putBoolean("recuerdame", true);
                            Log.d("LoginGoogle", "Recuérdame activado con Google.");
                        } else {
                            editor.remove("recuerdame");
                            Log.d("LoginGoogle", "Recuérdame no seleccionado con Google. Cerrando sesión.");
                            auth.signOut(); // solo si no se marcó Recuérdame
                        }

                        editor.apply();

                        startActivity(new Intent(Login.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(Login.this, "Autenticación fallida con Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}