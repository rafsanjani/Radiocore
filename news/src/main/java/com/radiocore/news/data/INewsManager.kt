package com.radiocore.news.data

interface INewsManager<T> {
    suspend fun fetchNews(): List<T>
}