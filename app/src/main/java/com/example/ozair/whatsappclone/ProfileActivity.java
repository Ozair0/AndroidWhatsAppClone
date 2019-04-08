package com.example.ozair.whatsappclone;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String mReceiverUserID, senderUserID,currentState;
    private CircleImageView mUserProfileImg;
    private TextView mUserProfileName,mUserProfileStatus;
    private Button mSendMessageRequestButton,mDeclineMessageRequestButton;
    private DatabaseReference mUserRef, mChatReqRef, mContactRef, NotifactionRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mChatReqRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        mContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotifactionRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        senderUserID = mAuth.getCurrentUser().getUid();
        mReceiverUserID = getIntent().getExtras().get("visitUserId").toString();

        mUserProfileImg = (CircleImageView) findViewById(R.id.visit_profile_image);
        mUserProfileName = (TextView) findViewById(R.id.visit_user_name);
        mUserProfileStatus = (TextView) findViewById(R.id.visit_user_status);
        mSendMessageRequestButton = (Button) findViewById(R.id.send_message_request_btn);
        mDeclineMessageRequestButton = (Button) findViewById(R.id.decline_message_request_btn);
        currentState = "new";

        RetriveUserInfo();
    }

    private void RetriveUserInfo() {
        mUserRef.child(mReceiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(mUserProfileImg);
                    mUserProfileName.setText(userName);
                    mUserProfileStatus.setText(userStatus);

                    ManageChatRequests();
                }else{
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    mUserProfileName.setText(userName);
                    mUserProfileStatus.setText(userStatus);

                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequests() {
        mChatReqRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(mReceiverUserID)){
                            String reqType = dataSnapshot.child(mReceiverUserID).child("request_type").getValue().toString();
                            if (reqType.equals("sent")){
                                currentState = "request_sent";
                                mSendMessageRequestButton.setText("Cancel Chat Request");
                            }else if(reqType.equals("received")) {
                                currentState = "request_received";
                                mSendMessageRequestButton.setText("Accept Chat Request");
                                mDeclineMessageRequestButton.setVisibility(View.VISIBLE);
                                mDeclineMessageRequestButton.setEnabled(true);
                                mDeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancellChatRequest();
                                    }
                                });
                            }
                        }else{
                            mContactRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(mReceiverUserID)){
                                                currentState = "friends";
                                                mSendMessageRequestButton.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!senderUserID.equals(mReceiverUserID)){
            mSendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSendMessageRequestButton.setEnabled(false);

                    if(currentState.equals("new")){
                        SendChatRequest();
                    }
                    if(currentState.equals("request_sent"))
                    {
                        CancellChatRequest();
                    }
                    if(currentState.equals("request_received"))
                    {
                        AccepChatRequest();
                    }
                    if(currentState.equals("friends"))
                    {
                        RemoveSpacificContact();
                    }
                }
            });
        }else{
            mSendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpacificContact() {
        mContactRef.child(senderUserID).child(mReceiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mContactRef.child(mReceiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                if(task.isSuccessful()){
                                                    mSendMessageRequestButton.setEnabled(true);
                                                    currentState = "new";
                                                    mSendMessageRequestButton.setText("Send Message");

                                                    mDeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                    mDeclineMessageRequestButton.setEnabled(false);
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AccepChatRequest() {
        mContactRef.child(senderUserID).child(mReceiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mContactRef.child(mReceiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                mChatReqRef.child(senderUserID).child(mReceiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    mChatReqRef.child(mReceiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        mSendMessageRequestButton.setEnabled(true);
                                                                                        currentState = "friends";
                                                                                        mSendMessageRequestButton.setText("Remove This Contact");

                                                                                        mDeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                        mDeclineMessageRequestButton.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancellChatRequest() {
        mChatReqRef.child(senderUserID).child(mReceiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mChatReqRef.child(mReceiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                if(task.isSuccessful()){
                                                    mSendMessageRequestButton.setEnabled(true);
                                                    currentState = "new";
                                                    mSendMessageRequestButton.setText("Send Message");

                                                    mDeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                    mDeclineMessageRequestButton.setEnabled(false);
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest() {
        mChatReqRef.child(senderUserID).child(mReceiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mChatReqRef.child(mReceiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                HashMap<String, String> chatNotification = new HashMap<>();
                                                chatNotification.put("from", senderUserID);
                                                chatNotification.put("type", "request");

                                                NotifactionRef.child(mReceiverUserID).push()
                                                        .setValue(chatNotification)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    mSendMessageRequestButton.setEnabled(true);
                                                                    currentState = "request_sent";
                                                                    mSendMessageRequestButton.setText("Cancel Chat Request");
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
