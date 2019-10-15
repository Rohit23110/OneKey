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

public class NotesRecyclerViewAdapter extends RecyclerView.Adapter<NotesRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<Notes> mNotes;
    private Context mContext;

    public NotesRecyclerViewAdapter(ArrayList<Notes> mNotes, Context mContext) {
        this.mNotes = mNotes;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.noteslist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesRecyclerViewAdapter.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        holder.titleImageView.setImageResource(R.drawable.notes_icon);
        holder.titleView.setText(mNotes.get(position).getTitle());
        if(mNotes.get(position).getContent().length()<=40) {
            holder.contentView.setText(mNotes.get(position).getContent());
        } else {
            holder.contentView.setText(mNotes.get(position).getContent().substring(0, 41)+"...");
        }
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked on " + mNotes.get(position).getTitle());
//                    Toast.makeText(getActivity(), mPassword.get(position).getUrl(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, ViewEditNotes.class);
                intent.putExtra("Uniqid","From_Add_Notes");
                intent.putExtra("Title", mNotes.get(position).getTitle());
                intent.putExtra("Content", mNotes.get(position).getContent());
                intent.putExtra("Id", mNotes.get(position).getId());
                intent.putExtra("Timestamp", mNotes.get(position).getTimestamp().toDate().getTime());
                ((Activity) mContext).startActivityForResult(intent, 4);
                //v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() { return mNotes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView titleImageView;
        TextView titleView;
        TextView contentView;
        RelativeLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleImageView = itemView.findViewById(R.id.titleimagepreview);
            titleView = itemView.findViewById(R.id.titlepreview);
            parentLayout = itemView.findViewById(R.id.parent_layout_notes);
            contentView = itemView.findViewById(R.id.contentpreview);
        }
    }
}

