package com.moaapps.prayertimesdemo.modules

data class State(
    val id: Int, val name: String,
    val state_code: String,
    val latitude: String,
    val longitude: String,
    val cities: List<City>
)
