package com.calvincottrell.app.sublist.Models

class Subcontractors(
    val name: String,
    val contractors: ArrayList<Contractor>,
    var isCollapsed: Boolean
) {
    constructor() : this("", ArrayList<Contractor>(), false)


}