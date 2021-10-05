package com.jay.sunnyweather.ui.weather

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jay.sunnyweather.R
import com.jay.sunnyweather.logic.model.Weather
import com.jay.sunnyweather.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {

    lateinit var weatherViewModel: WeatherViewModel
    private lateinit var tvPlaceNameWeather: TextView
    private lateinit var tvCurrentTemp: TextView
    private lateinit var tvCurrentSky: TextView
    private lateinit var tvCurrentAQI: TextView
    private lateinit var layoutNow: RelativeLayout
    private lateinit var layoutForecast: LinearLayout
    private lateinit var tvColdRisk: TextView
    private lateinit var tvDressing: TextView
    private lateinit var tvUltraviolet: TextView
    private lateinit var tvCarWashing: TextView
    private lateinit var layoutWeather: ScrollView
    private lateinit var layoutSwipeRefresh: SwipeRefreshLayout
    private lateinit var btnNav: Button
    private lateinit var layoutDrawer: DrawerLayout


    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        init()

        weatherViewModel = ViewModelProvider(
            ViewModelStore(),
            WeatherViewModelFactory()
        ).get(WeatherViewModel::class.java)

        if (weatherViewModel.locationLng.isEmpty()) {
            weatherViewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (weatherViewModel.locationLat.isEmpty()) {
            weatherViewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (weatherViewModel.placeName.isEmpty()) {
            weatherViewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        weatherViewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            layoutSwipeRefresh.isRefreshing = false
        })
        layoutSwipeRefresh.setColorSchemeColors(R.color.teal_200)
        refreshWeather()
        layoutSwipeRefresh.setOnRefreshListener {
            refreshWeather()
        }

        btnNav.setOnClickListener {
            layoutDrawer.openDrawer(GravityCompat.START)
        }
        layoutDrawer.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                val manger = getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager
                manger.hideSoftInputFromWindow(
                    drawerView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        })
    }

    private fun showWeatherInfo(weather: Weather) {
        tvPlaceNameWeather.text = weatherViewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        //填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()}℃"
        tvCurrentTemp.text = currentTempText
        tvCurrentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        tvCurrentAQI.text = currentPM25Text
        layoutNow.setBackgroundResource(getSky(realtime.skycon).bg)
        //填充forecast.xml布局中的数据
        layoutForecast.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view =
                LayoutInflater.from(this).inflate(R.layout.forecast_item, layoutForecast, false)
            val dateInfo = view.findViewById<TextView>(R.id.tvDataInfo)
            val skyIcon = view.findViewById<ImageView>(R.id.ivSkyIcon)
            val skyInfo = view.findViewById<TextView>(R.id.tvSkyInfo)
            val temperatureInfo = view.findViewById<TextView>(R.id.tvTemperatureInfo)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()}~${temperature.max.toInt()}℃"
            temperatureInfo.text = tempText
            layoutForecast.addView(view)
        }
        //填充life_index.xml布局的数据
        val lifeIndex = daily.lifeIndex
        tvColdRisk.text = lifeIndex.coldRisk[0].desc
        tvDressing.text = lifeIndex.dressing[0].desc
        tvUltraviolet.text = lifeIndex.ultraviolet[0].desc
        tvCarWashing.text = lifeIndex.carWashing[0].desc
        layoutWeather.visibility = View.VISIBLE
    }

    private fun init() {
        tvPlaceNameWeather = findViewById(R.id.tvPlaceNameWeather)
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp)
        tvCurrentSky = findViewById(R.id.tvCurrentSky)
        tvCurrentAQI = findViewById(R.id.tvCurrentAQI)
        layoutNow = findViewById(R.id.layoutNow)
        layoutForecast = findViewById(R.id.layoutForecast)
        tvColdRisk = findViewById(R.id.tvColdRisk)
        tvDressing = findViewById(R.id.tvDressing)
        tvUltraviolet = findViewById(R.id.tvUltraviolet)
        tvCarWashing = findViewById(R.id.tvCarWashing)
        layoutWeather = findViewById(R.id.layoutWeather)
        layoutSwipeRefresh = findViewById(R.id.layoutSwipeRefresh)
        btnNav = findViewById(R.id.btnNav)
        layoutDrawer = findViewById(R.id.layoutDrawer)
    }

    fun refreshWeather() {
        weatherViewModel.refreshWeather(weatherViewModel.locationLng, weatherViewModel.locationLat)
        layoutSwipeRefresh.isRefreshing = true
    }
}