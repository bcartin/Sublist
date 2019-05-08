package com.calvincottrell.app.sublist.Models

import java.io.Serializable

class Contractor(
    var id: String,
    val contact: String,
    val email: String,
    val features: ArrayList<String>,
    val location: String,
    val name: String,
    val number: String,
    val radius: Int,
    var stars: Int,
    val type: String,
    val reviews: ArrayList<Review>,
    isFavorite: Boolean

): Serializable {

    constructor() : this("", "", "", ArrayList<String>(),"", "", "", 0,0,"", ArrayList<Review>(), false)

    var isFavorite: Boolean = isFavorite
    set(value) { field = value }

    var numberOfReviews: Int = 0
        get() = reviews.size

}