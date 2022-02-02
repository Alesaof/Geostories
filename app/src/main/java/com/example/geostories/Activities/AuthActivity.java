package com.example.geostories.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.geostories.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {
    private Button signUpButton;
    private Button loginButton;
    private String userEmailAuth;
    private String userPasswordAuth;
    private Bundle bundle;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        signUpButton = findViewById(R.id.signUpButton);
        //setup
        setUp();
    }

    private void setUp() {
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Cuando se presione el botón, realiza una acción aquí
                openSignUp();
            }
        });
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText userEmailEditText =  findViewById(R.id.emailAuthEditText);
                userEmailAuth = userEmailEditText.getText().toString();
                EditText userPasswordEditText =  findViewById(R.id.passwordAuthEditText);
                userPasswordAuth = userPasswordEditText.getText().toString();

                if (!userEmailAuth.isEmpty() && !userPasswordAuth.isEmpty()){
                    LoginUser();
                } else {
                    Toast.makeText(AuthActivity.this, "Campos incompletos", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void LoginUser() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(userEmailAuth, userPasswordAuth).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    openHomeActivity(userEmailAuth);
                }else{
                    Toast.makeText(AuthActivity.this, "No se ha podido Iniciar Sesión", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Other Activities Open
    private void openHomeActivity(String actualUser) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("actualUser", actualUser);
        startActivity(intent);
    }

    private void openSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }
}