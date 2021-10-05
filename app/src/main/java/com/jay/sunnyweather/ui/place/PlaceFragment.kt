package com.jay.sunnyweather.ui.place

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jay.sunnyweather.MainActivity
import com.jay.sunnyweather.R
import com.jay.sunnyweather.logic.model.Place
import com.jay.sunnyweather.ui.weather.WeatherActivity


class PlaceFragment : Fragment() {

    private lateinit var adapter: PlaceAdapter
    lateinit var placeViewModel: PlaceViewModel


    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearchPlace: EditText
    private lateinit var ivBackground: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_place, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        etSearchPlace = view.findViewById(R.id.etSearchPlace)
        ivBackground = view.findViewById(R.id.ivBackground)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        placeViewModel = ViewModelProvider(
            ViewModelStore(),
            PlaceViewModelFactory()
        ).get(PlaceViewModel::class.java)
        Log.d("Debug", "FragmentOnActivityCreated")

        if (activity is MainActivity && placeViewModel.isPlaceSaved()) {
            val place = placeViewModel.getSavedPlace()
            val intent = Intent(context, WeatherActivity::class.java).apply {
                putExtra("location_lng", place.location.lng)
                putExtra("location_lat", place.location.lat)
                putExtra("place_name", place.name)
            }
            startActivity(intent)
            activity?.finish()
        }

        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        adapter = PlaceAdapter(this, placeViewModel.placeList)
        recyclerView.adapter = adapter
        etSearchPlace.addTextChangedListener { editable ->
            val content = editable.toString()
            if (content.isNotEmpty()) {
                placeViewModel.searchPlaces(content)
            } else {
                recyclerView.visibility = View.GONE
                ivBackground.visibility = View.VISIBLE
                placeViewModel.placeList.clear()
                adapter.notifyDataSetChanged()
            }
        }
        placeViewModel.placeLiveData.observe(this, Observer { result ->
            val places = result.getOrNull()
            if (places != null) {
                recyclerView.visibility = View.VISIBLE
                ivBackground.visibility = View.GONE
                placeViewModel.placeList.clear()
                placeViewModel.placeList.addAll(places as Collection<Place>)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(activity, "未查询到该地点", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
    }
}