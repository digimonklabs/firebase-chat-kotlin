package com.mobisharnam.domain.interacter

import android.content.Context
import com.mobisharnam.domain.repository.LocalRepository
import com.mobisharnam.domain.repository.RemoteRepository

class RegisterUseCase(
    private val context: Context,
    private val remoteRepository: RemoteRepository,
    private val localeRepository: LocalRepository
) : BaseUseCase(context, remoteRepository, localeRepository) {


}