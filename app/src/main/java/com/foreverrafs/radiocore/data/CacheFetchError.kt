package com.foreverrafs.radiocore.data

import com.android.volley.VolleyError

/**
 * Created by Rafsanjani on 6/19/2019
 */
class CacheFetchError : VolleyError {

    constructor() : super()

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(cause: Throwable) : super(cause)
}
