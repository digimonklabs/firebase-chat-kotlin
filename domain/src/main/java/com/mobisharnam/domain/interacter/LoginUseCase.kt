package com.mobisharnam.domain.interacter

import android.content.Context
import com.mobisharnam.domain.repository.LocalRepository
import com.mobisharnam.domain.repository.RemoteRepository

class LoginUseCase(
    context: Context,
    private val remoteRepository: RemoteRepository,
    private val localRepository: LocalRepository
) : BaseUseCase(context,remoteRepository,localRepository) {
}