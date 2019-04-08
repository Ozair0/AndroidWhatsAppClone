package com.example.ozair.whatsappclone;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button mSendVerificationCodeButton, mVerifyButton;
    private EditText mInputPhoneNumber, mInputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        mSendVerificationCodeButton = (Button) findViewById(R.id.send_verification_code_btn);
        mVerifyButton = (Button) findViewById(R.id.verifiy_btn);
        mInputPhoneNumber = (EditText) findViewById(R.id.phone_num_input);
        mInputVerificationCode = (EditText) findViewById(R.id.verification_code_input);
        mLoadingBar = new ProgressDialog(this);

        mSendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = mInputPhoneNumber.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "Please Write Your Phone Number!", Toast.LENGTH_SHORT).show();
                }else {
                    mLoadingBar.setTitle("Phone Verification");
                    mLoadingBar.setMessage("Please Wait, While we are authenticating your phone");
                    mLoadingBar.setCanceledOnTouchOutside(false);
                    mLoadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,
                            60,
                            TimeUnit.SECONDS,
                            PhoneLoginActivity.this,
                            mCallBack
                    );
                }
            }
        });

        mVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSendVerificationCodeButton.setVisibility(View.INVISIBLE);
                mInputPhoneNumber.setVisibility(View.INVISIBLE);

                String verirficationCode = mInputVerificationCode.getText().toString();
                if(TextUtils.isEmpty(verirficationCode)){
                    Toast.makeText(PhoneLoginActivity.this, "Write First The Code", Toast.LENGTH_SHORT).show();
                }else{
                    mLoadingBar.setTitle("Code Verification");
                    mLoadingBar.setMessage("Please Wait, While we are Verify your Account");
                    mLoadingBar.setCanceledOnTouchOutside(false);
                    mLoadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verirficationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                mLoadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number, Please Enter correct phone Number with your country code!", Toast.LENGTH_SHORT).show();
                mSendVerificationCodeButton.setVisibility(View.VISIBLE);
                mInputPhoneNumber.setVisibility(View.VISIBLE);
                mVerifyButton.setVisibility(View.INVISIBLE);
                mInputVerificationCode.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                mVerificationId = s;
                mResendToken = forceResendingToken;
                mLoadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code Has been Sent!", Toast.LENGTH_SHORT).show();
                mSendVerificationCodeButton.setVisibility(View.INVISIBLE);
                mInputPhoneNumber.setVisibility(View.INVISIBLE);
                mVerifyButton.setVisibility(View.VISIBLE);
                mInputVerificationCode.setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mLoadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulations You're Logged In!", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
