package com.rafaelescaleira.droidnews.ui.news;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.rafaelescaleira.droidnews.R;
import com.rafaelescaleira.droidnews.model.NewsModel;
import com.rafaelescaleira.droidnews.model.UserModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private ArrayList<NewsModel> newsModels;
    private UserModel userModel;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title, message, date, tag, user;
        public ImageView image;

        public ViewHolder(View view) {
            super(view);

            title = view.findViewById(R.id.newsDetailTitleTextView);
            message = view.findViewById(R.id.newsDetailMessageTextView);
            date = view.findViewById(R.id.newsDetailDateTextView);
            tag = view.findViewById(R.id.newsDetailTagTextView);
            user = view.findViewById(R.id.newsDetailUserTextView);
            image = view.findViewById(R.id.newsDetailImageView);
        }
    }

    public NewsAdapter(ArrayList<NewsModel> newsModels, UserModel userModel) {
        this.newsModels = newsModels;
        this.userModel = userModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_news_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final NewsModel newsModel = newsModels.get(position);

        holder.title.setText(newsModel.title);
        holder.message.setText(newsModel.message);
        holder.date.setText(newsModel.date);
        holder.tag.setText(newsModel.tag);
        holder.user.setText(newsModel.user);

        Picasso.get().load(newsModel.image).into(holder.image);

        if (userModel != null && userModel.name.toLowerCase(Locale.ROOT).equals(newsModel.user.toLowerCase(Locale.ROOT))) {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Remover Notícia");
                    builder.setMessage("Para remover essa notícia basta clicar em remover.");
                    builder.setPositiveButton("Remover", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (newsModel.image == null) {
                                FirebaseDatabase.getInstance().getReference().child("news").child(newsModel.uuid).removeValue();
                            } else {
                                FirebaseStorage.getInstance().getReference().child("news").child(newsModel.uuid).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isComplete()) {
                                            FirebaseDatabase.getInstance().getReference().child("news").child(newsModel.uuid).removeValue();
                                        }
                                    }
                                });
                            }
                        }
                    });
                    builder.setNegativeButton("Voltar", null);
                    builder.create().show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return newsModels.size();
    }
}
