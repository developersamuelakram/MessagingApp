package com.example.messagingapp;


import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //for sign in
    public static final int RC_SIGN_IN = 1;
    //forimage
    Uri imageUri = null;

    // for firebase
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;

    //for model and recyclerview
    RecyclerView recyclerView;
    List<MessageModel> models;
    MessageAdapter mAdapter;

    Button send;
    ImageView gallery;
    EditText editText;
    public static final int MESSAGE_TEXT_LIMIT = 1000;


    //for camera and gallery
    public static final int GALLERY_IMAGE_CODE = 100;
    public static final int CAMERA_IMAGE_CODE = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //permission for dexter

        permission();


        // firebase stuff
        auth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        //creating the path
        mDatabaseReference = mFirebaseDatabase.getReference("Messages");



        //views
        editText  = findViewById(R.id.edittext);
        gallery = findViewById(R.id.gallery);
        send = findViewById(R.id.send);





        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = auth.getCurrentUser();

                if (user!=null) {

                    Toast.makeText(MainActivity.this, "Signed In", Toast.LENGTH_SHORT).show();

                } else {

                    startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(Arrays.asList (new AuthUI.IdpConfig.EmailBuilder().build(), new
                                            AuthUI.IdpConfig.GoogleBuilder().build())).build(), RC_SIGN_IN);


                }

            }
        };



        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.toString().trim().length() > 0) {
                    send.setEnabled(true);
                } else {
                    send.setEnabled(false);
                }

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                // if we want the edittext to start with one space

                String text = editText.getText().toString();


                if (!text.startsWith(" ")) {
                    editText.getText().insert(0, " ");
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // IF WE WANT THE WORDS TO HAVE LIMIT

        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MESSAGE_TEXT_LIMIT)});

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                if (TextUtils.isEmpty(message)) {
                    editText.setError("Type Something");

                }


                uploadTextinSystem(message);
                //clear the text

                editText.setText("");


            }
        });



        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                imagePickDialog();
            }
        });


        recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        models = new ArrayList<>();
        mAdapter = new MessageAdapter(this, models);


        loadData();
    }

    private void loadData() {

        DatabaseReference ref = mFirebaseDatabase.getReference("Messages");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                models.clear();

                for (DataSnapshot ds: snapshot.getChildren()) {

                    MessageModel mdels = ds.getValue(MessageModel.class);
                    models.add(mdels);
                    mAdapter = new MessageAdapter(MainActivity.this, models);
                    recyclerView.setAdapter(mAdapter);




                }



            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





    }

    private void permission() {

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
            }
            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

            }

        }).check();



    }



    private void imagePickDialog() {


        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Choose Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (i==0) {

                    cameraDialog();

                }

                if (i==1) {

                    gallerydialog();
                }


            }
        });

        builder.create().show();
    }

    private void gallerydialog() {


        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_IMAGE_CODE);





    }

    private void cameraDialog() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        values.put(MediaStore.Images.Media.TITLE, "Temp Desc");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_IMAGE_CODE);



    }

    private void uploadTextinSystem(String message) {

        String timeStamp = String.valueOf(System.currentTimeMillis());

        FirebaseUser user = auth.getCurrentUser();

        if (user!=null) {

            HashMap<String, Object> hashMap = new HashMap<>();

            hashMap.put("username", user.getDisplayName());
            hashMap.put("useremail", user.getEmail());
            hashMap.put("time", timeStamp);
            hashMap.put("message", message);



            // reference is messsage the path is message and we are adding a child to it which will help us to sort out the database by timestamp
            mDatabaseReference.child(timeStamp).setValue(hashMap);






        }






    }


    @Override
    protected void onPause() {
        super.onPause();
        auth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        auth.addAuthStateListener(mAuthStateListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            if (resultCode == RESULT_OK) {



            } else if (resultCode == RESULT_CANCELED)  {

                finish();

            }


        }


        if (requestCode == GALLERY_IMAGE_CODE && resultCode == RESULT_OK) {

            Uri uri = data.getData();

            // CREATE A PATH FOR STORAGE DATABASE

            final String timeStamp = String.valueOf(System.currentTimeMillis());
            String path = "Photos/" + "photos_" + timeStamp;

            StorageReference ref = FirebaseStorage.getInstance().getReference(path);

            ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();

                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            // taking the photo
                            String photoid = uri.toString();

                            FirebaseUser user = auth.getCurrentUser();

                            HashMap<String, Object> hashMap = new HashMap<>();

                            hashMap.put("photoid", photoid);
                            hashMap.put("username", user.getDisplayName());
                            hashMap.put("time", timeStamp);
                            hashMap.put("useremail", user.getEmail());


                            mDatabaseReference.child(timeStamp + "_photos").setValue(hashMap);


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        }


        if (requestCode == CAMERA_IMAGE_CODE && resultCode == RESULT_OK) {

                Uri uri = imageUri;

                // CREATE A PATH FOR STORAGE DATABASE

                final String timeStamp = String.valueOf(System.currentTimeMillis());
                String path = "Photos/" + "photos_" + timeStamp;

                StorageReference ref = FirebaseStorage.getInstance().getReference(path);

                ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();

                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                // taking the photo
                                String photoid  = uri.toString();

                                FirebaseUser user = auth.getCurrentUser();

                                HashMap<String, Object> hashMap = new HashMap<>();

                                hashMap.put("photoid", photoid);
                                hashMap.put("username", user.getDisplayName());
                                hashMap.put("time", timeStamp);
                                hashMap.put("useremail", user.getEmail());




                                mDatabaseReference.child(timeStamp + "_photos").setValue(hashMap);


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });



                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });



            }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

       if (item.getItemId() == R.id.logout) {

           auth.signOut();
           Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show();
       }

        return super.onOptionsItemSelected(item);
    }




}