package com.eagskunst.emmanuel.gamingnews.fragments.releases.mvp

import android.util.Log
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.models.Cover
import com.eagskunst.emmanuel.gamingnews.models.Game
import com.eagskunst.emmanuel.gamingnews.models.ReleasesModel
import com.eagskunst.emmanuel.gamingnews.models.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Created by eagskunst on 11/01/2019
 */
class ReleasePresenter(private val model: ReleaseModel) : ReleaseView.Presenter, ReleaseView.OnReleasesListener {

    private var view: ReleaseView.View? = null
    private var length = ReleaseView.Presenter.platforms.size
    private val releasesList: MutableList<ReleasesModel> = ArrayList()

    override fun getReleasesByPlatform() {
        view!!.changeTextMessage(R.string.getting_releases)
        val millis = System.currentTimeMillis() / 1000
        Log.d(ReleasePresenter::class.java.simpleName, "getReleasesByPlatform: $length")
        for (platform in ReleaseView.Presenter.platforms) {
            model.getReleasesByPlatform(millis, platform, this)
        }
    }

    override fun createView(view: ReleaseView.View) {
        this.view = view
    }

    override fun destroyView() {
        this.view = null
    }

    override fun sortListByDate() {
        view!!.changeTextMessage(R.string.sorting_by_date)
        releasesList.sortWith(Comparator { release1, release2 ->
            val value1 = release1.gameReleaseDate.split("-")[2].toInt()
            val value2 = release2.gameReleaseDate.split("-")[2].toInt()
            value1.compareTo(value2)
        })
        view!!.updateList(releasesList)
    }

    override fun onGetReleasesSuccess(gamesList: List<Response>) {
        for (gameInfo in gamesList) {
            val game = gameInfo.game
            // Verifying that is a valid game
            if (game != null && game.name != null && game.name != "") {
                val platform = getPlatformByNumber(gameInfo) // Get game platform
                // Verifiying the platform exist and that the game is not already added
                if (platform != null && !isAdded(game.name!!, platform)) {
                    val url = getCoverUrl(game.cover)
                    // Check for a valid date
                    if (gameInfo.human != null && gameInfo.human!!.length > 8) {
                        var date = gameInfo.human!!
                        if (Locale.getDefault().language == "es") date = changeToSpanish(date)
                        if (isInThisMonth(date)) {
                            val release = ReleasesModel(url ?: "", game.name!!, date, getPlatformByNumber(gameInfo)!!, game.gameUrl ?: "")
                            releasesList.add(release)
                        }
                    }
                }
            }
        }
        length--
        if (length == 0) {
            sortListByDate()
        }
    }

    override fun erasePassedDate(list: MutableList<ReleasesModel>, day: Int) {
        for (j in 0 until 3) {
            var i = 0
            while (i < list.size) {
                val dates = list[i].gameReleaseDate.split("-")
                val releaseDate = dates[2].toInt()
                if (day > releaseDate) {
                    list.removeAt(i)
                }
                i++
            }
        }
    }

    private fun isAdded(name: String, platform: String): Boolean {
        for (i in releasesList.indices) {
            if (releasesList[i].gameName == name) {
                if (releasesList[i].gamePlatforms.contains(platform)) return true
                val builder = releasesList[i].gamePlatforms + ", " + platform
                releasesList[i].gamePlatforms = builder
                return true
            }
        }
        return false
    }

    private fun getPlatformByNumber(game: Response): String? {
        return when (game.platform) {
            6 -> "PC"
            49 -> "Xbox One"
            48 -> "PS4"
            130 -> "Nintendo Switch"
            else -> null
        }
    }

    private fun getCoverUrl(cover: Cover?): String? {
        var url: String? = null
        try {
            url = cover!!.url!!.replaceFirst("t_thumb", "t_cover_big")
            url = "https://" + url.substring(2, url.length)
        } catch (e: NullPointerException) {
            Log.d("ReleasesPresenter", "getCoverUrl: NullPointerException")
        }
        return url
    }

    private fun changeToSpanish(date: String): String {
        val split = date.split("-")
        val builder = StringBuilder()
        builder.append(split[0])
        builder.append("-")
        when (split[1]) {
            "Jan" -> builder.append("Ene")
            "Apr" -> builder.append("Abr")
            "Aug" -> builder.append("Ago")
            "Dec" -> builder.append("Dic")
            else -> builder.append(split[1])
        }
        builder.append("-")
        builder.append(split[2])
        return builder.toString()
    }

    private fun isInThisMonth(date: String): Boolean {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("MMM", Locale.getDefault())
        val month = formatter.format(calendar.time)
        val releaseMonth = date.split("-")
        return releaseMonth[1].equals(month, ignoreCase = true)
    }

    override fun onGetReleasesFail(message: String) {
        view!!.showToastError(message)
        view!!.setTryAgain()
        length--
        if (length == 0) {
            sortListByDate()
        }
    }

    override fun onGetReleasesFail(message: Int) {
        view!!.showToastError(message)
        view!!.setTryAgain()
        length--
        if (length == 0) {
            sortListByDate()
        }
    }

    fun getReleasesList(): List<ReleasesModel> {
        return releasesList
    }
}
