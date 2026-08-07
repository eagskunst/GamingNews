package com.eagskunst.emmanuel.gamingnews.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.models.ReleasesModel
import com.squareup.picasso.Picasso

/**
 * Created by eagskunst on 10/01/2019
 */
class ReleasesAdapter(
    private val releasesList: List<ReleasesModel>,
    private val clickListener: OnReleaseClickListener
) : RecyclerView.Adapter<ReleasesAdapter.ReleasesViewHolder>() {

    @NonNull
    override fun onCreateViewHolder(@NonNull viewGroup: ViewGroup, i: Int): ReleasesViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.cardview_new_releases, viewGroup, false)
        return ReleasesViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: ReleasesViewHolder, i: Int) {
        val release = releasesList[i]
        holder.gameName.text = release.gameName
        holder.gameDate.text = release.gameReleaseDate
        holder.gamePlatforms.text = release.gamePlatforms
        if (release.gameCoverUrl == null) {
            holder.gameCover.visibility = View.GONE
        } else {
            Picasso.get()
                .load(release.gameCoverUrl)
                .into(holder.gameCover)
        }
        holder.onClick(release, clickListener)
    }

    override fun getItemCount(): Int {
        return releasesList.size
    }

    inner class ReleasesViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameCover: ImageView = itemView.findViewById(R.id.game_imageView)
        val gameName: TextView = itemView.findViewById(R.id.game_name)
        val gameDate: TextView = itemView.findViewById(R.id.game_release_date)
        val gamePlatforms: TextView = itemView.findViewById(R.id.game_platform_releases)

        fun onClick(release: ReleasesModel, clickListener: OnReleaseClickListener) {
            itemView.setOnClickListener { clickListener.OnItemClick(release) }
        }
    }

    interface OnReleaseClickListener {
        fun OnItemClick(release: ReleasesModel)
    }
}
