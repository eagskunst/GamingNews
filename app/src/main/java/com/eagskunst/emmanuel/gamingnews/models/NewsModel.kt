package com.eagskunst.emmanuel.gamingnews.models

import android.os.Parcel
import android.os.Parcelable
import com.eagskunst.emmanuel.gamingnews.utility.SimpleDateSingleton
import java.text.ParseException
import java.util.Date

class NewsModel(
    var newsImage: String,
    var title: String,
    var subtext: String,
    var link: String,
    var pubDate: Date,
    var channelName: String
) : Parcelable {

    private constructor(inParcel: Parcel) : this(
        inParcel.readString().orEmpty(),
        inParcel.readString().orEmpty(),
        inParcel.readString().orEmpty(),
        inParcel.readString().orEmpty(),
        Date(),
        inParcel.readString().orEmpty()
    ) {
        pubDate = parseDate(inParcel.readString())
    }

    private fun parseDate(dateString: String?): Date {
        return try {
            SimpleDateSingleton.getInstance().inputSdf.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }
    }

    fun formattedDate(): String {
        return SimpleDateSingleton.getInstance().toSdf.format(pubDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(newsImage)
        dest.writeString(title)
        dest.writeString(subtext)
        dest.writeString(link)
        dest.writeString(channelName)
        dest.writeString(SimpleDateSingleton.getInstance().inputSdf.format(pubDate))
    }

    companion object CREATOR : Parcelable.Creator<NewsModel> {
        override fun createFromParcel(inParcel: Parcel): NewsModel {
            return NewsModel(inParcel)
        }

        override fun newArray(size: Int): Array<NewsModel?> {
            return arrayOfNulls(size)
        }
    }
}
