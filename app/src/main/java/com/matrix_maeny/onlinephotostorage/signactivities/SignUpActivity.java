package com.matrix_maeny.onlinephotostorage.signactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.matrix_maeny.onlinephotostorage.MainActivity;
import com.matrix_maeny.onlinephotostorage.R;
import com.matrix_maeny.onlinephotostorage.databinding.ActivitySignUpBinding;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    private String username, email, password;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        FirebaseApp.initializeApp(SignUpActivity.this);
        FirebaseAppCheck appCheck = FirebaseAppCheck.getInstance();
        appCheck.installAppCheckProviderFactory(SafetyNetAppCheckProviderFactory.getInstance());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        initialize();
    }

    private void initialize() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
        }
        firebaseDatabase = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(SignUpActivity.this);
        binding.signUpBtn.setOnClickListener(signUpBtnListener);
        binding.signInBtn.setOnClickListener(signInBtnListener);
    }


    View.OnClickListener signUpBtnListener = v -> {
        if (checkUserName() && checkEmail() && checkPassword()) {
            // save data
            createUser();
        }
    };
    View.OnClickListener signInBtnListener = v -> {
        progressDialog.setMessage("Logging in");
        if (checkUserName() && checkEmail() && checkPassword()) {
            progressDialog.show();
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){
                        startActivity(new Intent(SignUpActivity.this,MainActivity.class));
                        finish();
                    }else{
                        Toast.makeText(SignUpActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }
            });
        }
    };

    private void createUser() {
        progressDialog.setMessage("Please wait while creating account...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    UserModel model = new UserModel(username, email, password);
                    String uid = Objects.requireNonNull(task.getResult().getUser()).getUid();
                    firebaseDatabase.getReference().child("Users").child(uid).setValue(model);

                    Toast.makeText(SignUpActivity.this, "user created successfully", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });
    }

    private boolean checkUserName() {
        try {
            username = Objects.requireNonNull(binding.usernameEt.getText()).toString().trim();
            if (!username.equals("")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean checkEmail() {
        try {
            email = Objects.requireNonNull(binding.emailEt.getText()).toString().trim();
            if (!email.equals("")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean checkPassword() {
        try {
            password = Objects.requireNonNull(binding.passwordEt.getText()).toString().trim();
            if (!password.equals("")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
        return false;
    }
}