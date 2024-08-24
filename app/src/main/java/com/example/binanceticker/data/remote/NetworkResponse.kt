package com.example.binanceticker.data.remote

sealed class NetworkResponse<out T> {
    data class Success<out T>(val data: T) : NetworkResponse<T>()
    data class Error(val errMessage: String, val errCode: Int? = null) : NetworkResponse<Nothing>()
}