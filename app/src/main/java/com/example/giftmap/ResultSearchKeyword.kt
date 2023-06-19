package com.example.giftmap

data class ResultSearchKeyword(
    var documents: List<Place> // 검색 결과
)

data class Place(
    var store_name: String,
    var address: String, // 지번 주소
    var road_address: String, // 도로명 주소
    var x: String,
    var y: String,
)