package com.example.ozair.whatsappclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private Button mCreateAccountBtn;
    private EditText mUserEmail, mUserPassword;
    private TextView mAlreadyHaveAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog mLoadingBar;
    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        InitialzeField();

        mAlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMainLogin();
            }
        });

        mCreateAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email = mUserEmail.getText().toString();
        String password = mUserPassword.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please Enter Email...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please Enter Password...", Toast.LENGTH_SHORT).show();
        } else {
            mLoadingBar.setTitle("Creating New Account");
            mLoadingBar.setMessage("Please Wait, While Your Account is Created!");
            mLoadingBar.setCanceledOnTouchOutside(true);
            mLoadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                String currrentUserId = mAuth.getCurrentUser().getUid();
                                mRootRef.child("Users").child(currrentUserId).setValue("");
                                SendUserToMainLogin();
                                Toast.makeText(RegisterActivity.this, "Account Created!", Toast.LENGTH_SHORT).show();
                                mLoadingBar.dismiss();



                                mRootRef.child("Users").child(currrentUserId).child("device_token")
                                        .setValue(deviceToken);
                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error :"+message+"\nTry Again", Toast.LENGTH_SHORT).show();
                                mLoadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitialzeField() {
        mCreateAccountBtn = findViewById(R.id.register_btn);
        mUserEmail = findViewById(R.id.register_email);
        mUserPassword = findViewById(R.id.register_password);
        mAlreadyHaveAccountLink = findViewById(R.id.Already_have_account_link);
        mLoadingBar = new ProgressDialog(this);
    }

    private void SendUserToMainLogin() {
        Intent loginIntent = new Intent(RegisterActivity.this, MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
