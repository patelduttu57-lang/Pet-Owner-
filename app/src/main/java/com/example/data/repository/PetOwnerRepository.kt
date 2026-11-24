package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.OwnerFormInput
import com.example.data.model.PetFormInput
import com.example.data.model.ProfileFormValidator
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PetOwnerRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Authenticated user mismatch; refusing cross-user read." }
        val docPath = "users/$userId"
        val docRef = db.collection("users").document(userId)

        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docPath)
                    }
                    throw error
                }
        )
    }

    suspend fun registerUserProfile(
        owner: OwnerFormInput,
        pet: PetFormInput
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val errors = ProfileFormValidator.validateAll(owner, pet)
            require(!errors.hasErrors) { "Please fix the highlighted registration fields." }

            val docRef = db.collection("users").document(uid)
            docPath = docRef.path

            val payload = mapOf(
                "userId" to uid,
                "fullName" to owner.fullName.trim(),
                "email" to owner.email.trim(),
                "phoneNumber" to owner.phoneNumber.trim(),
                "address" to owner.address.trim(),
                "city" to owner.city.trim(),
                "state" to owner.state.trim(),
                "petDetails" to mapOf(
                    "petName" to pet.petName.trim(),
                    "petType" to pet.petType.label,
                    "petBreed" to pet.petBreed.trim(),
                    "petAge" to pet.petAge.trim(),
                    "petGender" to pet.petGender.label
                ),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateOwnerDetails(
        userId: String,
        owner: OwnerFormInput
    ): Result<Unit> {
        var docPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed; refusing cross-user update." }
            val errors = ProfileFormValidator.validateOwner(owner)
            require(!errors.hasErrors) { "Please fix the highlighted owner profile fields." }

            val docRef = db.collection("users").document(userId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "fullName" to owner.fullName.trim(),
                    "email" to owner.email.trim(),
                    "phoneNumber" to owner.phoneNumber.trim(),
                    "address" to owner.address.trim(),
                    "city" to owner.city.trim(),
                    "state" to owner.state.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updatePetDetails(
        userId: String,
        pet: PetFormInput
    ): Result<Unit> {
        var docPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed; refusing cross-user update." }
            val errors = ProfileFormValidator.validatePet(pet)
            require(!errors.hasErrors) { "Please fix the highlighted pet fields." }

            val docRef = db.collection("users").document(userId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "petDetails" to mapOf(
                        "petName" to pet.petName.trim(),
                        "petType" to pet.petType.label,
                        "petBreed" to pet.petBreed.trim(),
                        "petAge" to pet.petAge.trim(),
                        "petGender" to pet.petGender.label
                    ),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }
}
