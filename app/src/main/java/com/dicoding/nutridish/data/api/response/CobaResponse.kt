package com.dicoding.nutridish.data.api.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class EventResponse(

    @field:SerializedName("listEvents")
    val listEvents: List<ListEventsItem> = listOf(),

    @field:SerializedName("error")
    val error: Boolean = false,

    @field:SerializedName("message")
    val message: String = ""
)

data class EventApiResponse(
    val error: Boolean,
    val message: String,
    val event: ListEventsItem
)

@Parcelize
data class ListEventsItem(

    @field:SerializedName("summary")
    val summary: String = "",

    @field:SerializedName("mediaCover")
    val mediaCover: String = "",

    @field:SerializedName("registrants")
    val registrants: Int = 0,

    @field:SerializedName("imageLogo")
    val imageLogo: String = "",

    @field:SerializedName("link")
    val link: String = "",

    @field:SerializedName("description")
    val description: String = "",

    @field:SerializedName("ownerName")
    val ownerName: String = "",

    @field:SerializedName("cityName")
    val cityName: String = "",

    @field:SerializedName("quota")
    val quota: Int = 0,

    @field:SerializedName("name")
    val name: String = "",

    @field:SerializedName("id")
    var id: Int = 0,

    @field:SerializedName("beginTime")
    val beginTime: String = "",

    @field:SerializedName("endTime")
    val endTime: String = "",

    @field:SerializedName("category")
    val category: String = "",

    ) : Parcelable
