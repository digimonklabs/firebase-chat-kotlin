package com.mobisharnam.data.repository

import com.mobisharnam.data.source.remote.api.RestClient
import com.mobisharnam.domain.repository.RemoteRepository
import com.mobisharnam.domain.response.Response

class RemoteRepositoryImpl(private val restClient: RestClient) : RemoteRepository {

    override suspend fun <T> post(
        classDataObject: Class<T>,
        url: String,
        hashMap: String
    ): Response<T> {
        return restClient.post(classDataObject,url,hashMap)
    }

    override suspend fun <T> get(classDataObject: Class<T>, url: String): Response<T> {
       return restClient.get(classDataObject,url)
    }

}