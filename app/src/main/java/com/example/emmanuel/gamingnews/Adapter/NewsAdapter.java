package com.example.emmanuel.gamingnews.Adapter;

import android.net.Uri;
import android.nfc.Tag;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.emmanuel.gamingnews.Models.NewsModel;
import com.example.emmanuel.gamingnews.R;
import com.squareup.picasso.Picasso;


import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private final String TAG = "MainActivity";

    private List<NewsModel> newsList;
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
        if(!newsList.get(position).getNewsImage().isEmpty()){
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
