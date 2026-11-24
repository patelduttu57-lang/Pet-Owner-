package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.auth.authStateFlow
import com.example.auth.signOut
import com.example.data.model.OwnerFormInput
import com.example.data.model.PetFormInput
import com.example.data.repository.PetOwnerRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PublicHomepageScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.state.DashboardViewModel
import com.example.ui.state.MutationState
import com.example.ui.state.ProfileUiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

enum class AppDestination {
    HOME,
    LOGIN,
    REGISTER,
    DASHBOARD
}

private data class PendingRegistrationData(
    val owner: OwnerFormInput,
    val pet: PetFormInput
)

@Composable
fun PawCareApp() {
    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    // Public Homepage is ALWAYS the initial screen on launch
    var destination by remember { mutableStateOf(AppDestination.HOME) }
    var pendingRegistration by remember { mutableStateOf<PendingRegistrationData?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val performLogout: () -> Unit = {
        pendingRegistration = null
        signOut(credentialManager, scope) {
            destination = AppDestination.HOME
        }
    }

    val user = currentUser
    if (user == null) {
        // Unauthenticated visitors: strictly NO Firestore queries or DashboardViewModel composed.
        when (destination) {
            AppDestination.HOME, AppDestination.DASHBOARD -> {
                PublicHomepageScreen(
                    isAuthenticated = false,
                    onNavigateToLogin = { destination = AppDestination.LOGIN },
                    onNavigateToRegister = { destination = AppDestination.REGISTER },
                    onNavigateToDashboard = { destination = AppDestination.LOGIN },
                    onLogoutClick = performLogout
                )
            }
            AppDestination.LOGIN -> {
                LoginScreen(
                    onBackToHome = { destination = AppDestination.HOME },
                    onNavigateToRegister = { destination = AppDestination.REGISTER },
                    onLoginSuccess = { destination = AppDestination.DASHBOARD }
                )
            }
            AppDestination.REGISTER -> {
                RegisterScreen(
                    isAlreadySignedIn = false,
                    onBackToHome = { destination = AppDestination.HOME },
                    onNavigateToLogin = { destination = AppDestination.LOGIN },
                    onSubmitWithSignedInUser = { ownerInput, petInput ->
                        pendingRegistration = PendingRegistrationData(ownerInput, petInput)
                        destination = AppDestination.DASHBOARD
                    }
                )
            }
        }
    } else {
        // Authenticated session: key by uid so account switches cleanly reset ViewModel & listeners
        key(user.uid) {
            AuthenticatedPawCareSession(
                user = user,
                destination = destination,
                pendingRegistration = pendingRegistration,
                onClearPendingRegistration = { pendingRegistration = null },
                onNavigate = { destination = it },
                onLogoutClick = performLogout
            )
        }
    }
}

@Composable
private fun AuthenticatedPawCareSession(
    user: FirebaseUser,
    destination: AppDestination,
    pendingRegistration: PendingRegistrationData?,
    onClearPendingRegistration: () -> Unit,
    onNavigate: (AppDestination) -> Unit,
    onLogoutClick: () -> Unit
) {
    val viewModel: DashboardViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                DashboardViewModel(
                    repository = PetOwnerRepository(db),
                    currentUserId = user.uid
                )
            }
        }
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val mutationState by viewModel.mutationState.collectAsStateWithLifecycle()

    // If the user just completed the Registration form and authenticated with Google, persist to Firestore immediately
    LaunchedEffect(pendingRegistration, user.uid) {
        val pending = pendingRegistration ?: return@LaunchedEffect
        viewModel.registerProfile(pending.owner, pending.pet) {
            onClearPendingRegistration()
            onNavigate(AppDestination.DASHBOARD)
        }
    }

    when (destination) {
        AppDestination.HOME -> {
            PublicHomepageScreen(
                isAuthenticated = true,
                onNavigateToLogin = { onNavigate(AppDestination.DASHBOARD) },
                onNavigateToRegister = { onNavigate(AppDestination.DASHBOARD) },
                onNavigateToDashboard = { onNavigate(AppDestination.DASHBOARD) },
                onLogoutClick = onLogoutClick
            )
        }
        AppDestination.LOGIN, AppDestination.REGISTER, AppDestination.DASHBOARD -> {
            when (val state = profileState) {
                is ProfileUiState.Loading -> {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Loading your PawCare profile...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                is ProfileUiState.Error -> {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(
                            modifier = Modifier.padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.widthIn(max = 460.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Unable to Load Profile",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(onClick = { onNavigate(AppDestination.HOME) }) {
                                    Text("Return to Homepage")
                                }
                                OutlinedButton(onClick = onLogoutClick) {
                                    Text("Logout")
                                }
                            }
                        }
                    }
                }
                is ProfileUiState.Ready -> {
                    val profile = state.profile
                    if (profile == null) {
                        // Google account authenticated, but owner/pet registration not yet saved in Firestore
                        RegisterScreen(
                            isAlreadySignedIn = true,
                            initialFullName = user.displayName.orEmpty(),
                            initialEmail = user.email.orEmpty(),
                            isSaving = mutationState is MutationState.Saving,
                            externalError = (mutationState as? MutationState.Error)?.message,
                            onBackToHome = { onNavigate(AppDestination.HOME) },
                            onNavigateToLogin = { onNavigate(AppDestination.LOGIN) },
                            onSubmitWithSignedInUser = { ownerInput, petInput ->
                                viewModel.registerProfile(ownerInput, petInput) {
                                    onNavigate(AppDestination.DASHBOARD)
                                }
                            },
                            onLogoutClick = onLogoutClick
                        )
                    } else {
                        DashboardScreen(
                            profile = profile,
                            mutationState = mutationState,
                            onBackToHome = { onNavigate(AppDestination.HOME) },
                            onUpdateOwner = { owner, done ->
                                viewModel.updateOwnerProfile(owner, done)
                            },
                            onUpdatePet = { pet, done ->
                                viewModel.updatePetDetails(pet, done)
                            },
                            onClearMutationState = { viewModel.clearMutationState() },
                            onLogoutClick = onLogoutClick
                        )
                    }
                }
            }
        }
    }
}
