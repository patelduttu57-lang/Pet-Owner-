package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.auth.onGoogleSignInClicked
import com.example.data.model.FormFieldErrors
import com.example.data.model.OwnerFormInput
import com.example.data.model.PetFormInput
import com.example.data.model.PetGender
import com.example.data.model.PetType
import com.example.data.model.ProfileFormValidator
import com.example.ui.components.GoogleSignInActionButton
import com.example.ui.components.PawCareLogoBadge
import com.example.ui.components.PetGenderChipSelector
import com.example.ui.components.PetTypeChipSelector
import com.example.ui.components.SectionHeaderTag
import com.example.ui.components.ValidatedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    isAlreadySignedIn: Boolean,
    initialFullName: String = "",
    initialEmail: String = "",
    isSaving: Boolean = false,
    externalError: String? = null,
    onBackToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onSubmitWithSignedInUser: (OwnerFormInput, PetFormInput) -> Unit,
    onLogoutClick: (() -> Unit)? = null
) {
    BackHandler(onBack = onBackToHome)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var fullName by rememberSaveable(initialFullName) { mutableStateOf(initialFullName) }
    var email by rememberSaveable(initialEmail) { mutableStateOf(initialEmail) }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var state by rememberSaveable { mutableStateOf("") }

    var petName by rememberSaveable { mutableStateOf("") }
    var petType by remember { mutableStateOf(PetType.DOG) }
    var petBreed by rememberSaveable { mutableStateOf("") }
    var petAge by rememberSaveable { mutableStateOf("") }
    var petGender by remember { mutableStateOf(PetGender.MALE) }

    var fieldErrors by remember { mutableStateOf(FormFieldErrors()) }
    var authLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    val combinedLoading = isSaving || authLoading
    val displayedBannerError = externalError ?: authError

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    PawCareLogoBadge(
                        subtitle = if (isAlreadySignedIn) "Complete Profile" else "Owner Registration"
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToHome,
                        modifier = Modifier.testTag("register_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to homepage"
                        )
                    }
                },
                actions = {
                    if (isAlreadySignedIn && onLogoutClick != null) {
                        TextButton(onClick = onLogoutClick) {
                            Text("Logout")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionHeaderTag(
                    label = if (isAlreadySignedIn) "Step 2: Finish Your Pet Profile" else "New Member Registration",
                    isOrange = true
                )

                Text(
                    text = if (isAlreadySignedIn) {
                        "Almost Done — Add Your Pet & Contact Info"
                    } else {
                        "Register as a PawCare Pet Owner"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Enter your contact details and companion pet information. Your record is encrypted in transit and accessible only to you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AnimatedVisibility(visible = displayedBannerError != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = displayedBannerError.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                // Owner Details Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Owner Personal & Contact Details",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        ValidatedTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                fieldErrors = fieldErrors.copy(fullName = null)
                            },
                            label = "Full Name",
                            placeholder = "Jordan Taylor",
                            leadingIcon = Icons.Filled.Person,
                            errorText = fieldErrors.fullName,
                            testTag = "reg_full_name"
                        )

                        ValidatedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                fieldErrors = fieldErrors.copy(email = null)
                            },
                            label = "Email Address",
                            placeholder = "jordan@example.com",
                            leadingIcon = Icons.Filled.Email,
                            errorText = fieldErrors.email,
                            keyboardType = KeyboardType.Email,
                            testTag = "reg_email"
                        )

                        ValidatedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                phoneNumber = it
                                fieldErrors = fieldErrors.copy(phoneNumber = null)
                            },
                            label = "Phone Number",
                            placeholder = "+1 503-555-0188",
                            leadingIcon = Icons.Filled.Phone,
                            errorText = fieldErrors.phoneNumber,
                            keyboardType = KeyboardType.Phone,
                            testTag = "reg_phone"
                        )

                        ValidatedTextField(
                            value = address,
                            onValueChange = {
                                address = it
                                fieldErrors = fieldErrors.copy(address = null)
                            },
                            label = "Residential Address",
                            placeholder = "742 Evergreen Terrace",
                            leadingIcon = Icons.Filled.Home,
                            errorText = fieldErrors.address,
                            testTag = "reg_address"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ValidatedTextField(
                                value = city,
                                onValueChange = {
                                    city = it
                                    fieldErrors = fieldErrors.copy(city = null)
                                },
                                label = "City",
                                placeholder = "Portland",
                                leadingIcon = Icons.Filled.LocationCity,
                                errorText = fieldErrors.city,
                                testTag = "reg_city",
                                modifier = Modifier.weight(1f)
                            )

                            ValidatedTextField(
                                value = state,
                                onValueChange = {
                                    state = it
                                    fieldErrors = fieldErrors.copy(state = null)
                                },
                                label = "State",
                                placeholder = "Oregon",
                                leadingIcon = Icons.Filled.Map,
                                errorText = fieldErrors.state,
                                testTag = "reg_state",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Pet Details Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Pet Companion Details",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        ValidatedTextField(
                            value = petName,
                            onValueChange = {
                                petName = it
                                fieldErrors = fieldErrors.copy(petName = null)
                            },
                            label = "Pet Name",
                            placeholder = "Barnaby",
                            leadingIcon = Icons.Filled.Pets,
                            errorText = fieldErrors.petName,
                            testTag = "reg_pet_name"
                        )

                        PetTypeChipSelector(
                            selectedType = petType,
                            onTypeSelected = { petType = it }
                        )

                        ValidatedTextField(
                            value = petBreed,
                            onValueChange = {
                                petBreed = it
                                fieldErrors = fieldErrors.copy(petBreed = null)
                            },
                            label = "Pet Breed",
                            placeholder = "Golden Retriever",
                            leadingIcon = Icons.Filled.Pets,
                            errorText = fieldErrors.petBreed,
                            testTag = "reg_pet_breed"
                        )

                        ValidatedTextField(
                            value = petAge,
                            onValueChange = {
                                petAge = it
                                fieldErrors = fieldErrors.copy(petAge = null)
                            },
                            label = "Pet Age",
                            placeholder = "3 years",
                            leadingIcon = Icons.Filled.Cake,
                            errorText = fieldErrors.petAge,
                            imeAction = ImeAction.Done,
                            testTag = "reg_pet_age"
                        )

                        PetGenderChipSelector(
                            selectedGender = petGender,
                            onGenderSelected = { petGender = it }
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isAlreadySignedIn) {
                                "Signed in with Google. Your details will be stored securely under your private owner profile."
                            } else {
                                "No plain-text passwords stored — your account is secured with Google Sign-In & Firebase Auth."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                GoogleSignInActionButton(
                    text = if (isAlreadySignedIn) "Save Profile & Go to Dashboard" else "Register & Connect with Google",
                    isLoading = combinedLoading,
                    useOrangeAccent = true,
                    onClick = {
                        authError = null
                        val ownerInput = OwnerFormInput(
                            fullName = fullName,
                            email = email,
                            phoneNumber = phoneNumber,
                            address = address,
                            city = city,
                            state = state
                        )
                        val petInput = PetFormInput(
                            petName = petName,
                            petType = petType,
                            petBreed = petBreed,
                            petAge = petAge,
                            petGender = petGender
                        )
                        val errors = ProfileFormValidator.validateAll(ownerInput, petInput)
                        fieldErrors = errors
                        if (errors.hasErrors) {
                            authError = "Please check the highlighted fields above before continuing."
                            return@GoogleSignInActionButton
                        }

                        if (isAlreadySignedIn) {
                            onSubmitWithSignedInUser(ownerInput, petInput)
                        } else {
                            authLoading = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    authLoading = false
                                    onSubmitWithSignedInUser(ownerInput, petInput)
                                },
                                onAuthError = { msg ->
                                    authLoading = false
                                    authError = msg
                                },
                                scope = scope,
                                onAuthCancelled = {
                                    authLoading = false
                                }
                            )
                        }
                    },
                    testTag = "register_submit_button"
                )

                if (!isAlreadySignedIn) {
                    OutlinedButton(
                        onClick = onNavigateToLogin,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_to_login_button")
                    ) {
                        Text("Already registered? Sign in with Google")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
