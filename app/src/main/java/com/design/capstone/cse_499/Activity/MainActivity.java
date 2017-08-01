package com.design.capstone.cse_499.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.design.capstone.cse_499.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    TextView registerTv , forgotPass;
    EditText emailTv, passwordTv;
    Button loginBtn;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        initComponents();
        onclickAction();
        userAuth();


    }

    private void onclickAction() {

        registerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
            }
        });


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email =emailTv.getText().toString().trim();
                String pass = passwordTv.getText().toString();
                if (!email.isEmpty()&& !pass.isEmpty()){
                    checkLogin(email,pass);
                }

            }
        });


        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //forgot pass

                startActivity(new Intent(MainActivity.this,ResetPasswordActivity.class));
            }
        });

    }

    private void checkLogin(String email , String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                         // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (task.isSuccessful()) {

                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(MainActivity.this, MapActivity.class));
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Please enter Correct User Name and Password", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }

    public void userAuth(){

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in


                } else {
                    // User is signed out


                }
                // ...
            }
        };

    }

    private void initComponents() {


        registerTv = (TextView) findViewById(R.id.registerTv);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        emailTv = (EditText) findViewById(R.id.emailEt);
        passwordTv = (EditText) findViewById(R.id.passwordEt);
        forgotPass = (TextView) findViewById(R.id.forgotpassword);


    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
