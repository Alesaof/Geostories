package com.example.geostories.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.geostories.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    Button accept;
    EditText userNameEditText;
    String userName;
    EditText userEmailEditText;
    String userEmail;
    EditText userPasswordEditText;
    String userPassword;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Esto es para que aparezca la flecha de volver atras
        setUp();
    }

    private void setUp() {
        this.setTitle("Registrar");

        accept = findViewById(R.id.acceptSignUpButton);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText userEmailEditText = findViewById(R.id.emailSignUpEditText);
                userEmail = userEmailEditText.getText().toString();
                EditText userNameEditText = findViewById(R.id.signUpNameEditText);
                userName = userNameEditText.getText().toString();
                EditText userPasswordEditText =  findViewById(R.id.passwordSignUpEditText);
                userPassword = userPasswordEditText.getText().toString();

                if (!userName.isEmpty() && !userEmail.isEmpty() && !userPassword.isEmpty()){
                    signUpUser();
                } else {
                    Toast.makeText(SignUpActivity.this, "Campos incompletos", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void signUpUser() {
        Log.d("Geostories","username: " + userName);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Map<String, Object> map = new HashMap<>();
                    map.put("userName", userName);
                    map.put("userEmail", SignUpActivity.this.userEmail);
                    map.put("userPassword", SignUpActivity.this.userPassword);
                    map.put("profileViews", 0);
                    map.put("viewsDone", 0);
                    map.put("profilePicUrl", "https://firebasestorage.googleapis.com/v0/b/geostories-9ae6f.appspot.com/o/examples%2Fimages%2Fprofile.jpg.png?alt=media&token=08366982-74f1-497a-a325-bb09050b2931");
                    db.collection("users").document(SignUpActivity.this.userEmail).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {
                            if(task2.isSuccessful()){
                                Log.d("Geostories","mapita: " + map.toString());
                                userEmailEditText = findViewById(R.id.emailSignUpEditText);
                                userNameEditText = findViewById(R.id.signUpNameEditText);
                                EditText userPasswordEditText =  findViewById(R.id.passwordSignUpEditText);
                                userEmailEditText.setText("");
                                userNameEditText.setText("");
                                userPasswordEditText.setText("");
                            }else{
                                Toast.makeText(SignUpActivity.this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(SignUpActivity.this, "Ha habido un error con el registro", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //Para la flecha de volver atras
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}