package com.mobisharnam.domain.response

class Response<T> private constructor(val status: Status, val data: T?, val message: String?, val errorCode :Int?) {
    enum class Status {
        SUCCESS, ERROR, EXCEPTION
    }
    companion object {
        fun <T> success(data: T?, errorCode: Int?): Response<T> {
            return Response(
                Status.SUCCESS,
                data,
                null,
                 errorCode
            )
        }
        fun <T> error(message: String,errorCode: Int?): Response<T> {
            return Response(
                Status.ERROR,
                null,
                message,
                errorCode
            )
        }

        fun <T> exception(message: String,errorCode: Int?): Response<T> {
            return Response(
                Status.EXCEPTION,
                null,
                message,
                errorCode
            )
        }
    }
}