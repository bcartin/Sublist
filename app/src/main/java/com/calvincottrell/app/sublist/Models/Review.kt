package com.calvincottrell.app.sublist.Models

import java.io.Serializable

class Review (
    val reviewer: String,
    val stars: Int,
    val workmanship: Int,
    val onBudget: Int,
    val onSchedule: Int,
    val communication: Int,
    val date: String,
    val feature: String,
    val address: String,
    val details: String
): Serializable {

    constructor() : this("", 0, 0, 0, 0, 0, "", "", "", "")
}