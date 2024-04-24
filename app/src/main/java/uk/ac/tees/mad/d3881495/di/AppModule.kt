package uk.ac.tees.mad.d3881495.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uk.ac.tees.mad.d3881495.data.repository.AuthRepository
import uk.ac.tees.mad.d3881495.data.repository.AuthRepositoryImpl
import uk.ac.tees.mad.d3881495.data.repository.DatabaseRepository
import uk.ac.tees.mad.d3881495.data.repository.FirestoreDatabaseRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providesFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun providesFirestore() = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun providesStorage() = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun providesRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository =
        AuthRepositoryImpl(firebaseAuth, firestore)

    @Provides
    @Singleton
    fun providesFiresotreRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): FirestoreDatabaseRepository =
        DatabaseRepository(firestore, storage)


}