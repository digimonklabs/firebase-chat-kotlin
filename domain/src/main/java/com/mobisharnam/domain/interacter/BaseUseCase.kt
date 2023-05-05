package com.mobisharnam.domain.interacter

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.mobisharnam.domain.repository.LocalRepository
import com.mobisharnam.domain.repository.RemoteRepository

open class BaseUseCase(private val context: Context, private val remoteRepository: RemoteRepository, private val localeRepository: LocalRepository) {

    private var firebaseAuth: FirebaseAuth?= null
    private var dataBaseReferences: DatabaseReference?= null
    private var fireBaseInstance: FirebaseAuth?= null

    fun getFireBaseInstance() : FirebaseAuth {
        if (fireBaseInstance != null) {
            return fireBaseInstance!!
        }
        fireBaseInstance = FirebaseAuth.getInstance()
        return fireBaseInstance!!
    }

    fun getFireBaseAuth() : FirebaseAuth {
        if (firebaseAuth != null) {
            return firebaseAuth!!
        }
        firebaseAuth = Firebase.auth
        return firebaseAuth!!
    }

    fun getContext(): Context {
        return context
    }

    fun getDataBaseReference(): DatabaseReference {
        if (dataBaseReferences != null) {
            return dataBaseReferences!!
        }
        dataBaseReferences = FirebaseDatabase.getInstance().reference
        return dataBaseReferences!!
    }
}