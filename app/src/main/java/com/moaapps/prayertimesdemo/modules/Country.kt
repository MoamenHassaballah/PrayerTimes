package com.moaapps.prayertimesdemo.modules

data class Country(
    val id: Int, val name: String, val native: String, val emoji: String,
    val states: List<State>
)
