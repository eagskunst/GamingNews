package com.eagskunst.emmanuel.gamingnews.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eagskunst.emmanuel.gamingnews.models.NewsModel;
import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.utility.SharedPreferencesLoader;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<NewsModel> newsList;
    private List<NewsModel> newsListCopy = new ArrayList<>();
    private NewsViewHolder.OnItemClickListener onItemClickListener;

    public NewsAdapter(List<NewsModel> newsList, NewsViewHolder.OnItemClickListener onItemClickListener) {
        this.newsList = newsList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_news,parent,false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        holder.tvTitle.setText(newsList.get(position).getTitle());
        holder.tvDescription.setText(newsList.get(position).getSubtext());
        holder.tvPubDate.setText(newsList.get(position).formattedDate());
        holder.tvChannelName.setText(newsList.get(position).getChannelName());
        if(!newsList.get(position).getNewsImage().isEmpty() && SharedPreferencesLoader.canLoadImages){
            holder.imageView.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(newsList.get(position).getNewsImage())
                    .into(holder.imageView);
        }
        else {
            holder.imageView.setVisibility(View.GONE);
        }
        holder.bind(newsList.get(position),onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public List<NewsModel> getNewsListCopy() {
        return newsListCopy;
    }


    public void filter(String text) {
        newsList.clear();
        if(text.isEmpty()){
            newsList.addAll(newsListCopy);
        } else{
            text = text.toLowerCase();
            for(NewsModel item: newsListCopy){
                if(item.getTitle().toLowerCase().contains(text) && !newsList.contains(item)){
                    newsList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }


    public static class NewsViewHolder extends RecyclerView.ViewHolder{

        private TextView tvTitle;
        private TextView tvDescription;
        private TextView tvChannelName;
        private TextView tvPubDate;
        private ImageView imageView;

        public NewsViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.iv_cardview);
            this.tvTitle = itemView.findViewById(R.id.tv_cardview);
            this.tvDescription = itemView.findViewById(R.id.tv2_cardview);
            this.tvChannelName = itemView.findViewById(R.id.tv_channel_title);
            this.tvPubDate = itemView.findViewById(R.id.tv_publish_date);
        }

        private void bind(final NewsModel newsModel, final OnItemClickListener onItemClickListener){
            itemView.setOnClickListener(view -> onItemClickListener.OnItemClick(newsModel));
        }

        public interface OnItemClickListener{
            void OnItemClick(NewsModel item);
        }
    }
}
