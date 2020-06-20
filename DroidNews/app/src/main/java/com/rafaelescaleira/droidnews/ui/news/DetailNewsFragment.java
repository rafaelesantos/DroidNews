package com.rafaelescaleira.droidnews.ui.news;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rafaelescaleira.droidnews.R;
import com.rafaelescaleira.droidnews.model.NewsModel;
import com.rafaelescaleira.droidnews.model.UserModel;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import kotlin.UByteArray;

public class DetailNewsFragment extends Fragment {

    FirebaseAuth auth;
    StorageReference storageReference;
    EditText title, message, tag, link;
    ImageView imageView;

    private static int IMAGE_PICK_CODE = 1000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View root = inflater.inflate(R.layout.fragment_detail_news, container, false);

        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("news");
        final FirebaseUser currentUser = auth.getCurrentUser();

        title = root.findViewById(R.id.warningDetailTitleEditText);
        message = root.findViewById(R.id.warningDetailMessageEditText);
        tag = root.findViewById(R.id.warningDetailTagEditText);
        imageView = root.findViewById(R.id.warningDetailImageView);

        final Button publishButton = root.findViewById(R.id.warningDetailGoButton);
        final Button backButton = root.findViewById(R.id.warningDetailBackButton);
        final Button chooseButton = root.findViewById(R.id.warningDetailChooseButton);
        final Button deleteButton = root.findViewById(R.id.warningDetailDeleteButton);
        final ProgressBar progressBar = root.findViewById(R.id.warningDetailProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                Navigation.findNavController(root).popBackStack();
            }
        });

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                pickImageFromGallery();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                imageView.setImageDrawable(null);
            }
        });

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard();
                progressBar.setVisibility(View.VISIBLE);
                publishButton.setEnabled(false);
                backButton.setEnabled(false);
                chooseButton.setEnabled(false);
                deleteButton.setEnabled(false);

                if (!title.getText().toString().isEmpty() && !message.getText().toString().isEmpty()) {

                    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
                    userReference.keepSynced(true);
                    userReference.addValueEventListener(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            final UserModel userModel = dataSnapshot.getValue(UserModel.class);
                            Long tsLong = System.currentTimeMillis();
                            final String uuid = tsLong.toString();
                            final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            final LocalDateTime localDateTime = LocalDateTime.now();

                            if (imageView.getDrawable() != null && userModel != null) {

                                storageReference.child(uuid).putBytes(getPNG(imageView)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            storageReference.child(uuid).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @RequiresApi(api = Build.VERSION_CODES.O)
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if (task.isSuccessful()) {
                                                        NewsModel newsModel = new NewsModel(
                                                                uuid,
                                                                userModel.name,
                                                                tag.getText().toString(),
                                                                dateTimeFormatter.format(localDateTime),
                                                                task.getResult().toString(),
                                                                title.getText().toString(),
                                                                message.getText().toString()
                                                        );

                                                        Map<String, Object> newsModelValues = newsModel.toMap();
                                                        Map<String, Object> childUpdates = new HashMap<>();
                                                        childUpdates.put("/news/" + uuid, newsModelValues);

                                                        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    publishButton.setEnabled(true);
                                                                    backButton.setEnabled(true);
                                                                    chooseButton.setEnabled(true);
                                                                    deleteButton.setEnabled(true);
                                                                    Navigation.findNavController(root).popBackStack();
                                                                } else {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    publishButton.setEnabled(true);
                                                                    backButton.setEnabled(true);
                                                                    chooseButton.setEnabled(true);
                                                                    deleteButton.setEnabled(true);
                                                                    Toast.makeText(getContext(), "Erro ao publicar a notícia\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        publishButton.setEnabled(true);
                                                        backButton.setEnabled(true);
                                                        chooseButton.setEnabled(true);
                                                        deleteButton.setEnabled(true);
                                                        Toast.makeText(getContext(), "Erro ao fazer upload da imagem\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            publishButton.setEnabled(true);
                                            backButton.setEnabled(true);
                                            chooseButton.setEnabled(true);
                                            deleteButton.setEnabled(true);
                                            Toast.makeText(getContext(), "Erro ao fazer upload da imagem\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else if (userModel != null) {

                                NewsModel newsModel = new NewsModel(
                                        uuid,
                                        userModel.name,
                                        tag.getText().toString(),
                                        dateTimeFormatter.format(localDateTime),
                                        null,
                                        title.getText().toString(),
                                        message.getText().toString()
                                );

                                Map<String, Object> newsModelValues = newsModel.toMap();
                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put("/news/" + uuid, newsModelValues);

                                FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            publishButton.setEnabled(true);
                                            backButton.setEnabled(true);
                                            chooseButton.setEnabled(true);
                                            deleteButton.setEnabled(true);
                                            Navigation.findNavController(root).popBackStack();
                                        } else {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            publishButton.setEnabled(true);
                                            backButton.setEnabled(true);
                                            chooseButton.setEnabled(true);
                                            deleteButton.setEnabled(true);
                                            Toast.makeText(getContext(), "Erro ao publicar a notícia\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    publishButton.setEnabled(true);
                    backButton.setEnabled(true);
                    chooseButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    Toast.makeText(getContext(), "Campos Vazios\nPara efetuar a publicação preencha todos os campos obrigatórios.", Toast.LENGTH_LONG).show();
                }
            }
        });

        return root;
    }

    private void pickImageFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private byte[] getPNG(ImageView imageView) {

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case 1001:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(getContext(), "Permissão Negada", Toast.LENGTH_SHORT).show();
                }
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            Picasso.get().load(data.getDataString()).into(imageView);
        } finally {
        }
    }

    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
}