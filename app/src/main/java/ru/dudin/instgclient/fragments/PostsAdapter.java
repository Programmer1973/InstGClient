package ru.dudin.instgclient.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import ru.dudin.instgclient.R;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private final LayoutInflater mInflater;
    private final Picasso mPicasso = Picasso.get();

    private ClickListener mClickListener;

    private List<JSONObject> mUserMediaEndpoints;

    PostsAdapter(Context context, List<JSONObject> userMediaEndpoints) {
        mInflater = LayoutInflater.from(context);
        mUserMediaEndpoints = userMediaEndpoints;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.view_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        mPicasso.cancelRequest(holder.photoImageView);
        holder.photoImageView.setImageBitmap(null);

        try {
            mPicasso
                    .load(mUserMediaEndpoints
                            .get(position)
                            .getJSONObject("images")
                            .getJSONObject("low_resolution")
                            .getString("url"))
                    .placeholder(R.drawable.my_place_holder)
                    .resizeDimen(R.dimen.image_size, R.dimen.image_size)
                    .into(holder.photoImageView);

            holder.heartImageView.setImageResource(R.drawable.icons8_heart_24);

            holder.textViewLikes.setText(String.valueOf(mUserMediaEndpoints
                                                                .get(position)
                                                                .getJSONObject("likes")
                                                                .getInt("count")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return  mUserMediaEndpoints == null ? 0 : mUserMediaEndpoints.size();
    }

    void setOnItemClickListener(ClickListener clickListener) {
        mClickListener = clickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

       final ImageView photoImageView;
       final ImageView heartImageView;
       final TextView textViewLikes;

        ViewHolder(final View itemView) {
            super(itemView);

            photoImageView = itemView.findViewById(R.id.photo_view_image);
            heartImageView = itemView.findViewById(R.id.heart_view_image);
            textViewLikes = itemView.findViewById(R.id.text_view_likes);

            photoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        mClickListener.onItemClick(mUserMediaEndpoints.get(getAdapterPosition())
                                                                    .getJSONObject("images")
                                                                    .getJSONObject("low_resolution")
                                                                    .getString("url"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public interface ClickListener {
        void onItemClick(String uri);
    }
}