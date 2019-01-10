package com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp;

import android.text.Html;

import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.models.NewsModel;
import com.prof.rssparser.Article;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by eagskunst on 09/01/2019
 */
public class NewsListPresenter implements NewsListView.Presenter, NewsListView.OnArticlesLoadedListener {

    private static final int OBJ_NUMBER = 65532;


    private NewsListView.View view;
    private List<NewsModel> list;
    private Set<String> titles;
    private NewsListModel model;
    private int length;

    public NewsListPresenter(NewsListModel model){
        this.model = model;
        model.setListener(this);
        titles = new HashSet<>();
    }

    @Override
    public void onCreateView(NewsListView.View view) {
        this.view = view;
        this.list = new ArrayList<>();
    }

    @Override
    public void onDestroyView() {
        this.view = null;
    }


    @Override
    public void getArticles(String urls[]) {
       if(view.checkInternetConnection()){
           this.length = urls.length;
           for (String url : urls) {
               model.getArticlesFromUrl(url);
           }
       }
       else{
           view.createAlertDialog(R.string.check_your_connection);
       }
    }

    @Override
    public String formatDescription(String content) {
        StringBuilder mString = new StringBuilder();
        if(!content.isEmpty()){
            content = Html.fromHtml(content).toString().replace((char) OBJ_NUMBER,' ').trim();
            if(content.indexOf('.') != -1){
                mString.append(content.substring(0,content.indexOf('.')));
                mString.append('.');
            }
            else{
                mString.append(content);
                mString.append("...");
            }
            if(mString.length()>180){
                mString.delete(180,mString.length()-1);
                mString.append("...");
            }
        }

        return mString.toString();
    }

    @Override
    public void sortListByDate() {
        Collections.sort(list, new Comparator<NewsModel>() {
            @Override
            public int compare(NewsModel newsModel, NewsModel t1) {
                if(newsModel.getPubDate() != null && t1.getPubDate() != null){
                    return t1.getPubDate().compareTo(newsModel.getPubDate());
                }
                else{
                    return 1;
                }
            }
        });
        view.updateList(list);
    }

    @Override
    public void onGetArticlesSucess(List<Article> articles) {
        for(Article article : articles){
            if(!titles.contains(article.getTitle())){
                NewsModel newsModel = new NewsModel(article.getImage() == null ? "":article.getImage(),article.getTitle(),
                        formatDescription(article.getDescription()),article.getLink(),article.getPubDate());
                list.add(newsModel);
                titles.add(article.getTitle());
            }
        }
        length--;
        if(length == 0){
            sortListByDate();
        }
    }

    @Override
    public void onGetArticlesFail() {
        view.showToastError(R.string.cant_get_articles);
        length--;
        if(length == 0){
            sortListByDate();
        }
    }
}
