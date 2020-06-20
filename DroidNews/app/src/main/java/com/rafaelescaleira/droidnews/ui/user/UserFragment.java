package com.rafaelescaleira.droidnews.ui.user;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rafaelescaleira.droidnews.R;
import com.rafaelescaleira.droidnews.model.NewsModel;
import com.rafaelescaleira.droidnews.model.UserModel;
import com.rafaelescaleira.droidnews.ui.news.NewsAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class UserFragment extends Fragment {

    FirebaseAuth auth;
    ArrayList<NewsModel> newsModels;
    TextView name, email;

    public UserFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_user, container, false);

        auth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {

            name = root.findViewById(R.id.userNameTextView);
            email = root.findViewById(R.id.userEmailTextView);
            final RecyclerView recyclerView = root.findViewById(R.id.userNewsRecyclerView);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);

            recyclerView.setLayoutManager(linearLayoutManager);

            Button signOut = root.findViewById(R.id.userSignOutButton);
            signOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    auth.signOut();
                    Navigation.findNavController(root).popBackStack();
                }
            });

            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            userReference.keepSynced(true);
            userReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    final UserModel userModel = dataSnapshot.getValue(UserModel.class);

                    name.setText(userModel.name);
                    email.setText(userModel.email);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("news");
                    reference.keepSynced(true);
                    reference.limitToLast(100).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            newsModels = new ArrayList<NewsModel>();

                            for (DataSnapshot newsSnapshot : dataSnapshot.getChildren()){
                                if (newsSnapshot.getValue(NewsModel.class).user.toLowerCase(Locale.ROOT).equals(userModel.name.toLowerCase(Locale.ROOT))) {
                                    newsModels.add(newsSnapshot.getValue(NewsModel.class));
                                }
                            }

                            recyclerView.setAdapter(new NewsAdapter(newsModels, userModel));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {  }
            });
        }

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        auth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Navigation.findNavController(getView()).popBackStack();
        }
    }
}