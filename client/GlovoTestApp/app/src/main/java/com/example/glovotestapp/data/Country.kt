package com.example.glovotestapp.data

import android.support.annotation.Keep
import com.google.gson.annotations.SerializedName

data class Country(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String
)