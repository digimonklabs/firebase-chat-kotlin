package com.mobisharnam.data.source.remote.settings

class Setting {

    companion object {
        var BASE_URL: String = "https://fcm.googleapis.com/fcm/"
        var IS_HEADER_REQUIRED: Boolean = true
        var SETOFFLINEDATAACCESS: Boolean = false
        var HEADER: HashMap<String, String>? = null
    }

}