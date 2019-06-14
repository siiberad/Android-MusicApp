package com.siiberad.musicapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.siiberad.musicapp.R;
import com.siiberad.musicapp.model.SongModel;

import java.util.List;

public class SongPlaylistAdapter extends RecyclerView.Adapter<SongPlaylistAdapter.CustomViewHolder> {

    private List<SongModel> dataList;
    private Context context;
    private View.OnClickListener onClickListener;

    public SongPlaylistAdapter(Context context, List<SongModel> dataList, View.OnClickListener onClickListener){
        this.context = context;
        this.dataList = dataList;
        this.onClickListener = onClickListener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.custom_row, parent, false);
        return new CustomViewHolder(view);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView singer;
        TextView album;
        TextView duration;

        CustomViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            singer = itemView.findViewById(R.id.singer);
            album = itemView.findViewById(R.id.album);
            duration = itemView.findViewById(R.id.duration);
        }
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        final SongModel songModel = dataList.get(position);
        holder.title.setText(songModel.getTitle());
        holder.singer.setText(songModel.getSinger());
        holder.album.setText(songModel.getAlbum());
        holder.duration.setText(songModel.getDuration());
        holder.itemView.setTag(songModel);
        holder.itemView.setOnClickListener(onClickListener);

    }

    @Override
    public int getItemCount() {
        if(dataList!=null) {
            return dataList.size();
        }else
            return 0;
    }
}