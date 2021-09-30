package com.moaapps.prayertimesdemo.utils

class Resource<T>(val status: Status, val data:T?, val message:String) {
    companion object {
        fun<T> loading():Resource<T> {
            return Resource(Status.LOADING, null, "")
        }

        fun<T> error(message:String = ""):Resource<T> {
            return Resource(Status.FAIL, null, message)
        }

        fun<T> success(data:T, message:String = ""):Resource<T> {
            return Resource(Status.LOADING, data, message)
        }
    }
}