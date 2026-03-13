package com.example.chitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class login extends AppCompatActivity {
    TextView logsignup;
    Button button;
    EditText email, password;
    FirebaseAuth auth;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    ProgressBar progressBar; // <-- Using ProgressBar instead of ProgressDialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Objects.requireNonNull(getSupportActionBar()).hide();
        auth = FirebaseAuth.getInstance();

        button = findViewById(R.id.logbutton);
        email = findViewById(R.id.editTexLogEmail);
        password = findViewById(R.id.editTextLogPassword);
        logsignup = findViewById(R.id.logsignup);
        progressBar = findViewById(R.id.progressBar); // <-- Initialize ProgressBar

        logsignup.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, registration.class);
            startActivity(intent);
            finish();
        });

        button.setOnClickListener(v -> {
            String Email = email.getText().toString();
            String pass = password.getText().toString();

            if (TextUtils.isEmpty(Email)) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(login.this, "Enter The Email", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(pass)) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(login.this, "Enter The Password", Toast.LENGTH_SHORT).show();
            } else if (!Email.matches(emailPattern)) {
                progressBar.setVisibility(View.GONE);
                email.setError("Give Proper Email Address");
            } else if (pass.length() < 6) {
                progressBar.setVisibility(View.GONE);
                password.setError("More Than Six Characters");
                Toast.makeText(login.this, "Password Needs To Be Longer Than Six Characters", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE); // Show while logging in
                auth.signInWithEmailAndPassword(Email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE); // Hide after completion
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.VISIBLE);
                            try {
                                Intent intent = new Intent(login.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } catch (Exception e) {
                                Toast.makeText(login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}