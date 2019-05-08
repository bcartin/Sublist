package com.calvincottrell.app.sublist.Models

class User (
    val email: String,
    val name: String,
    val number: String,
    val company: String,
    var allowedInSublist: Boolean
) {

    constructor(): this("", "", "","", false)
}