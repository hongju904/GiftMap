package com.example.giftmap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import net.daum.android.map.MapViewEventListener
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK 5d08621c2d5119da4b81a9661dac4f92" // REST API 키
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION), 1)

        mapView = MapView(this)
        val mapViewContainer = findViewById<ViewGroup>(R.id.map_view)
        mapViewContainer.addView(mapView)

//        mapView.setMapViewEventListener(this)

        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading)
        mapView.setCustomCurrentLocationMarkerTrackingImage(R.drawable.flags2, MapPOIItem.ImageOffset(30, 30))

        val intent = Intent(this, ManageActivity::class.java)
        var menubtn: FloatingActionButton = findViewById(R.id.menubtn)
        menubtn.setOnClickListener { startActivity(intent) }

        searchKeyword("교원")
        searchKeyword("은행")
    }

    private fun searchKeyword(keyword: String) {
        val retrofit = Retrofit.Builder() // Retrofit 구성
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java)
        val call = api.getSearchKeyword(API_KEY, keyword)

        // API 서버에 요청
        call.enqueue(object : Callback<ResultSearchKeyword> {
            override fun onResponse(
                call: Call<ResultSearchKeyword>,
                response: Response<ResultSearchKeyword>
            ) {
                // 통신 성공. 검색 결과는 response.body()에 담김
                Log.d("Test", "Body: ${response.body()}")
                val places = response.body()?.documents

                if (places != null) {
                    for (place in places) {
//                        val place = places[0]
//                        Log.d("Test", "Body: ${place}")
                        val marker = MapPOIItem()
                        marker.itemName = place.place_name
                        marker.mapPoint =
                            MapPoint.mapPointWithGeoCoord(place.y.toDouble(), place.x.toDouble())
                        marker.markerType = MapPOIItem.MarkerType.BluePin
                        mapView.addPOIItem(marker)
                    }
                }
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                // 통신 실패
                Log.w("MainActivity", "통신 실패: ${t.message}")
            }
        })

    }
}