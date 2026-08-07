package com.eagskunst.emmanuel.gamingnews.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.models.NewsModel
import com.eagskunst.emmanuel.gamingnews.utility.SharedPreferencesLoader
import com.squareup.picasso.Picasso

class NewsAdapter(
    private val newsList: MutableList<NewsModel>,
    private val onItemClickListener: NewsAdapter.NewsViewHolder.OnItemClickListener
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    private val newsListCopy: MutableList<NewsModel> = ArrayList()

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.tvTitle.text = newsList[position].title
        holder.tvDescription.text = newsList[position].subtext
        holder.tvPubDate.text = newsList[position].formattedDate()
        holder.tvChannelName.text = newsList[position].channelName
        if (newsList[position].newsImage.isNotEmpty() && SharedPreferencesLoader.canLoadImages) {
            holder.imageView.visibility = View.VISIBLE
            Picasso.get()
                .load(newsList[position].newsImage)
                .into(holder.imageView)
        } else {
            holder.imageView.visibility = View.GONE
        }
        holder.bind(newsList[position], onItemClickListener)
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    fun getNewsListCopy(): MutableList<NewsModel> {
        return newsListCopy
    }

    fun filter(text: String) {
        newsList.clear()
        if (text.isEmpty()) {
            newsList.addAll(newsListCopy)
        } else {
            val lowerText = text.lowercase()
            for (item in newsListCopy) {
                if (item.title.lowercase().contains(lowerText) && !newsList.contains(item)) {
                    newsList.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.iv_cardview)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_cardview)
        val tvDescription: TextView = itemView.findViewById(R.id.tv2_cardview)
        val tvChannelName: TextView = itemView.findViewById(R.id.tv_channel_title)
        val tvPubDate: TextView = itemView.findViewById(R.id.tv_publish_date)

        fun bind(newsModel: NewsModel, onItemClickListener: OnItemClickListener) {
            itemView.setOnClickListener { onItemClickListener.OnItemClick(newsModel) }
        }

        interface OnItemClickListener {
            fun OnItemClick(item: NewsModel)
        }
    }
}
