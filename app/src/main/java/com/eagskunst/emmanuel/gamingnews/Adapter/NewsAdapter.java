package com.eagskunst.emmanuel.gamingnews.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.eagskunst.emmanuel.gamingnews.Models.NewsModel;
import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.Utility.SharedPreferencesLoader;
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

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_news,parent,false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        holder.textView1.setText(newsList.get(position).getTitle());
        holder.textView2.setText(newsList.get(position).getSubtext());
        if(!newsList.get(position).getNewsImage().isEmpty() && SharedPreferencesLoader.canLoadImages){
            holder.imageView.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(newsList.get(position).getNewsImage())
                    .into(holder.imageView);
        }
        else{
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

        private TextView textView1;
        private TextView textView2;
        private ImageView imageView;

        public NewsViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.iv_cardview);
            this.textView1 = itemView.findViewById(R.id.tv_cardview);
            this.textView2 = itemView.findViewById(R.id.tv2_cardview);
        }

        private void bind(final NewsModel newsModel, final OnItemClickListener onItemClickListener){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.OnItemClick(newsModel);
                }
            });
        }

        public interface OnItemClickListener{
            void OnItemClick(NewsModel item);
        }
    }
}
