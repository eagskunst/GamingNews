package com.eagskunst.emmanuel.gamingnews.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.models.ReleasesModel;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by eagskunst on 10/01/2019
 */
public class ReleasesAdapter extends RecyclerView.Adapter<ReleasesAdapter.ReleasesViewHolder>{

    private List<ReleasesModel> releasesList;
    private OnReleaseClickListener clickListener;

    public ReleasesAdapter(List<ReleasesModel> releasesList, OnReleaseClickListener clickListener){
        this.releasesList = releasesList;
        this.clickListener = clickListener;
    }


    @NonNull
    @Override
    public ReleasesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_new_releases, viewGroup, false);
        return new ReleasesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReleasesViewHolder holder, int i) {
        ReleasesModel release = releasesList.get(i);
        holder.gameName.setText(release.getGameName());
        holder.gameDate.setText(release.getGameReleaseDate());
        holder.gamePlatforms.setText(release.getGamePlatforms());
        if(release.getGameCoverUrl() == null){
            holder.gameCover.setVisibility(View.GONE);
        }
        else{
            Picasso.get()
                    .load(release.getGameCoverUrl())
                    .into(holder.gameCover);
        }
        holder.onClick(release, clickListener);
    }

    @Override
    public int getItemCount() {
        return releasesList.size();
    }

    class ReleasesViewHolder extends RecyclerView.ViewHolder{
        private ImageView gameCover;
        private TextView gameName;
        private TextView gameDate;
        private TextView gamePlatforms;

        public ReleasesViewHolder(@NonNull View itemView) {
            super(itemView);
            gameCover = itemView.findViewById(R.id.game_imageView);
            gameName = itemView.findViewById(R.id.game_name);
            gameDate = itemView.findViewById(R.id.game_release_date);
            gamePlatforms = itemView.findViewById(R.id.game_platform_releases);
        }

        private void onClick(ReleasesModel release, OnReleaseClickListener clickListener){
            itemView.setOnClickListener(view -> clickListener.OnItemClick(release));
        }
    }

    public interface OnReleaseClickListener{
        void OnItemClick(ReleasesModel release);
    }
}
