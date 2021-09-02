package com.jsquaredstudios.journalapp;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalApi;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText createUsernameText;
    private AutoCompleteTextView createEmailText;
    private EditText createPasswordText;
    private ProgressBar progressBar;
    private Button createAccountButton;


    /* Setting up Firebase Authentication */
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    /* Firestore Connection */
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        /* Action Bar Settings */
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        /* Firebase */
        firebaseAuth = FirebaseAuth.getInstance();



        //Setting Up Views
        createUsernameText = findViewById(R.id.ET_createUsername);
        createEmailText = findViewById(R.id.ATV_createEmail);
        createPasswordText = findViewById(R.id.ET_createPassword);
        progressBar = findViewById(R.id.PB_createProgress);
        createAccountButton = findViewById(R.id.BTN_createAccountButton);

        /* Firebase; Authentication State Listener */
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {
                    //User is already logged in

                }else {
                    //User is not logged in or not created

                }
            }
        };


        /* Firebase; Creating the Account */
        createAccountButton.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(createEmailText.getText().toString())
            && !TextUtils.isEmpty(createPasswordText.getText().toString())
            && !TextUtils.isEmpty(createUsernameText.getText().toString())) {

                String email = createEmailText.getText().toString().trim();
                String password = createPasswordText.getText().toString().trim();
                String username = createUsernameText.getText().toString().trim();

                createUserEmailAccount(email, password, username);

            }else {
                Toast.makeText(CreateAccountActivity.this,
                        "Please Enter all the Information",
                        Toast.LENGTH_LONG).show();
            }

        });



    }

    /* Firebase: Setting Up Create Account Method/Function */
    private void createUserEmailAccount(String email, String password, String username) {
        if (!TextUtils.isEmpty(email)
        && !TextUtils.isEmpty(password)
        && !TextUtils.isEmpty(username)) {

            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //This is after already creating the User
                                //We take user to AddJournalActivity if successful
                                //Setting up the User in database
                                currentUser = firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                String currentUserId = currentUser.getUid();

                                //Create a user Map so we can create a user in the User collection
                                Map<String, String> userObj = new HashMap<>();
                                userObj.put("userId", currentUserId);
                                userObj.put("username", username);

                                //save to Firestore Database
                                collectionReference.add(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (Objects.requireNonNull(task.getResult()).exists()) {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    String name = task.getResult()
                                                                            .getString("username");

                                                                    /* Using JournalApi singleton */
                                                                    JournalApi journalApi = JournalApi.getInstance(); //Global API
                                                                    journalApi.setUserId(currentUserId);
                                                                    journalApi.setUsername(name);

                                                                    Intent intent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
                                                                    //Putting the information to the following Activity
                                                                    intent.putExtra("username", name);
                                                                    intent.putExtra("userId", currentUserId);
                                                                    startActivity(intent);
                                                                }else {
                                                                    progressBar.setVisibility(View.INVISIBLE);

                                                                }
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });


                            }else{
                                //Something went wrong
                                //Email was already registered
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(CreateAccountActivity.this,
                                        "Email already registered",
                                        Toast.LENGTH_LONG).show();
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });


        }else {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        /* Firebase; Check if user is signed in (non null) */
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);

    }
}