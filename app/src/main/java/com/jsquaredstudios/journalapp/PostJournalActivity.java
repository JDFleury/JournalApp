package com.jsquaredstudios.journalapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.Objects;

import model.Journal;
import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity {
    private static final int GALLERY_CODE = 1;
    private ImageView postMainImage;
    private TextView postUsernameText;
    private TextView postDate;
    private ImageView uploadImageButton;
    private EditText postTitleEditText;
    private EditText postThoughtsEditText;
    private Button savePostButton;
    private ProgressBar postProgressBar;

    private String currentUserId;
    private String currentUsername;

    /* Image URI */
    private Uri imageUri;

    /* Firebase */
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    /* Connection to Firestore */
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Journal");

    /* Storage */
    private StorageReference storageReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        /* Action Bar Settings */
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        /* Storage */
        storageReference = FirebaseStorage.getInstance().getReference();

        /* Firebase Auth */
        firebaseAuth = FirebaseAuth.getInstance();

        /* Setting Up Views */
        postMainImage = findViewById(R.id.IV_postMainImage);
        postUsernameText = findViewById(R.id.TV_postUsernameText);
        postDate = findViewById(R.id.TV_postDateText);
        uploadImageButton = findViewById(R.id.IV_postImageButton);
        postTitleEditText = findViewById(R.id.ET_postTitle);
        postThoughtsEditText = findViewById(R.id.ET_postYourThoughts);
        savePostButton = findViewById(R.id.BTN_postSaveJournal);
        postProgressBar = findViewById(R.id.PB_postProgress);


        /* Buttons */
        savePostButton.setOnClickListener(v -> {
            saveJournal();

        });
        uploadImageButton.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT); //This is how to get a photo
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_CODE);

        });




        /* Retrieving Information from API */
        if (JournalApi.getInstance() != null) {
            currentUserId = JournalApi.getInstance().getUserId();
            currentUsername = JournalApi.getInstance().getUsername();

            postUsernameText.setText(currentUsername);
        }

        /* Firebase: State Listener */
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {

                }else {

                }
            }
        };












    }

    /* Saving the Journal Post */
    private void saveJournal() {
        String title = postTitleEditText.getText().toString().trim();
        String thoughts = postThoughtsEditText.getText().toString().trim();



        if (!TextUtils.isEmpty(title)
        && !TextUtils.isEmpty(thoughts)
        && imageUri != null) {

            postProgressBar.setVisibility(View.VISIBLE);

            StorageReference filePath = storageReference
                    .child("journal_images")
                    .child("my_image" + Timestamp.now().getSeconds());

            filePath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filePath.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String imageUrl = uri.toString(); // --> This is our URL

                                    //Todo: Create a Journal Object
                                    Journal journal = new Journal();

                                    journal.setTitle(title);
                                    journal.setThought(thoughts);
                                    journal.setImageUrl(imageUrl);
                                    journal.setTimeAdded(new Timestamp(new Date()));
                                    journal.setUserName(currentUsername);
                                    journal.setUserId(currentUserId);


                                    //Todo: Invoke our collection reference
                                    collectionReference.add(journal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    postProgressBar.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(PostJournalActivity.this,
                                                            JournalListActivity.class));
                                                    finish();

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(PostJournalActivity.this,
                                                            "Did not suceed",
                                                            Toast.LENGTH_LONG).show();

                                                }
                                            });


                                    //Todo: Add and save a Journal Instance

                                }
                            });




                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            postProgressBar.setVisibility(View.INVISIBLE);

                        }
                    });

        }else {
            Toast.makeText(PostJournalActivity.this,
                    "Please enter in all the information",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                postMainImage.setImageURI(imageUri);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    /* Makes it so it removes the listener which is data intensive */
    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}