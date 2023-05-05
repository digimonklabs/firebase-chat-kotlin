package com.firebase.chat.koin

import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.ui.viewmodel.ChatDetailViewModel
import com.firebase.chat.ui.viewmodel.ChatListViewModel
import com.firebase.chat.ui.viewmodel.ForgotPasswordViewModel
import com.firebase.chat.ui.viewmodel.LoginViewModel
import com.firebase.chat.ui.viewmodel.RegisterViewModel
import com.mobisharnam.data.repository.LocalRepositoryImpl
import com.mobisharnam.data.repository.RemoteRepositoryImpl
import com.mobisharnam.data.source.remote.api.RestClient
import com.mobisharnam.domain.interacter.BaseUseCase
import com.mobisharnam.domain.interacter.ChatDetailUseCase
import com.mobisharnam.domain.interacter.ChatListUseCase
import com.mobisharnam.domain.interacter.ForgotPasswordUseCase
import com.mobisharnam.domain.interacter.LoginUseCase
import com.mobisharnam.domain.interacter.RegisterUseCase
import com.mobisharnam.domain.repository.LocalRepository
import com.mobisharnam.domain.repository.RemoteRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { BaseViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { ForgotPasswordViewModel(get()) }
    viewModel { ChatListViewModel(get()) }
    viewModel { ChatDetailViewModel(get()) }
}
val useCaseModule = module {
    factory { BaseUseCase(get(), get(), get()) }
    factory { LoginUseCase(get(), get(), get()) }
    factory { RegisterUseCase(get(), get(), get()) }
    factory { ForgotPasswordUseCase(get(), get(), get()) }
    factory { ChatListUseCase(get(), get(), get()) }
    factory { ChatDetailUseCase(get(), get(), get()) }
}
val repositoryModule = module {
    factory<RemoteRepository> { RemoteRepositoryImpl(get()) }
    factory<LocalRepository> { LocalRepositoryImpl(get()) }
}
val restClientModule = module {
    single { RestClient(get()) }
}