package com.example.ui.state

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.toFriendlyFirestoreMessage
import com.example.data.model.OwnerFormInput
import com.example.data.model.PetFormInput
import com.example.data.model.UserProfile
import com.example.data.repository.PetOwnerRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Ready(val profile: UserProfile?) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

sealed interface MutationState {
    data object Idle : MutationState
    data object Saving : MutationState
    data class Success(val message: String) : MutationState
    data class Error(val message: String) : MutationState
}

class DashboardViewModel(
    private val repository: PetOwnerRepository,
    private val currentUserId: String
) : ViewModel() {

    val profileState: StateFlow<ProfileUiState> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, ProfileUiState> { profile -> ProfileUiState.Ready(profile) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing user profile", rootError)
            emit(ProfileUiState.Error(rootError.toFriendlyFirestoreMessage()))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ProfileUiState.Loading
        )

    private val _mutationState = MutableStateFlow<MutationState>(MutationState.Idle)
    val mutationState: StateFlow<MutationState> = _mutationState.asStateFlow()

    fun registerProfile(
        owner: OwnerFormInput,
        pet: PetFormInput,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _mutationState.value = MutationState.Saving
            val result = repository.registerUserProfile(owner, pet)
            result.fold(
                onSuccess = {
                    _mutationState.value = MutationState.Success("Registration saved to PawCare!")
                    onComplete()
                },
                onFailure = { err ->
                    _mutationState.value = MutationState.Error(err.toFriendlyFirestoreMessage())
                }
            )
        }
    }

    fun updateOwnerProfile(owner: OwnerFormInput, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _mutationState.value = MutationState.Saving
            val result = repository.updateOwnerDetails(currentUserId, owner)
            result.fold(
                onSuccess = {
                    _mutationState.value = MutationState.Success("Owner profile updated.")
                    onComplete()
                },
                onFailure = { err ->
                    _mutationState.value = MutationState.Error(err.toFriendlyFirestoreMessage())
                }
            )
        }
    }

    fun updatePetDetails(pet: PetFormInput, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _mutationState.value = MutationState.Saving
            val result = repository.updatePetDetails(currentUserId, pet)
            result.fold(
                onSuccess = {
                    _mutationState.value = MutationState.Success("Pet details updated.")
                    onComplete()
                },
                onFailure = { err ->
                    _mutationState.value = MutationState.Error(err.toFriendlyFirestoreMessage())
                }
            )
        }
    }

    fun clearMutationState() {
        _mutationState.value = MutationState.Idle
    }

    private companion object {
        const val TAG = "DashboardVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
