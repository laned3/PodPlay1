package com.lanedever.podplay.model

import java.util.*

// Data class to hold Episode
data class Episode (
    var guid: String = "",
    var title: String = "",
    var description: String = "",
    var mediaUrl: String = "",
    var mimeType: String = "",
    var releaseDate: Date = Date(),
    var duration: String = ""
)