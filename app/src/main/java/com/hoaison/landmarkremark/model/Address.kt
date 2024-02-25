package com.hoaison.landmarkremark.model

import java.io.Serializable

data class Address(
    var id: String? = null,
    var title: String? = null,
    var description: String? = null,
    var longitude: Double? = null,
    var latitude: Double? = null,
    var images: List<String>? = null,
    var polygons: List<List<Double>>? = null
) : Serializable