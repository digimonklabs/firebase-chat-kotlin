package com.mobisharnam.data.source.remote.api

import android.accounts.NetworkErrorException
import android.content.Context
import com.mobisharnam.data.R
import com.mobisharnam.domain.response.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLException

object HandleException {

    fun <T> getMessage(e: Exception, context: Context): Response<T> {
        when (e) {
            is SocketTimeoutException -> {
                return Response.exception(e.message.toString(), 10)
            }
            is SSLException -> {
                return Response.exception(e.message.toString(),10)
            }
            is UnknownHostException -> {
                return Response.exception(context.getString(R.string.alert_no_internet), 1020)
            }
            is ConnectException -> {
                return Response.exception(context.getString(R.string.alert_no_internet), 0)
            }
            is TimeoutException -> {
                return Response.exception(context.getString(R.string.alert_connection_time_out), 11)
            }
            is ParseException -> {
                return Response.exception(context.getString(R.string.alert_parsing_error), 12)
            }
            is NetworkErrorException -> {
                return Response.exception(context.getString(R.string.alert_no_internet), 0)
            }
            else -> {
                return Response.exception(e.message.toString(), 101)
            }
        }
    }
}