package com.example.chitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    RecyclerView mainUserRecyclerView;
    UserAdpter adapter;
    FirebaseDatabase database;
    ArrayList<Users> usersArrayList;
    ArrayList<Users> messagedUsersList; // Permanent messaged users
    ImageView imglogout, cumbut, setbut;
    SearchView searchView;
    DatabaseReference reference, messagedRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        auth = FirebaseAuth.getInstance();

        // 🔹 Check if user is logged in FIRST
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, login.class));
            finish();
            return; // Stop execution to avoid null user
        }

        database = FirebaseDatabase.getInstance();

        // Now safe to get UID
        reference = database.getReference().child("user");
        messagedRef = database.getReference().child("messagedUsers")
                .child(auth.getCurrentUser().getUid());

        cumbut = findViewById(R.id.camBut);
        setbut = findViewById(R.id.settingBut);
        imglogout = findViewById(R.id.logoutImg);
        searchView = findViewById(R.id.searchView);

        usersArrayList = new ArrayList<>();
        messagedUsersList = new ArrayList<>();

        mainUserRecyclerView = findViewById(R.id.mainUserRecyclerView);
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdpter(MainActivity.this, usersArrayList);
        mainUserRecyclerView.setAdapter(adapter);

        // Load permanent messaged users from Firebase
        loadMessagedUsers();

        // SearchView listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) searchUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    searchUsers(newText);
                } else {
                    adapter.updateList(messagedUsersList);
                }
                return true;
            }
        });

        // Logout dialog
        imglogout.setOnClickListener(v -> {
            Dialog dialog = new Dialog(MainActivity.this, R.style.dialoge);
            dialog.setContentView(R.layout.dialog_layout);
            Button yes = dialog.findViewById(R.id.yesbnt);
            Button no = dialog.findViewById(R.id.nobnt);
            yes.setOnClickListener(view -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, login.class));
                finish();
            });
            no.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        });

        setbut.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, setting.class)));

        cumbut.setOnClickListener(v -> startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 10));
    }

    // 🔹 Load permanent messaged users from Firebase
    private void loadMessagedUsers() {
        messagedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagedUsersList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Users user = data.getValue(Users.class);
                    if (user != null) messagedUsersList.add(user);
                }
                adapter.updateList(messagedUsersList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // 🔹 Search users by typed name
    private void searchUsers(String searchText) {
        Query query = reference.orderByChild("userName")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Users> searchList = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user != null && !user.getUserId().equals(auth.getCurrentUser().getUid())) {
                        searchList.add(user);
                    }
                }

                // Merge messaged users if not already in search results
                for (Users user : messagedUsersList) {
                    if (!searchList.contains(user)) searchList.add(user);
                }

                adapter.updateList(searchList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // 🔹 Call this method when a user is messaged
    public void addMessagedUser(Users user) {
        if (user != null) {
            // Save in memory
            if (!messagedUsersList.contains(user)) messagedUsersList.add(user);

            // Save in Firebase
            messagedRef.child(user.getUserId()).setValue(user);
        }
    }
}
