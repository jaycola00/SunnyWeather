package com.jay.sunnyweather.logic

import androidx.lifecycle.liveData
import com.jay.sunnyweather.logic.dao.PlaceDao
import com.jay.sunnyweather.logic.model.Location
import com.jay.sunnyweather.logic.model.Place
import com.jay.sunnyweather.logic.model.Weather
import com.jay.sunnyweather.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.*
import java.lang.RuntimeException
import kotlin.Exception

object Repository {

    fun searchPlaces(query: String) = liveData() {
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "ok") {
                val places = placeResponse.places
                Result.success(places)
            } else {
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure<Place>(e)
        }
        emit(result)
    }

    fun refreshWeather(lng: String, lat: String) = liveData() {
        val result = try {
            val realtimeResponse = SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            val dailyResponse = SunnyWeatherNetwork.getDailyWeather(lng, lat)
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                Result.success(weather)
            } else {
                Result.failure(RuntimeException("realtime response is ${realtimeResponse.status}, daily response is ${dailyResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure<Weather>(e)
        }
        emit(result)
    }

    fun savePlace(place: Place) {
        CoroutineScope(Dispatchers.IO).launch {
            PlaceDao().savePlace(place)
        }
    }

    fun getSavedPlace()= PlaceDao().getSavedPlace()

    fun isPlaceSaved()=PlaceDao().isPlaceSaved()

}
