package com.matrix_maeny.onlinephotostorage;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.viewHolder> {

    private final Context context;
    private final ArrayList<String> urlList;

    public PhotoAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.urlList = list;


    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.photo_view_model, parent, false);

        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        Picasso.get().load(urlList.get(position)).placeholder(R.drawable.placeholder).into(holder.imageViewMPA);

        holder.imageViewMPA.setOnClickListener(v -> {
            Intent intent = new Intent(context.getApplicationContext(), ImageActivity.class);
            intent.putExtra("url", urlList.get(position));
            context.startActivity(intent);
        });


    }


    @Override
    public int getItemCount() {
        return urlList.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewMPA;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewMPA = itemView.findViewById(R.id.imageViewMPA);
        }
    }
}
