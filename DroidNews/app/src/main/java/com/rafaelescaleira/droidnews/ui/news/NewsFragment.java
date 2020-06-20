package com.rafaelescaleira.droidnews.ui.news;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;
import com.rafaelescaleira.droidnews.R;
import com.rafaelescaleira.droidnews.model.NewsModel;
import com.rafaelescaleira.droidnews.model.UserModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NewsFragment extends Fragment {

    ArrayList<NewsModel> newsModels;
    FirebaseAuth auth;
    UserModel userModel;

    public NewsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_news, container, false);

        auth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = auth.getCurrentUser();

        final RecyclerView recyclerView = root.findViewById(R.id.newsRecyclerView);

        FloatingActionButton floatingActionButton = root.findViewById(R.id.newsAddButton);
        floatingActionButton.setEnabled(false);

        if (currentUser != null) {
            floatingActionButton.setAlpha(1f);
            floatingActionButton.setEnabled(true);
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.navigation_detail_news);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);

        if (currentUser != null) {
            FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    userModel = dataSnapshot.getValue(UserModel.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("news");
        reference.keepSynced(true);
        reference.limitToLast(100).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                newsModels = new ArrayList<NewsModel>();

                for (DataSnapshot newsSnapshot : dataSnapshot.getChildren()){
                    newsModels.add(newsSnapshot.getValue(NewsModel.class));
                }

                NewsAdapter newsAdapter = new NewsAdapter(newsModels, userModel);
                recyclerView.setAdapter(newsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return root;
    }
}