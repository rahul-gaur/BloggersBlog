package com.rahulgaur.bloggersblog.account;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahulgaur.bloggersblog.R;

import java.util.ArrayList;

public class GridViewAdapter extends ArrayAdapter {

    private Context context;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private ArrayList<GridViewList> objects = new ArrayList<>();
    private GridViewList gridViewList;

    public GridViewAdapter(@NonNull Context context, int resource, ArrayList<GridViewList> list) {
        super(context, resource, list);
        this.context = context;
        objects = list;
    }

    public class Holder {
        ImageView post_image;
        ProgressBar progressBar;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Holder holder;
        View row = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int int1;
            row = inflater.inflate(R.layout.grid_view_item, parent, false);
            holder = new Holder();
            holder.post_image = row.findViewById(R.id.grid_item_imageView);
            holder.progressBar = row.findViewById(R.id.grid_item_progressBar);
            row.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        gridViewList = new GridViewList();
        Glide.with(context).load(objects.get(position).getImageURL())
                .into(holder.post_image);
        holder.progressBar.setVisibility(View.INVISIBLE);
        return row;
    }
}
