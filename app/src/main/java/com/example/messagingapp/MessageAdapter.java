package com.example.messagingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyHolder> {

    Context context;
    List<MessageModel> modelslist;


    public MessageAdapter(Context context, List<MessageModel> modelslist) {
        this.context = context;
        this.modelslist = modelslist;


    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.layoutformessages, parent, false);
        return new MyHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        String themessage = modelslist.get(position).getMessage();
        String theusername = modelslist.get(position).getUsername();
        String thephoto = modelslist.get(position).getPhotoid();

        if (thephoto!=null) {

            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(context).load(thephoto).into(holder.imageView);


        }


        holder.message.setText(themessage);
        holder.username.setText(theusername);

    }

    @Override
    public int getItemCount() {
        return modelslist.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{


        TextView message, username;
        ImageView imageView;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.message);
            username = itemView.findViewById(R.id.username);
            imageView = itemView.findViewById(R.id.messageImage);
        }


    }
}
