package com.mobisharnam.domain.interacter

import android.content.Context
import android.util.Log
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.values
import com.google.firebase.ktx.Firebase
import com.mobisharnam.domain.model.Invitation
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.repository.LocalRepository
import com.mobisharnam.domain.repository.RemoteRepository
import com.mobisharnam.domain.response.Response
import com.mobisharnam.domain.util.AppConstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class InvitationUseCase(
    context: Context,
    private val remoteRepository: RemoteRepository,
    private val localRepository: LocalRepository
): BaseUseCase(context,remoteRepository,localRepository) {

    fun getInvitation(): Flow<Response<ArrayList<Invitation>>> = flow {
        val ref = getDataBaseReference().child(AppConstant.USER_TABLE)
        try {
            val snapshot = ref.get().await()
            val userList = ArrayList<Invitation>()

            for (childSnapshot in snapshot.children) {
                try {
                    childSnapshot.getValue(Invitation::class.java)?.let {
                        userList.add(it)
                    }
                }catch (e: Exception) {
                    emit(Response.exception(e.message.toString(),101))
                }
            }
            userList.let {
                emit(Response.success(it,0))
            }
        }catch (e: Exception) {
            emit(Response.exception(e.message.toString(),201))
        }
    }.flowOn(Dispatchers.IO)
}