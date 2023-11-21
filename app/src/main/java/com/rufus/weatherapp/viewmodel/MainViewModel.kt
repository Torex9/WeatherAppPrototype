package com.rufus.weatherapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rufus.weatherapp.model.MyLatLng
import com.rufus.weatherapp.model.forecast.ForecastResult
import com.rufus.weatherapp.model.weather.WeatherResult
import com.rufus.weatherapp.network.RetrofitClient
import kotlinx.coroutines.launch
import java.lang.Exception

enum class STATE {
    LOADING,
    SUCCESS,
    FAILED
}
class MainViewModel : ViewModel() {
    //control state of view model
    var state by mutableStateOf(STATE.LOADING)
    //Hold value from API for weather info
    var weatherResponse : WeatherResult by mutableStateOf(WeatherResult())
    //Hold value from API for forecast info
    var forecastResponse : ForecastResult by mutableStateOf(ForecastResult())
    var errorMessage: String by mutableStateOf("")

    fun getWeatherByLocation(latLng: MyLatLng) {
        viewModelScope.launch {
            state = STATE.LOADING
            val apiService = RetrofitClient.getInstace()
            try{
                val apiResponse = apiService.getWeather(latLng.lat, latLng.lng)
                weatherResponse = apiResponse //Update state
                state = STATE.SUCCESS
            }catch (e: Exception) {
                errorMessage = e.message!!.toString()
                state = STATE.FAILED
            }
        }
    }

    fun getForecastByLocation(latLng: MyLatLng) {
        viewModelScope.launch {
            state = STATE.LOADING
            val apiService = RetrofitClient.getInstace()
            try{
                val apiResponse = apiService.getForecast(latLng.lat, latLng.lng)
                forecastResponse = apiResponse //Update state
                state = STATE.SUCCESS
            }catch (e: Exception) {
                errorMessage = e.message!!.toString()
                state = STATE.FAILED
            }
        }
    }
}