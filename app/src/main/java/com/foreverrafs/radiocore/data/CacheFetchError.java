package com.foreverrafs.radiocore.data;

import com.android.volley.VolleyError;

/**
 * Created by Rafsanjani on 6/19/2019
 */
public class CacheFetchError extends VolleyError {

    public CacheFetchError() {
        super();
    }

    public CacheFetchError(String message) {
        super(message);
    }

    public CacheFetchError(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheFetchError(Throwable cause) {
        super(cause);
    }
}
