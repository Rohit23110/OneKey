package com.example.onekey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PasswordRecyclerViewAdapter extends RecyclerView.Adapter<PasswordRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<Password> mPassword;
    private Context mContext;

    public PasswordRecyclerViewAdapter(ArrayList<Password> mPassword, Context mContext) {
        this.mPassword = mPassword;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.passwordlist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordRecyclerViewAdapter.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        holder.urlImagePreview.setImageResource(R.drawable.password_icon);
        holder.urlPreview.setText(mPassword.get(position).getUrl());
        holder.username.setText(mPassword.get(position).getUsername());
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked on " + mPassword.get(position).getUrl());
//                    Toast.makeText(getActivity(), mPassword.get(position).getUrl(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, ViewEditPassword.class);
                intent.putExtra("Uniqid","From_Add_Password");
                intent.putExtra("URL", mPassword.get(position).getUrl());
                intent.putExtra("Username", mPassword.get(position).getUsername());
                intent.putExtra("Password", mPassword.get(position).getPassword());
                intent.putExtra("Id", mPassword.get(position).getId());
                intent.putExtra("Timestamp", mPassword.get(position).getTimestamp().toDate().getTime());
                ((Activity) mContext).startActivityForResult(intent, 3);
                //v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPassword.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView urlImagePreview;
        TextView urlPreview;
        TextView username;
        RelativeLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            urlImagePreview = itemView.findViewById(R.id.urlimagepreview);
            urlPreview = itemView.findViewById(R.id.urlpreview);
            username = itemView.findViewById(R.id.username);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}

