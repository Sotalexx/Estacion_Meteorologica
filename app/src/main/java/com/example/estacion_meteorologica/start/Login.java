package com.example.estacion_meteorologica.start;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private static final String TAG = "Login";

    private MaterialButton btnGoogle, btnIrLogin;
    private Button btnLogin;
    private ImageButton btnBack;
    private TextInputLayout email, password;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnBack = findViewById(R.id.btnBack);
        btnLogin = findViewById(R.id.btn_login);
        btnIrLogin = findViewById(R.id.btnIrLogin);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btnGoogle = findViewById(R.id.btnGoogle);

        auth = FirebaseAuth.getInstance();

        // Configurar Login con Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id)) //
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnBack.setOnClickListener(v -> finish());

        btnIrLogin.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, SignUp.class);
            startActivity(intent);
            finish();
        });

        btnLogin.setOnClickListener(v -> {
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
                                startActivity(new Intent(Login.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(Login.this, "Verifica tu correo electrónico", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(Login.this, "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

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
                        startActivity(new Intent(Login.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(Login.this, "Autenticación fallida con Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}