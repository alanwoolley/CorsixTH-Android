/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth

class AsyncTaskResult<T>(val result: T? = null) {
    var error: Exception? = null

    constructor(error: Exception) : this(null){
        this.error = error
    }
}