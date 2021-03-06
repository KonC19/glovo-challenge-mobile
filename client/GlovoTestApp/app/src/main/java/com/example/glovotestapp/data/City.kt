package com.example.glovotestapp.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class City(
    @SerializedName("code") val code: String,
    @SerializedName("country_code") val countryCode: String,
    @SerializedName("name") val name: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("enabled") val enabled: Boolean,
    @SerializedName("busy") val busy: Boolean,
    @SerializedName("time_zone") val timeZone: String,
    @SerializedName("language_code") val languageCode: String,
    @SerializedName("working_area") val workingArea: List<String>
) : Parcelable