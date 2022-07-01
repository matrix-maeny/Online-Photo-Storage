package com.matrix_maeny.onlinephotostorage;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.matrix_maeny.onlinephotostorage.databinding.ActivityMainBinding;
import com.matrix_maeny.onlinephotostorage.signactivities.SignUpActivity;
import com.matrix_maeny.onlinephotostorage.signactivities.UserModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;


    private Uri mainUri;
    private ArrayList<String> urlList;
    private PhotoAdapter adapter;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private FirebaseStorage storage;
    private FirebaseDatabase firebaseDatabase;

    private String uid;
    private long count = 0L;
    private long urlListLength = 0L;
    HashMap<String, String> hashMap;// = new HashMap<>();

    final Handler handler = new Handler();
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(MainActivity.this);
        FirebaseAppCheck appCheck = FirebaseAppCheck.getInstance();
        appCheck.installAppCheckProviderFactory(SafetyNetAppCheckProviderFactory.getInstance());

        initialize();

        readData();



    }


    private void readData() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Images");//.child("image:0");
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("username");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                username = snapshot.getValue(String.class);
                if (username != null) {
                    handler.post(()-> Objects.requireNonNull(getSupportActionBar()).setTitle(username));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        progressDialog.setMessage("Fetching data...");
        progressDialog.show();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                urlList.clear();
                urlListLength = snapshot.getChildrenCount();
                for (long i = 0; i < urlListLength; i++) {
                    try {
                        String x = snapshot.child("image:" + i).getValue(String.class);
                        urlList.add(x);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
//                if (initial) {
////                    count = urlListLength - 1;
////
////                    count = count == -1 ? 0 : count;
////
//                    initial = false;
//
//                }
                count = urlListLength;

                if (count == 0) {
                    binding.emptyTv.setVisibility(View.VISIBLE);

                } else {
                    binding.emptyTv.setVisibility(View.GONE);
                }


                refreshAdapter();
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initialize() {

        firebaseAuth = FirebaseAuth.getInstance();
        uid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        storage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        hashMap = new HashMap<>();
        progressDialog = new ProgressDialog(MainActivity.this);
        urlList = new ArrayList<>(); // contains url
        adapter = new PhotoAdapter(MainActivity.this, urlList);

        binding.recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
        binding.recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_app:
                // go to about activity
                startActivity(new Intent(MainActivity.this,AboutActivity.class));
                break;
            case R.id.upload_image:
                startUploading();
                break;
            case R.id.log_out:
                logout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {

        progressDialog.setMessage("Logging out...");
        progressDialog.show();
        firebaseAuth.signOut();
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            finish();

        }, 1500);

    }

    private void startUploading() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
//        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);

        resultLauncher.launch(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshAdapter() {
        adapter.notifyDataSetChanged();
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            Intent data = result.getData();

            if (data != null) {
//                if (data.getClipData() != null) {
//                    int count = data.getClipData().getItemCount();
//
//                    for (int i = 0; i < count; i++) {
//                        Uri uri = data.getClipData().getItemAt(i).getUri();
//                        uriList.add(uri);
//                        Log.i("allUris", uri.toString());
//                    }
//                } else if (data.getData() != null) {
//                    Uri uri = data.getData();
//                    uriList.add(uri);
//                    Log.i("allUris", uri.toString());
//                }

                Uri uri = data.getData();
                mainUri = uri;
                Log.i("allUris", uri.toString());
                addToFireBase();
            } else mainUri = null;


        }
    });

    private void addToFireBase() {

        progressDialog.setMessage("Please wait while uploading");
        handler.post(() -> progressDialog.show());

        long finalI = count;
        final StorageReference reference = storage.getReference().child("Users").child(uid).child("Image:" + finalI);

        reference.putFile(mainUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        firebaseDatabase.getReference().child("Users").child(uid)
                                .child("Images").child("image:" + finalI).setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
//                                count = count + 1;
                                handler.post(() -> Toast.makeText(MainActivity.this, "File uploaded", Toast.LENGTH_SHORT).show());
                                handler.post(() -> progressDialog.dismiss());

                            }
                        });
                    }
                });
            }
        });

        mainUri = null;


    }


//    private void addToFireBase() {
//
//        progressDialog.setMessage("Please wait while uploading");
//        handler.post(()-> progressDialog.show());
//        for (int i = 0; i < uriList.size(); i++) {
//            test = true;
//            long finalI = count;
//            final StorageReference reference = storage.getReference().child("Users").child(uid).child("Image:" + finalI);
//
//            try {
//                reference.putFile(uriList.get(i)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                            @Override
//                            public void onSuccess(Uri uri) {
//                                count = count + 1;
//                                firebaseDatabase.getReference().child("Users").child(uid)
//                                        .child("Images").child("image:" + finalI).setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void unused) {
//                                        handler.post(() -> Toast.makeText(MainActivity.this, "File uploaded", Toast.LENGTH_SHORT).show());
//                                        test = false;
//
//                                    }
//                                });
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                handler.post(() -> Toast.makeText(MainActivity.this, "Error uploading a file", Toast.LENGTH_SHORT).show());
//                                test = false;
//                            }
//                        });
//                    }
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//                test = false;
//            }
//
//            while (test) {
//            }
//            ;
//
//
//        }
//        handler.post(()->progressDialog.dismiss());
//        uriList.clear();
//    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseApp.initializeApp(MainActivity.this);
        FirebaseAppCheck appCheck = FirebaseAppCheck.getInstance();
        appCheck.installAppCheckProviderFactory(SafetyNetAppCheckProviderFactory.getInstance());
    }
}