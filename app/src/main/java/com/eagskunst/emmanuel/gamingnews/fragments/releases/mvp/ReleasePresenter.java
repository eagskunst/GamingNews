package com.eagskunst.emmanuel.gamingnews.fragments.releases.mvp;

import android.util.Log;

import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.models.Cover;
import com.eagskunst.emmanuel.gamingnews.models.Game;
import com.eagskunst.emmanuel.gamingnews.models.ReleasesModel;
import com.eagskunst.emmanuel.gamingnews.models.Response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by eagskunst on 11/01/2019
 */
public class ReleasePresenter implements ReleaseView.Presenter, ReleaseView.OnReleasesListener{

    private ReleaseModel model;
    private ReleaseView.View view;
    private int length = platforms.length;
    private List<ReleasesModel> releasesList;

    public ReleasePresenter(ReleaseModel model){
        this.model = model;
        releasesList = new ArrayList<>();
    }

    @Override
    public void getReleasesByPlatform() {
        view.changeTextMessage(R.string.getting_releases);
        long millis = System.currentTimeMillis()/1000;
        Log.d(ReleasePresenter.class.getSimpleName(), "getReleasesByPlatform: "+length);
        for (int platform : platforms) {
            model.getReleasesByPlatform(millis, platform, this);
        }
    }

    @Override
    public void createView(ReleaseView.View view) {
        this.view = view;
    }

    @Override
    public void destroyView() {
        this.view = null;
    }

    @Override
    public void sortListByDate() {
        view.changeTextMessage(R.string.sorting_by_date);
        Collections.sort(releasesList, (release1, release2) ->{
            int value1 = Integer.valueOf(release1.getGameReleaseDate().split("-")[2]);
            int value2 = Integer.valueOf(release2.getGameReleaseDate().split("-")[2]);
            return Integer.compare(value1,value2);
        });
        view.updateList(releasesList);
    }

    @Override
    public void onGetReleasesSuccess(List<Response> gamesList) {
        for(Response gameInfo: gamesList){
            Game game = gameInfo.getGame();
            //Verifying that is a valid game
            if(game != null && game.getName() != null && !game.getName().equals("")){
                String platform = getPlatformByNumber(gameInfo); //Get game platform
                //Verifiying the platform exist and that the game is not already added
                if(platform != null && !isAdded(game.getName(), platform)){
                    String url = getCoverUrl(game.getCover());
                    //Check for a valid date
                    if(gameInfo.getHuman().length()>8){
                        String date = gameInfo.getHuman();
                        if(Locale.getDefault().getLanguage().equals("es"))
                            date = changeToSpanish(date);
                        if(isInThisMonth(date)){
                            ReleasesModel release = new ReleasesModel(url, game.getName() , date, getPlatformByNumber(gameInfo), game.getGameUrl());
                            releasesList.add(release);
                        }
                    }
                }
            }
        }
        length--;
        if(length == 0){
            sortListByDate();
        }
    }

    @Override
    public void erasePassedDate(List<ReleasesModel> list, int day){
        for(int i = 0;i<list.size() ;i++){
            String dates[] = list.get(i).getGameReleaseDate().split("-");
            int releaseDate = Integer.parseInt(dates[2]);
            if(releaseDate<day){
                releasesList.remove(i);
            }
            else{
                break;
            }
        }
    }

    private boolean isAdded(String name, String platform) {
        for (int i = 0;i<releasesList.size();i++){
            if(releasesList.get(i).getGameName().equals(name)){
                if(releasesList.get(i).getGamePlatforms().contains(platform))
                    return true;
                String builder = releasesList.get(i).getGamePlatforms() +
                        ", " + platform;
                releasesList.get(i).setGamePlatforms(builder);
                return true;
            }
        }
        return false;
    }

    private String getPlatformByNumber(Response game) {
        switch (game.getPlatform()){
            case 6:
                return "PC";
            case 49:
                return "Xbox One";
            case 48:
                return "PS4";
            case 130:
                return "Nintendo Switch";
            default:
                return null;
        }
    }

    private String getCoverUrl(Cover cover) {
        String url = null;
        try {
            url = cover.getUrl().replaceFirst("t_thumb", "t_cover_big");
            url = "https://" + url.substring(2, url.length());
        } catch (NullPointerException e) {
            Log.d("ReleasesPresenter", "getCoverUrl: NullPointerException");
        }
        return url;
    }

    private String changeToSpanish(String date) {
        String split[] = date.split("-");
        StringBuilder builder = new StringBuilder();
        builder.append(split[0]);
        builder.append("-");
        switch (split[1]){
            case "Jan":
                builder.append("Ene");
                break;
            case "Apr":
                builder.append("Abr");
                break;
            case "Aug":
                builder.append("Ago");
                break;
            case "Dec":
                builder.append("Dic");
                break;
            default:
                builder.append(split[1]);
                break;
        }
        builder.append("-");
        builder.append(split[2]);
        return builder.toString();
    }

    private boolean isInThisMonth(String date) {
        final Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM", Locale.getDefault());
        String month = formatter.format(calendar.getTime());
        month = month.substring(0,month.length()-1);
        String releaseMonth[] = date.split("-");
        return releaseMonth[1].equalsIgnoreCase(month);
    }

    @Override
    public void onGetReleasesFail(String message) {
        view.showToastError(message);
        view.setTryAgain();
        length--;
        if(length == 0){
            sortListByDate();
        }
    }

    @Override
    public void onGetReleasesFail(int message) {
        view.showToastError(message);
        view.setTryAgain();
        length--;
        if(length == 0){
            sortListByDate();
        }
    }

    public List<ReleasesModel> getReleasesList() {
        return releasesList;
    }
}
