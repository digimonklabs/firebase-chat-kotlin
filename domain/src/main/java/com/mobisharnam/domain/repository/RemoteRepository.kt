package com.mobisharnam.domain.repository

import com.mobisharnam.domain.response.Response

interface RemoteRepository {

    suspend fun <T> post(classDataObject: Class<T>, url:String, hashMap: String): Response<T>

    suspend fun <T> get(classDataObject: Class<T>, url:String): Response<T>

}