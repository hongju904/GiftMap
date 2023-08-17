package com.example.giftmap

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Query

class MapActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var databaseRef = FirebaseDatabase.getInstance().getReference("user_data/$uid")
    private val storeSet = HashSet<String>()
    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK 5d08621c2d5119da4b81a9661dac4f92" // REST API 키
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        ActivityCompat.requestPermissions(this, arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION), 1)

        mapView = MapView(this)
        val mapViewContainer = findViewById<ViewGroup>(R.id.map_view)
        mapViewContainer.addView(mapView)

        mapView.setCurrentLocationTrackingMode(
            MapView.CurrentLocationTrackingMode.
            TrackingModeOnWithoutHeading)
        mapView.setCustomCurrentLocationMarkerTrackingImage(R.drawable.flags2, MapPOIItem.ImageOffset(30, 30))

        val myLocation: FloatingActionButton = findViewById(R.id.locationbtn)
        myLocation.setOnClickListener {
            // 현재 위치 가져오기
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationProvider = LocationManager.NETWORK_PROVIDER
            mapView.removeAllPOIItems()
            try {
                val lastKnownLocation = locationManager.getLastKnownLocation(locationProvider)
                lastKnownLocation?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    moveMapToCurrentLocation(latitude, longitude)

                    val marker = MapPOIItem()
                    marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                    marker.markerType = MapPOIItem.MarkerType.CustomImage
                    marker.customImageResourceId = R.drawable.flags2
                    marker.setCustomImageAnchor(0.5f, 1.0f)
                    mapView.addPOIItem(marker)
                }

            } catch (e: SecurityException) {
                e.printStackTrace()
            }
            fetchDataFromDatabaseAndSearchKeyword()

        }


        val intent = Intent(this, ManageActivity::class.java)
        val menubtn: FloatingActionButton = findViewById(R.id.menubtn)
        menubtn.setOnClickListener { startActivity(intent) }
    }

    private fun searchKeyword(keyword: String, latitude: Double, longitude: Double) {
        val retrofit = Retrofit.Builder() // Retrofit 구성
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java)

        val call = api.getSearchKeyword(API_KEY, keyword, latitude, longitude)

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
                        val marker = MapPOIItem()
                        marker.itemName = place.place_name
                        marker.mapPoint =
                            MapPoint.mapPointWithGeoCoord(place.y.toDouble(), place.x.toDouble())
                        marker.markerType = MapPOIItem.MarkerType.BluePin
                        marker.setShowDisclosureButtonOnCalloutBalloon(false)
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

    private fun moveMapToCurrentLocation(latitude: Double, longitude: Double) {
        val mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
        mapView.setMapCenterPoint(mapPoint, true) // 애니메이션 효과를 포함하여 지도 이동
        for (store in storeSet) {
            searchKeyword(store, latitude, longitude)
        }
    }

    private fun fetchDataFromDatabaseAndSearchKeyword() {
        databaseRef = FirebaseDatabase.getInstance().getReference("user_data/$uid")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    val store = childSnapshot.child("store").value?.toString()
                    store?.let {
                        storeSet.add(store) // HashSet에 추가하여 중복 제거
                    }
                }
                Log.d("Test", "Body: ${storeSet}")
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // 데이터 검색 실패 처리
            }
        })
    }


}