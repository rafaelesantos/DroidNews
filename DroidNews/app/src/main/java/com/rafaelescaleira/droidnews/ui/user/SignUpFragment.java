package com.rafaelescaleira.droidnews.ui.user;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.rafaelescaleira.droidnews.R;
import com.rafaelescaleira.droidnews.model.UserModel;

import java.util.HashMap;
import java.util.Map;

public class SignUpFragment extends Fragment {

    FirebaseAuth auth;
    EditText email, password, name;
    ProgressBar progressBar;

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View root = inflater.inflate(R.layout.fragment_sign_up, container, false);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        email = root.findViewById(R.id.signUpEmailEditText);
        password = root.findViewById(R.id.signUpPasswordEditText);
        name = root.findViewById(R.id.signUpNameEditText);
        progressBar = root.findViewById(R.id.signUpProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        final Button backButton = root.findViewById(R.id.signUpBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                Navigation.findNavController(root).popBackStack();
            }
        });

        final Button signUpGoButton = root.findViewById(R.id.signUpGoButton);
        signUpGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                progressBar.setVisibility(View.VISIBLE);
                signUpGoButton.setEnabled(false);
                backButton.setEnabled(false);

                if (!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty() && !name.getText().toString().isEmpty()) {

                    auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                FirebaseUser currentUser = auth.getCurrentUser();
                                UserModel userModel = new UserModel(email.getText().toString(), name.getText().toString());
                                Map<String, Object> userModelValues = userModel.toMap();
                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put("/users/" + currentUser.getUid(), userModelValues);

                                FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            signUpGoButton.setEnabled(true);
                                            backButton.setEnabled(true);
                                            Navigation.findNavController(root).navigate(R.id.navigation_user);
                                        } else {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            signUpGoButton.setEnabled(true);
                                            backButton.setEnabled(true);
                                            Toast.makeText(getContext(), "Não foi possível cadastrar-se\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });


                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                signUpGoButton.setEnabled(true);
                                backButton.setEnabled(true);
                                Toast.makeText(getContext(), "Não foi possível cadastrar-se\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    signUpGoButton.setEnabled(true);
                    backButton.setEnabled(true);
                    Toast.makeText(getContext(), "Campos Vazios\nPara efetuar o cadastro preencha todos os campos.", Toast.LENGTH_LONG).show();
                }
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            Navigation.findNavController(getView()).navigate(R.id.navigation_user);
        }
    }

    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
}