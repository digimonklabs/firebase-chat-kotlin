package com.mobisharnam.data.source.remote.auth

import java.net.SocketTimeoutException
import okhttp3.Interceptor
import okhttp3.Response

class AuthenticationInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        try {
            if (request.header(RetrofitContents.PARAM_NO_AUTHENTICATION) == null) {
                request = request.newBuilder().build()
            }
        }catch (e: SocketTimeoutException) {
            e.printStackTrace()
        }

        return try {
            chain.proceed(request)
        }catch (e:java.lang.Exception){
            e.printStackTrace()
            chain.proceed(request)
        }
    }
}
