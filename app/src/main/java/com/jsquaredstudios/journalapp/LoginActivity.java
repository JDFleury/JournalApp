package com.jsquaredstudios.journalapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import util.JournalApi;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton, createAccountButton;
    private AutoCompleteTextView loginEmailText;
    private EditText loginPasswordText;
    private ProgressBar progressBar;

    /* Firebase */
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Action Bar Settings */
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();


        //Setting Up Views
        loginButton = findViewById(R.id.BTN_loginButton);
        createAccountButton = findViewById(R.id.BTN_loginCreateAccountButton);
        loginEmailText = findViewById(R.id.ATV_loginEmail);
        loginPasswordText = findViewById(R.id.ET_loginPassword);
        progressBar = findViewById(R.id.PB_loginProgress);


        createAccountButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
        });


        loginButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            loginEmailPassword(loginEmailText.getText().toString().trim(),
                    loginPasswordText.getText().toString().trim());

        });





    }

    private void loginEmailPassword(String email, String password) {

        if (!TextUtils.isEmpty(email)
        && !TextUtils.isEmpty(password)) {
            /* Firebase: Logging in */
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            assert user != null;
                            String currentUserId = user.getUid();

                            /* Loop through all current users to find the correct one */
                            collectionReference
                                    .whereEqualTo("userId", currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            /* Have the user with the currentUserId */
                                            progressBar.setVisibility(View.INVISIBLE);

                                            if (error != null) {
                                            }

                                            assert value != null;
                                            if (!value.isEmpty()) {

                                                for (QueryDocumentSnapshot snapshot : value) {
                                                    JournalApi journalApi = JournalApi.getInstance();
                                                    journalApi.setUsername(snapshot.getString("username"));
                                                    journalApi.setUserId(snapshot.getString("userId"));

                                                    //Go to JournalListActivity
                                                    startActivity(new Intent(LoginActivity.this, JournalListActivity.class));
                                                }

                                            }
                                        }
                                    });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    });

        }else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(LoginActivity.this,
                    "Please enter Email and Password",
                    Toast.LENGTH_LONG).show();
        }
    }


}