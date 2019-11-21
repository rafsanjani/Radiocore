package com.foreverrafs.radiocore.data

interface INewsManager<T> {
    suspend fun fetchNews(): List<T>
}