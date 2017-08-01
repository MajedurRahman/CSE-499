package com.design.capstone.cse_499.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.design.capstone.cse_499.R;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Majedur Rahman on 8/1/2017.
 */

public class ResetPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    Button resetButton;
    EditText email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetpassword);
        mAuth = FirebaseAuth.getInstance();

        initComponents();
        onClickAction();

    }

    private void initComponents() {

        email = (EditText) findViewById(R.id.resetEmail);
        resetButton = (Button) findViewById(R.id.resetpassBtn);
    }

    public void onClickAction()
    {
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailadd = email.getText().toString().trim();

                mAuth.sendPasswordResetEmail(emailadd);
                Toast.makeText(ResetPasswordActivity.this, "Check Your mail to reset Password ", Toast.LENGTH_SHORT).show();


            }
        });

    }
}
