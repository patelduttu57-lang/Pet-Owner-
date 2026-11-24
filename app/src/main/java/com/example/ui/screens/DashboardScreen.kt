package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.FormFieldErrors
import com.example.data.model.OwnerFormInput
import com.example.data.model.PetFormInput
import com.example.data.model.PetGender
import com.example.data.model.PetType
import com.example.data.model.ProfileFormValidator
import com.example.data.model.UserProfile
import com.example.ui.components.PawCareLogoBadge
import com.example.ui.components.PetGenderChipSelector
import com.example.ui.components.PetTypeChipSelector
import com.example.ui.components.SectionHeaderTag
import com.example.ui.components.ValidatedTextField
import com.example.ui.state.MutationState
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    profile: UserProfile,
    mutationState: MutationState,
    onBackToHome: () -> Unit,
    onUpdateOwner: (OwnerFormInput, () -> Unit) -> Unit,
    onUpdatePet: (PetFormInput, () -> Unit) -> Unit,
    onClearMutationState: () -> Unit,
    onLogoutClick: () -> Unit
) {
    BackHandler(onBack = onBackToHome)

    val snackbarHostState = remember { SnackbarHostState() }
    var showEditOwnerDialog by remember { mutableStateOf(false) }
    var showEditPetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(mutationState) {
        when (mutationState) {
            is MutationState.Success -> {
                snackbarHostState.showSnackbar(mutationState.message)
                onClearMutationState()
            }
            is MutationState.Error -> {
                snackbarHostState.showSnackbar(mutationState.message)
                onClearMutationState()
            }
            else -> Unit
        }
    }

    val memberSinceText = remember(profile.createdAt) {
        val ts = profile.createdAt ?: Timestamp.now()
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(ts.toDate())
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { PawCareLogoBadge(subtitle = "Owner Dashboard") },
                actions = {
                    TextButton(
                        onClick = onBackToHome,
                        modifier = Modifier.testTag("dashboard_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Public,
                            contentDescription = "Explore public homepage",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Home")
                    }
                    OutlinedButton(
                        onClick = onLogoutClick,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .heightIn(min = 40.dp)
                            .testTag("dashboard_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            val isWide = maxWidth >= 720.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Welcome Banner Card
                WelcomeDashboardBanner(
                    profile = profile,
                    memberSinceText = memberSinceText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 980.dp)
                )

                if (isWide) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 980.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        OwnerProfileCard(
                            profile = profile,
                            onEditClick = { showEditOwnerDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                        PetDetailsCard(
                            profile = profile,
                            memberSinceText = memberSinceText,
                            onEditClick = { showEditPetDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    OwnerProfileCard(
                        profile = profile,
                        onEditClick = { showEditOwnerDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                    )
                    PetDetailsCard(
                        profile = profile,
                        memberSinceText = memberSinceText,
                        onEditClick = { showEditPetDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                    )
                }
            }
        }
    }

    if (showEditOwnerDialog) {
        EditOwnerProfileDialog(
            profile = profile,
            isSaving = mutationState is MutationState.Saving,
            onDismiss = { showEditOwnerDialog = false },
            onSave = { updatedOwner ->
                onUpdateOwner(updatedOwner) {
                    showEditOwnerDialog = false
                }
            }
        )
    }

    if (showEditPetDialog) {
        EditPetDetailsDialog(
            profile = profile,
            isSaving = mutationState is MutationState.Saving,
            onDismiss = { showEditPetDialog = false },
            onSave = { updatedPet ->
                onUpdatePet(updatedPet) {
                    showEditPetDialog = false
                }
            }
        )
    }
}

@Composable
private fun WelcomeDashboardBanner(
    profile: UserProfile,
    memberSinceText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 165.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_dashboard_banner),
                contentDescription = "Botanical pet owner welcome banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionHeaderTag(label = "Verified PawCare Member", isOrange = true)
                Text(
                    text = "Welcome back, ${profile.fullName}!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Caring for ${profile.petDetails.petName} (${profile.petDetails.petType}) • Member since $memberSinceText",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OwnerProfileCard(
    profile: UserProfile,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Owner profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = "Owner Profile",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Personal & Contact Info",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedButton(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 42.dp)
                        .testTag("edit_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit owner profile",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Profile")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))

            DashboardDetailRow(Icons.Filled.Person, "Full Name", profile.fullName)
            DashboardDetailRow(Icons.Filled.Email, "Email Address", profile.email)
            DashboardDetailRow(Icons.Filled.Phone, "Phone Number", profile.phoneNumber)
            DashboardDetailRow(Icons.Filled.Home, "Residential Address", profile.address)
            DashboardDetailRow(Icons.Filled.LocationCity, "City & State", "${profile.city}, ${profile.state}")
        }
    }
}

@Composable
private fun PetDetailsCard(
    profile: UserProfile,
    memberSinceText: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pet = profile.petDetails
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Pets,
                            contentDescription = "Pet information",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Column {
                        Text(
                            text = "Pet Information",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${pet.petType} Companion Card",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 42.dp)
                        .testTag("edit_pet_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Update pet details",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Update Pet")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))

            DashboardDetailRow(Icons.Filled.Pets, "Pet Name", pet.petName, isOrange = true)
            DashboardDetailRow(Icons.Filled.Pets, "Pet Type", pet.petType, isOrange = true)
            DashboardDetailRow(Icons.Filled.Pets, "Breed", pet.petBreed, isOrange = true)
            DashboardDetailRow(Icons.Filled.Cake, "Age", pet.petAge, isOrange = true)
            DashboardDetailRow(Icons.Filled.Person, "Gender", pet.petGender, isOrange = true)
            DashboardDetailRow(Icons.Filled.CalendarToday, "Registered On", memberSinceText, isOrange = true)
        }
    }
}

@Composable
private fun DashboardDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    isOrange: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isOrange) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value.ifBlank { "—" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EditOwnerProfileDialog(
    profile: UserProfile,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (OwnerFormInput) -> Unit
) {
    var fullName by remember { mutableStateOf(profile.fullName) }
    var email by remember { mutableStateOf(profile.email) }
    var phoneNumber by remember { mutableStateOf(profile.phoneNumber) }
    var address by remember { mutableStateOf(profile.address) }
    var city by remember { mutableStateOf(profile.city) }
    var state by remember { mutableStateOf(profile.state) }
    var errors by remember { mutableStateOf(FormFieldErrors()) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Edit Owner Profile") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ValidatedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; errors = errors.copy(fullName = null) },
                    label = "Full Name",
                    placeholder = "Full Name",
                    leadingIcon = Icons.Filled.Person,
                    errorText = errors.fullName,
                    testTag = "edit_owner_name"
                )
                ValidatedTextField(
                    value = email,
                    onValueChange = { email = it; errors = errors.copy(email = null) },
                    label = "Email Address",
                    placeholder = "Email",
                    leadingIcon = Icons.Filled.Email,
                    errorText = errors.email,
                    keyboardType = KeyboardType.Email,
                    testTag = "edit_owner_email"
                )
                ValidatedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it; errors = errors.copy(phoneNumber = null) },
                    label = "Phone Number",
                    placeholder = "Phone",
                    leadingIcon = Icons.Filled.Phone,
                    errorText = errors.phoneNumber,
                    keyboardType = KeyboardType.Phone,
                    testTag = "edit_owner_phone"
                )
                ValidatedTextField(
                    value = address,
                    onValueChange = { address = it; errors = errors.copy(address = null) },
                    label = "Residential Address",
                    placeholder = "Address",
                    leadingIcon = Icons.Filled.Home,
                    errorText = errors.address,
                    testTag = "edit_owner_address"
                )
                ValidatedTextField(
                    value = city,
                    onValueChange = { city = it; errors = errors.copy(city = null) },
                    label = "City",
                    placeholder = "City",
                    leadingIcon = Icons.Filled.LocationCity,
                    errorText = errors.city,
                    testTag = "edit_owner_city"
                )
                ValidatedTextField(
                    value = state,
                    onValueChange = { state = it; errors = errors.copy(state = null) },
                    label = "State",
                    placeholder = "State",
                    leadingIcon = Icons.Filled.Map,
                    errorText = errors.state,
                    imeAction = ImeAction.Done,
                    testTag = "edit_owner_state"
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving,
                onClick = {
                    val input = OwnerFormInput(fullName, email, phoneNumber, address, city, state)
                    val validation = ProfileFormValidator.validateOwner(input)
                    errors = validation
                    if (!validation.hasErrors) {
                        onSave(input)
                    }
                },
                modifier = Modifier.testTag("save_owner_button")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save Changes")
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditPetDetailsDialog(
    profile: UserProfile,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (PetFormInput) -> Unit
) {
    val existingPet = profile.petDetails
    var petName by remember { mutableStateOf(existingPet.petName) }
    var petType by remember { mutableStateOf(PetType.fromLabel(existingPet.petType)) }
    var petBreed by remember { mutableStateOf(existingPet.petBreed) }
    var petAge by remember { mutableStateOf(existingPet.petAge) }
    var petGender by remember { mutableStateOf(PetGender.fromLabel(existingPet.petGender)) }
    var errors by remember { mutableStateOf(FormFieldErrors()) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Update Pet Details") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ValidatedTextField(
                    value = petName,
                    onValueChange = { petName = it; errors = errors.copy(petName = null) },
                    label = "Pet Name",
                    placeholder = "Pet Name",
                    leadingIcon = Icons.Filled.Pets,
                    errorText = errors.petName,
                    testTag = "edit_pet_name"
                )
                PetTypeChipSelector(
                    selectedType = petType,
                    onTypeSelected = { petType = it }
                )
                ValidatedTextField(
                    value = petBreed,
                    onValueChange = { petBreed = it; errors = errors.copy(petBreed = null) },
                    label = "Pet Breed",
                    placeholder = "Breed",
                    leadingIcon = Icons.Filled.Pets,
                    errorText = errors.petBreed,
                    testTag = "edit_pet_breed"
                )
                ValidatedTextField(
                    value = petAge,
                    onValueChange = { petAge = it; errors = errors.copy(petAge = null) },
                    label = "Pet Age",
                    placeholder = "Age",
                    leadingIcon = Icons.Filled.Cake,
                    errorText = errors.petAge,
                    imeAction = ImeAction.Done,
                    testTag = "edit_pet_age"
                )
                PetGenderChipSelector(
                    selectedGender = petGender,
                    onGenderSelected = { petGender = it }
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                onClick = {
                    val input = PetFormInput(petName, petType, petBreed, petAge, petGender)
                    val validation = ProfileFormValidator.validatePet(input)
                    errors = validation
                    if (!validation.hasErrors) {
                        onSave(input)
                    }
                },
                modifier = Modifier.testTag("save_pet_button")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save Pet")
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
