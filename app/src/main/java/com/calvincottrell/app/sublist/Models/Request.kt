package com.calvincottrell.app.sublist.Models

class Request (
    val address: String,
    val contractorEmail: String,
    val contractorName: String,
    val contractorNumber: String,
    val email: String,
    val name: String,
    val phoneNumber: String,
    val projectDetails: String
) {

    constructor(): this("", "", "","","","","","")
}