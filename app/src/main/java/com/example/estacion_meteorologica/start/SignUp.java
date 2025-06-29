package com.example.estacion_meteorologica.start;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.estacion_meteorologica.MainActivity;
import com.example.estacion_meteorologica.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUp extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private Button btnBack, btnSignup;
    private TextView btnIrLogin;
    MaterialCheckBox checkBoxRecuerdame;
    private TextInputLayout name, email, password;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private LinearLayout btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_sign_up);

        btnBack = findViewById(R.id.btnBack);
        btnSignup = findViewById(R.id.btn_signup);
        btnIrLogin = findViewById(R.id.btnIrLogin);
        btnGoogle = findViewById(R.id.btnGoogle);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        checkBoxRecuerdame = findViewById(R.id.checkBoxRecuerdame);

        auth = FirebaseAuth.getInstance();

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id)) // este string viene de google-services.json
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        //Boton de regresar
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnIrLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUp.this, Login.class);
            startActivity(intent);
        });

        btnSignup.setOnClickListener(v -> {
            String userName = name.getEditText().getText().toString().trim();
            String userEmail = email.getEditText().getText().toString().trim();
            String userPassword = password.getEditText().getText().toString().trim();

            if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(userPassword)) {
                Toast.makeText(SignUp.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userPassword.length() < 6) {
                Toast.makeText(SignUp.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();

                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(userName)
                                    .build();

                            user.updateProfile(profileUpdate).addOnCompleteListener(updateTask -> {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(verifyTask -> {
                                            if (verifyTask.isSuccessful()) {
                                                Toast.makeText(SignUp.this, "Registro exitoso. Verifica tu correo", Toast.LENGTH_LONG).show();
                                                Log.d("SignUp", "Usuario creado: " + user.getEmail());

                                                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                                SharedPreferences.Editor editor = prefs.edit();

                                                if (checkBoxRecuerdame != null && checkBoxRecuerdame.isChecked()) {
                                                    Log.d("SignUp", "Checkbox Recuérdame seleccionado. Guardando preferencia.");
                                                    editor.putBoolean("recuerdame", true);
                                                } else {
                                                    Log.d("SignUp", "Checkbox NO seleccionado. Eliminando preferencia.");
                                                    editor.remove("recuerdame");
                                                }

                                                editor.apply();

                                                auth.signOut();

                                                SharedPreferences updatedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                                boolean confirmacion = updatedPrefs.getBoolean("recuerdame", false);
                                                Log.d("SignUp", "¿Recuerdame quedó guardado? " + confirmacion);

                                                startActivity(new Intent(SignUp.this, Login.class));
                                                finish();
                                            }

                                        });
                            });
                        } else {
                            Toast.makeText(SignUp.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        Log.d("SignUpGoogle", "Usuario creado: " + user.getEmail());

                        // Guardar Recuerdame
                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        if (checkBoxRecuerdame != null && checkBoxRecuerdame.isChecked()) {
                            Log.d("SignUpGoogle", "Checkbox Recuérdame seleccionado. Guardando preferencia.");
                            editor.putBoolean("recuerdame", true);
                        } else {
                            Log.d("SignUpGoogle", "Checkbox NO seleccionado. Eliminando preferencia.");
                            editor.remove("recuerdame");
                            auth.signOut(); // cerrar sesión si no marcó la casilla
                        }

                        editor.apply();

                        // Confirmar que se guardó correctamente
                        SharedPreferences updatedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        boolean confirmacion = updatedPrefs.getBoolean("recuerdame", false);
                        Log.d("SignUpGoogle", "¿Recuerdame quedó guardado? " + confirmacion);

                        // Redirigir
                        if (confirmacion) {
                            startActivity(new Intent(SignUp.this, MainActivity.class));
                        } else {
                            startActivity(new Intent(SignUp.this, Login.class));
                        }

                        Toast.makeText(this, "Sesión iniciada con Google", Toast.LENGTH_SHORT).show();
                        finish();

                    } else {
                        Toast.makeText(this, "Fallo en autenticación con Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                int statusCode = e.getStatusCode();
                String message = GoogleSignInStatusCodes.getStatusCodeString(statusCode);

                Toast.makeText(this, "Error en Google Sign-In: " + statusCode + " (" + message + ")", Toast.LENGTH_LONG).show();
                Log.e("GoogleSignIn", "Error " + statusCode + ": " + message, e);
            }
        }
    }

}
