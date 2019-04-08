package com.example.ozair.whatsappclone;


import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private View mGroupFragmentView;
    private ListView mListView;
    private ArrayAdapter<String> mArratAdapter;
    private ArrayList<String> mListOfGroups = new ArrayList<>();
    private DatabaseReference mGroupRef;
    private FirebaseAuth mAuth;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mGroupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        mGroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        InitializeFields();

        RetriveAndDisplayGroup();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String currentGroupName = adapterView.getItemAtPosition(position).toString();
                Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
                groupChatIntent.putExtra("groupName", currentGroupName);
                startActivity(groupChatIntent);
            }
        });

        return mGroupFragmentView;
    }

    private void InitializeFields() {
        mListView = mGroupFragmentView.findViewById(R.id.list_view);
        mArratAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, mListOfGroups);
        mListView.setAdapter(mArratAdapter);
    }

    private void RetriveAndDisplayGroup() {
        mGroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashSet<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()){
                set.add(((DataSnapshot)iterator.next()).getKey());
                }

                mListOfGroups.clear();
                mListOfGroups.addAll(set);
                mArratAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
