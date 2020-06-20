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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rafaelescaleira.droidnews.R;

public class SignInFragment extends Fragment {

    FirebaseAuth auth;
    EditText email, password;
    ProgressBar progressBar;

    public SignInFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_sign_in, container, false);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        email = root.findViewById(R.id.signInEmailEditText);
        password = root.findViewById(R.id.signInPasswordEditText);
        progressBar = root.findViewById(R.id.signInProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        final Button signUpButton = root.findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                Navigation.findNavController(root).navigate(R.id.navigation_sign_up);
            }
        });

        final Button signInButton = root.findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                signInButton.setEnabled(false);
                signUpButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                if (!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
                    auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.INVISIBLE);
                                signInButton.setEnabled(true);
                                signUpButton.setEnabled(true);
                                Navigation.findNavController(root).navigate(R.id.navigation_user);
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                signInButton.setEnabled(true);
                                signUpButton.setEnabled(true);
                                Toast.makeText(getContext(), "Não foi possível acessar\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    signInButton.setEnabled(true);
                    signUpButton.setEnabled(true);
                    Toast.makeText(getContext(), "Campos Vazios\nPara efetuar o login preencha todos os campos.", Toast.LENGTH_LONG).show();
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