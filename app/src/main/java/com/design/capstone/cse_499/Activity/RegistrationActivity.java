package com.design.capstone.cse_499.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.design.capstone.cse_499.Model.User;
import com.design.capstone.cse_499.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;

public class RegistrationActivity extends AppCompatActivity {

    EditText fullNameEt, userNameEt, passwordEt, emailEt, phoneEt;
    Button cancelBtn, signUpBtn;
    User user;
    String fullname, username, password, email, phonenumber;


    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference userRef = database.getReference("UserData");
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();

        initComponent();
        onClickAction();

       if (mAuth!=null)
       {
           mAuth.signOut();
       }


    }

    private void onClickAction() {

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            }
        });


        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Toast.makeText(RegistrationActivity.this, "Not Set Yet", Toast.LENGTH_SHORT).show();
                if (checkSignUpData()) {

                    initUserObject();

                }


            }
        });
    }

    private void initUserObject() {

        user = new User();

        user.setFullName(fullname);
        user.setEmail(email);
        user.setUserName(username);
        user.setPassword(password);
        user.setPhoneNumber(phonenumber);

      //  createUserAuth(user.getEmail(), user.getPassword());
        createUserAuth(user.getEmail(),user.getPassword());
        //gotoPhoneVerificationActivity();





    }

    private void gotoPhoneVerificationActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RegistrationActivity.this, " Verify Your Phone Number ", Toast.LENGTH_SHORT).show();
                final Intent mainIntent = new Intent(RegistrationActivity.this, PhoneVerificationActivity.class);
                mainIntent.putExtra("user" ,  user);
                startActivity(mainIntent);
                finish();
            }
        }, 1000);


    }



    public void createUserAuth(String email, String pass) {


        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (task.isSuccessful()) {

                            Toast.makeText(RegistrationActivity.this, " Registration Successful..", Toast.LENGTH_SHORT).show();
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser != null) {
                                mAuth.signOut();

                                gotoPhoneVerificationActivity();
                            }
                        }


                    }
                });

    }

    private void SendDatatoFireBase(User user, String uId) {
        userRef.child(uId).setValue(user);

    }

    public boolean checkSignUpData() {

        fullname = String.valueOf(fullNameEt.getText().toString().trim());
        username = String.valueOf(userNameEt.getText().toString().trim());
        password = passwordEt.getText().toString();
        email = String.valueOf(emailEt.getText().toString().trim());
        phonenumber = String.valueOf(phoneEt.getText().toString().trim());

        if (fullname.isEmpty()) {

            Toast.makeText(this, "Full Name is Empty ", Toast.LENGTH_SHORT).show();
        }
        if (username.isEmpty()) {
            Toast.makeText(this, "User Name is Empty ", Toast.LENGTH_SHORT).show();
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Password is Empty ", Toast.LENGTH_SHORT).show();

        }
        if (phonenumber.isEmpty()) {
            Toast.makeText(this, "Phone Number is Empty ", Toast.LENGTH_SHORT).show();

        }
        if (email.isEmpty()) {

            Toast.makeText(this, "Email is Empty ", Toast.LENGTH_SHORT).show();

        }
        if (!email.isEmpty() && !phonenumber.isEmpty() && !password.isEmpty() && !username.isEmpty() && !fullname.isEmpty()) {

            if (email.contains("@") && email.contains(".") && !email.trim().contains(" ")&& !email.contains("@.") && !email.contains(".@")) {

                if (phonenumber.length() == 11) {

                    if (phonenumber.startsWith("017") || phonenumber.startsWith("018") || phonenumber.startsWith("016")
                            || phonenumber.startsWith("019") || phonenumber.startsWith("015")) {

                        return true;
                    } else {

                        Toast.makeText(this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else {

                    Toast.makeText(this, "Enter Correct Phone Number ", Toast.LENGTH_SHORT).show();
                }
            } else {

                Toast.makeText(this, "Please Enter Correct Email", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return false;
    }

    public void initComponent() {

        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        signUpBtn = (Button) findViewById(R.id.signupBtn);

        fullNameEt = (EditText) findViewById(R.id.fullNameEt);
        userNameEt = (EditText) findViewById(R.id.userNameEt);
        passwordEt = (EditText) findViewById(R.id.regPasswordEt);
        emailEt = (EditText) findViewById(R.id.emailEt);
        phoneEt = (EditText) findViewById(R.id.phoneRt);

    }


}
