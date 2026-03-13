package com.example.chitchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class setting extends AppCompatActivity {
    ImageView setprofile;
    EditText setname, setstatus;
    Button donebut;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri setImageUri;
    String email, password;
    ProgressDialog progressDialog;
    String selectedBuiltIn = null; // To store which built-in image user chose

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Objects.requireNonNull(getSupportActionBar()).hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        setprofile = findViewById(R.id.settingprofile);
        setname = findViewById(R.id.settingname);
        setstatus = findViewById(R.id.settingstatus);
        donebut = findViewById(R.id.donebutt);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);

        DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
        StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());

        // Load existing user data
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                email = snapshot.child("mail").getValue(String.class);
                password = snapshot.child("password").getValue(String.class);
                String name = snapshot.child("userName").getValue(String.class);
                String profile = snapshot.child("profilepic").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);

                setname.setText(name);
                setstatus.setText(status);

                if (profile != null && !profile.isEmpty()) {
                    if (profile.startsWith("drawable:")) {
                        String resName = profile.replace("drawable:", "");
                        int resId = getResources().getIdentifier(resName, "drawable", getPackageName());
                        setprofile.setImageResource(resId);
                    } else {
                        Picasso.get().load(profile).into(setprofile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // On profile click → show options
        setprofile.setOnClickListener(view -> {
            String[] options = {"Choose from gallery", "Use Sample 1", "Use Sample 2"};
            AlertDialog.Builder builder = new AlertDialog.Builder(setting.this);
            builder.setTitle("Select Profile Picture")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            // Gallery
                            selectedBuiltIn = null;
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
                        } else if (which == 1) {
                            // Man DP
                            selectedBuiltIn = "drawable:mandp";
                            setImageUri = null;
                            setprofile.setImageResource(R.drawable.mandp);
                        } else if (which == 2) {
                            // Woman DP
                            selectedBuiltIn = "drawable:womandp";
                            setImageUri = null;
                            setprofile.setImageResource(R.drawable.womandp);
                        }
                    });
            builder.show();
        });

        // Save button
        donebut.setOnClickListener(view -> {
            progressDialog.show();
            String name = setname.getText().toString();
            String Status = setstatus.getText().toString();

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String currentImageUrl = snapshot.hasChild("profilepic")
                            ? snapshot.child("profilepic").getValue(String.class)
                            : "";

                    if (selectedBuiltIn != null) {
                        // Save as built-in
                        saveUserData(reference, new Users(auth.getUid(), name, email, password, selectedBuiltIn, Status));
                    } else if (setImageUri != null) {
                        // Upload new image
                        storageReference.putFile(setImageUri).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String finalImageUri = uri.toString();
                                    saveUserData(reference, new Users(auth.getUid(), name, email, password, finalImageUri, Status));
                                });
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(setting.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // No change
                        saveUserData(reference, new Users(auth.getUid(), name, email, password, currentImageUrl, Status));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                    Toast.makeText(setting.this, "Failed to read data", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void saveUserData(DatabaseReference reference, Users users) {
        reference.setValue(users).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(setting.this, "Data is saved", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(setting.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(setting.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
            setImageUri = data.getData();
            selectedBuiltIn = null;
            setprofile.setImageURI(setImageUri);
        }
    }
}
