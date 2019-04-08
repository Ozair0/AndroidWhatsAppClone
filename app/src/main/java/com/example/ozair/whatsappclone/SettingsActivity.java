package com.example.ozair.whatsappclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button mUpdateAccountSettings;
    private EditText mUserName, mUserStatus;
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage";
    private CircleImageView mUserProfileImage;
    private String mCurrentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private static final int mGalleryPick = 1;
    private StorageReference mUserProfileImagesRef;
    private ProgressDialog mLoadingBar;
    private Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializFields();

        mUserName.setVisibility(View.INVISIBLE);

        mUpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetriveUserInfo();

        mUserProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, mGalleryPick);
            }
        });

    }

    private void InitializFields() {
        mUpdateAccountSettings = findViewById(R.id.update_settings_btn);
        mUserName = findViewById(R.id.set_user_name);
        mUserStatus = findViewById(R.id.set_profile_status);
        mUserProfileImage = findViewById(R.id.profile_image);
        mLoadingBar = new ProgressDialog(this);
        mToolBar = (Toolbar) findViewById(R.id.settings_tool_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,@Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == mGalleryPick && resultCode == RESULT_OK && data!=null){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                mLoadingBar.setTitle("Uploading Image");
                mLoadingBar.setMessage("Please wait, your profile picture is updating!");
                mLoadingBar.setCanceledOnTouchOutside(false);
                mLoadingBar.show();

                Uri resultUri = result.getUri();
                final StorageReference filePath = mUserProfileImagesRef.child(mCurrentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Profile Image Uploaded!", Toast.LENGTH_SHORT).show();
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();
                                    mRootRef.child("Users").child(mCurrentUserId).child("image")
                                            .setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(Task<Void> task) {
                                            if(task.isSuccessful()){
                                                mLoadingBar.dismiss();
                                                Toast.makeText(SettingsActivity.this, "Imaged Saved in Database", Toast.LENGTH_SHORT).show();
                                            }else{
                                                String message = task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                                                mLoadingBar.dismiss();
                                            }
                                        }
                                    });
                                }
                            });

                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                            mLoadingBar.dismiss();
                        }
                    }
                });
            }
        }

    }

    private void UpdateSettings() {
        String setUserName = mUserName.getText().toString();
        String setUserStatus = mUserStatus.getText().toString();
        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please write your Username", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(setUserStatus)){
            Toast.makeText(this, "Please write your Status", Toast.LENGTH_SHORT).show();
        }else{
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put("uid", mCurrentUserId);
            profileMap.put("name", setUserName);
            profileMap.put("status", setUserStatus);
            mRootRef.child("Users").child(mCurrentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                SendUserToMainActitvity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void RetriveUserInfo(){
        mRootRef.child("Users").child(mCurrentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))){
                            String retriveUserName = dataSnapshot.child("name").getValue().toString();
                            String retriveUserStatus = dataSnapshot.child("status").getValue().toString();
                            String retriveProfileImage = dataSnapshot.child("image").getValue().toString();

                            mUserName.setText(retriveUserName);
                            mUserStatus.setText(retriveUserStatus);
                            Picasso.get().load(retriveProfileImage).into(mUserProfileImage);

                        }else if(dataSnapshot.exists() && (dataSnapshot.hasChild("name"))){
                            String retriveUserName = dataSnapshot.child("name").getValue().toString();
                            String retriveUserStatus = dataSnapshot.child("status").getValue().toString();

                            mUserName.setText(retriveUserName);
                            mUserStatus.setText(retriveUserStatus);
                        }else{
                            mUserName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please Set & Update information", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendUserToMainActitvity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}
