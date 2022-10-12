package com.cs4530spring2022.project1.util

data class ItemsHikesRow(val image: Int, val name: String, val address: String){
    // used to store the coords of a location.
    var latitude: Double = 0.0
    var longitude: Double = 0.0
}