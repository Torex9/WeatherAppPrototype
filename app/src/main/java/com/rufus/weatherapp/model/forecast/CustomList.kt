package com.rufus.weatherapp.model.forecast

import com.google.gson.annotations.SerializedName
import com.rufus.weatherapp.model.weather.Clouds
import com.rufus.weatherapp.model.weather.Sys
import com.rufus.weatherapp.model.weather.Weather
import com.rufus.weatherapp.model.weather.Wind

data class CustomList (
    @SerializedName("dt") var dt: Int? = null,
    @SerializedName("main") var main: Int? = null,
    @SerializedName("weather") var weather: ArrayList<Weather>? = arrayListOf(),
    @SerializedName("clouds") var clouds: Clouds? = Clouds(),
    @SerializedName("wind") var wind: Wind? = Wind(),
    @SerializedName("visibility") var visibility: Int? = null,
    @SerializedName("pop") var pop: Double? = null,
    @SerializedName("sys") var sys: Sys? = Sys(),
    @SerializedName("dt_txt") var dtTxt: String? = null,
)