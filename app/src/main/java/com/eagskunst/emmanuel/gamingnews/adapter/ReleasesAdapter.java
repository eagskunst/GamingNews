package com.eagskunst.emmanuel.gamingnews.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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

    public ReleasesAdapter(List<ReleasesModel> releasesList){
        this.releasesList = releasesList;
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
    }
}
