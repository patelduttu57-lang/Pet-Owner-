package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.PetType
import com.example.ui.components.PawCareLogoBadge
import com.example.ui.components.SectionHeaderTag
import com.example.ui.components.ValidatedTextField
import kotlinx.coroutines.launch

private data class NavSectionItem(
    val label: String,
    val listIndex: Int,
    val icon: ImageVector
)

private data class ServiceCardData(
    val title: String,
    val description: String,
    val badge: String,
    val icon: ImageVector,
    val isOrangeAccent: Boolean
)

private data class PetCareTipData(
    val title: String,
    val summary: String,
    val bullets: List<String>
)

@Composable
fun PublicHomepageScreen(
    isAuthenticated: Boolean,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var activeSectionIndex by remember { mutableIntStateOf(0) }

    val navSections = remember {
        listOf(
            NavSectionItem("Home", 0, Icons.Filled.Home),
            NavSectionItem("About", 1, Icons.Filled.Info),
            NavSectionItem("Services", 2, Icons.Filled.MedicalServices),
            NavSectionItem("Pet Care", 3, Icons.Filled.Pets),
            NavSectionItem("Contact", 4, Icons.Filled.Call)
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HomepageTopNavigationBar(
                isAuthenticated = isAuthenticated,
                navSections = navSections,
                activeSectionIndex = activeSectionIndex,
                onSectionClick = { index, listIndex ->
                    activeSectionIndex = index
                    coroutineScope.launch {
                        listState.animateScrollToItem(listIndex)
                    }
                },
                onLoginClick = onNavigateToLogin,
                onRegisterClick = onNavigateToRegister,
                onDashboardClick = onNavigateToDashboard,
                onLogoutClick = onLogoutClick
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 680.dp

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 0: HOME HERO SECTION
                item(key = "section_home") {
                    HeroSection(
                        isWideScreen = isWideScreen,
                        isAuthenticated = isAuthenticated,
                        onRegisterClick = onNavigateToRegister,
                        onLoginClick = onNavigateToLogin,
                        onDashboardClick = onNavigateToDashboard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 980.dp)
                    )
                }

                // 1: ABOUT US SECTION
                item(key = "section_about") {
                    AboutUsSection(
                        isWideScreen = isWideScreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 980.dp)
                    )
                }

                // 2: SERVICES SECTION
                item(key = "section_services") {
                    ServicesSection(
                        isWideScreen = isWideScreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 980.dp)
                    )
                }

                // 3: PET CARE INFORMATION SECTION
                item(key = "section_pet_care") {
                    PetCareInfoSection(
                        isWideScreen = isWideScreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 980.dp)
                    )
                }

                // 4: CONTACT US SECTION & FOOTER
                item(key = "section_contact") {
                    ContactUsSection(
                        isWideScreen = isWideScreen,
                        isAuthenticated = isAuthenticated,
                        onLoginClick = onNavigateToLogin,
                        onRegisterClick = onNavigateToRegister,
                        onDashboardClick = onNavigateToDashboard,
                        onScrollTopClick = {
                            activeSectionIndex = 0
                            coroutineScope.launch { listState.animateScrollToItem(0) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 980.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomepageTopNavigationBar(
    isAuthenticated: Boolean,
    navSections: List<NavSectionItem>,
    activeSectionIndex: Int,
    onSectionClick: (Int, Int) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onDashboardClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PawCareLogoBadge(subtitle = "Pet Owner Portal")

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAuthenticated) {
                        Button(
                            onClick = onDashboardClick,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .testTag("nav_dashboard_button")
                        ) {
                            Text(stringResource(R.string.action_dashboard))
                        }
                        OutlinedButton(
                            onClick = onLogoutClick,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .testTag("nav_logout_button")
                        ) {
                            Text(stringResource(R.string.action_logout))
                        }
                    } else {
                        OutlinedButton(
                            onClick = onLoginClick,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .testTag("nav_login_button")
                        ) {
                            Text(stringResource(R.string.action_login))
                        }
                        Button(
                            onClick = onRegisterClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .testTag("nav_register_button")
                        ) {
                            Text(stringResource(R.string.action_register))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(navSections) { index, section ->
                    val selected = index == activeSectionIndex
                    FilterChip(
                        selected = selected,
                        onClick = { onSectionClick(index, section.listIndex) },
                        label = { Text(section.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = section.label,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .testTag("nav_chip_${section.label.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroSection(
    isWideScreen: Boolean,
    isAuthenticated: Boolean,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onDashboardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.28f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            if (isWideScreen) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeroCopyBlock(
                        isAuthenticated = isAuthenticated,
                        onRegisterClick = onRegisterClick,
                        onLoginClick = onLoginClick,
                        onDashboardClick = onDashboardClick,
                        modifier = Modifier.weight(1.1f)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_pets),
                        contentDescription = "Happy dog, cat, and songbird cared for with PawCare",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(0.9f)
                            .height(250.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_pets),
                        contentDescription = "Happy dog, cat, and songbird cared for with PawCare",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                    HeroCopyBlock(
                        isAuthenticated = isAuthenticated,
                        onRegisterClick = onRegisterClick,
                        onLoginClick = onLoginClick,
                        onDashboardClick = onDashboardClick
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCopyBlock(
    isAuthenticated: Boolean,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onDashboardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeaderTag(label = "Trusted Pet Owner Registry", isOrange = true)

        Text(
            text = "Every Pet Deserves Organized, Loving Care.",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "PawCare connects pet families with digital companion profiles, emergency contact readiness, and practical species-specific care guides — all secured in the cloud.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isAuthenticated) {
                Button(
                    onClick = onDashboardClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("hero_dashboard_button")
                ) {
                    Text("Open My Dashboard")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Button(
                    onClick = onRegisterClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("hero_register_button")
                ) {
                    Text("Register Pet")
                }

                OutlinedButton(
                    onClick = onLoginClick,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("hero_login_button")
                ) {
                    Text("Owner Login")
                }
            }
        }
    }
}

@Composable
private fun AboutUsSection(
    isWideScreen: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeaderTag(label = "About PawCare")

            if (isWideScreen) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_about_care),
                        contentDescription = "Pet owner caring for dog and cat at home",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(0.9f)
                            .height(220.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                    AboutTextColumn(modifier = Modifier.weight(1.1f))
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_about_care),
                    contentDescription = "Pet owner caring for dog and cat at home",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
                AboutTextColumn()
            }
        }
    }
}

@Composable
private fun AboutTextColumn(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Built Around Peace of Mind for Pet Families",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Whether you share your home with an energetic puppy, a curious tabby, or a singing parrot, PawCare keeps your contact details and companion records accurate, private, and ready whenever a veterinarian or caregiver needs them.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AboutHighlightRow(
                icon = Icons.Filled.VerifiedUser,
                text = "Owner-isolated Cloud Firestore profiles"
            )
            AboutHighlightRow(
                icon = Icons.Filled.Favorite,
                text = "Tailored records for Dogs, Cats, Birds & More"
            )
            AboutHighlightRow(
                icon = Icons.Filled.Shield,
                text = "Zero plain-text passwords — Google-verified security"
            )
        }
    }
}

@Composable
private fun AboutHighlightRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ServicesSection(
    isWideScreen: Boolean,
    modifier: Modifier = Modifier
) {
    val services = remember {
        listOf(
            ServiceCardData(
                title = "Pet Profile Registry",
                description = "Maintain breed, age, species, and gender details in one clean digital companion card.",
                badge = "Core Registry",
                icon = Icons.Filled.Pets,
                isOrangeAccent = false
            ),
            ServiceCardData(
                title = "Owner Contact Vault",
                description = "Keep phone numbers and residential addresses current for clinics, sitters, and emergencies.",
                badge = "Always Accessible",
                icon = Icons.Filled.Home,
                isOrangeAccent = true
            ),
            ServiceCardData(
                title = "Wellness Checklists",
                description = "Guidance for annual vet checkups, dental hygiene, vaccinations, and parasite prevention.",
                badge = "Preventive Care",
                icon = Icons.Filled.HealthAndSafety,
                isOrangeAccent = false
            ),
            ServiceCardData(
                title = "Caregiver Readiness",
                description = "Quickly update your pet's age or owner address from your private dashboard in seconds.",
                badge = "Instant Sync",
                icon = Icons.Filled.NotificationsActive,
                isOrangeAccent = true
            )
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionHeaderTag(label = "Our Services", isOrange = true)
        Text(
            text = "Everything Pet Owners Need in One Place",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (isWideScreen) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                services.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { service ->
                            ServiceCard(service = service, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                services.forEach { service ->
                    ServiceCard(service = service, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(service: ServiceCardData, modifier: Modifier = Modifier) {
    val containerTint = if (service.isOrangeAccent) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    }
    val iconTint = if (service.isOrangeAccent) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(containerTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = service.title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = service.badge.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = iconTint,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PetCareInfoSection(
    isWideScreen: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedPetType by remember { mutableStateOf(PetType.DOG) }

    val tipsBySpecies = remember {
        mapOf(
            PetType.DOG to PetCareTipData(
                title = "Canine Daily Wellness & Exercise",
                summary = "Dogs thrive on consistent movement, balanced protein, and routine checkups.",
                bullets = listOf(
                    "Provide 30–60 minutes of daily brisk walks and mental enrichment.",
                    "Keep fresh water accessible and measure portioned meals twice daily.",
                    "Schedule annual booster vaccinations and year-round flea/tick prevention."
                )
            ),
            PetType.CAT to PetCareTipData(
                title = "Feline Comfort, Hydration & Play",
                summary = "Indoor cats stay healthy with hydration support, vertical perches, and interactive play.",
                bullets = listOf(
                    "Encourage hydration with wide water bowls or circulating fountains.",
                    "Rotate interactive feather/wand play for 15 minutes before evening meals.",
                    "Scoop litter boxes daily and book yearly dental & wellness exams."
                )
            ),
            PetType.BIRD to PetCareTipData(
                title = "Avian Nutrition & Safe Habitats",
                summary = "Companion birds need varied foraging diets, clean air, and daily social interaction.",
                bullets = listOf(
                    "Offer formulated pellets alongside dark leafy greens and safe fruits.",
                    "Place cages away from kitchen fumes, drafts, and direct harsh heat.",
                    "Provide natural wood perches of varying widths for healthy feet."
                )
            ),
            PetType.OTHER to PetCareTipData(
                title = "Small Mammals & Exotic Companions",
                summary = "Rabbits, guinea pigs, and reptiles depend on tailored habitats and exotic vet care.",
                bullets = listOf(
                    "Maintain species-specific temperature, bedding, and fresh hay or greens.",
                    "Monitor appetite and activity daily — small pets hide discomfort early.",
                    "Keep an exotic-experienced veterinarian's contact on your PawCare card."
                )
            )
        )
    }

    val currentTip = tipsBySpecies[selectedPetType] ?: tipsBySpecies.getValue(PetType.DOG)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeaderTag(label = "Pet Care Information")

            Text(
                text = "Species-Specific Care Guides",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PetType.entries.forEach { species ->
                    val isSelected = species == selectedPetType
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPetType = species },
                        label = { Text(species.label) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .testTag("care_tab_${species.label.lowercase()}")
                    )
                }
            }

            if (isWideScreen) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_pet_care_guide),
                        contentDescription = "Pet nutrition bowl, stethoscope, and ID tag illustration",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(0.85f)
                            .height(210.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                    CareGuideDetailCard(
                        tip = currentTip,
                        modifier = Modifier.weight(1.15f)
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_pet_care_guide),
                    contentDescription = "Pet nutrition bowl, stethoscope, and ID tag illustration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
                CareGuideDetailCard(tip = currentTip)
            }
        }
    }
}

@Composable
private fun CareGuideDetailCard(
    tip: PetCareTipData,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = tip.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            tip.bullets.forEach { bullet ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(16.dp)
                    )
                    Text(
                        text = bullet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactUsSection(
    isWideScreen: Boolean,
    isAuthenticated: Boolean,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onDashboardClick: () -> Unit,
    onScrollTopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var senderName by remember { mutableStateOf("") }
    var senderEmail by remember { mutableStateOf("") }
    var inquiryText by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var messageError by remember { mutableStateOf<String?>(null) }
    var inquirySubmitted by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionHeaderTag(label = "Contact Us", isOrange = true)

                Text(
                    text = "We're Here for Every Tail Wag & Purr",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isWideScreen) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        ContactInfoColumn(modifier = Modifier.weight(0.9f))
                        ContactFormColumn(
                            senderName = senderName,
                            onNameChange = { senderName = it; nameError = null; inquirySubmitted = false },
                            senderEmail = senderEmail,
                            onEmailChange = { senderEmail = it; emailError = null; inquirySubmitted = false },
                            inquiryText = inquiryText,
                            onMessageChange = { inquiryText = it; messageError = null; inquirySubmitted = false },
                            nameError = nameError,
                            emailError = emailError,
                            messageError = messageError,
                            inquirySubmitted = inquirySubmitted,
                            onSubmit = {
                                nameError = if (senderName.trim().length < 2) "Please enter your name." else null
                                emailError = if (!android.util.Patterns.EMAIL_ADDRESS.matcher(senderEmail.trim()).matches()) {
                                    "Enter a valid email address."
                                } else null
                                messageError = if (inquiryText.trim().length < 5) "Enter a brief question or message." else null
                                if (nameError == null && emailError == null && messageError == null) {
                                    inquirySubmitted = true
                                    senderName = ""
                                    senderEmail = ""
                                    inquiryText = ""
                                }
                            },
                            modifier = Modifier.weight(1.1f)
                        )
                    }
                } else {
                    ContactInfoColumn()
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    ContactFormColumn(
                        senderName = senderName,
                        onNameChange = { senderName = it; nameError = null; inquirySubmitted = false },
                        senderEmail = senderEmail,
                        onEmailChange = { senderEmail = it; emailError = null; inquirySubmitted = false },
                        inquiryText = inquiryText,
                        onMessageChange = { inquiryText = it; messageError = null; inquirySubmitted = false },
                        nameError = nameError,
                        emailError = emailError,
                        messageError = messageError,
                        inquirySubmitted = inquirySubmitted,
                        onSubmit = {
                            nameError = if (senderName.trim().length < 2) "Please enter your name." else null
                            emailError = if (!android.util.Patterns.EMAIL_ADDRESS.matcher(senderEmail.trim()).matches()) {
                                "Enter a valid email address."
                            } else null
                            messageError = if (inquiryText.trim().length < 5) "Enter a brief question or message." else null
                            if (nameError == null && emailError == null && messageError == null) {
                                inquirySubmitted = true
                                senderName = ""
                                senderEmail = ""
                                inquiryText = ""
                            }
                        }
                    )
                }
            }
        }

        // Clean Footer
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PawCareLogoBadge(subtitle = "Pet Owner Management System")
                    TextButton(onClick = onScrollTopClick) {
                        Text("Back to Top")
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAuthenticated) {
                        TextButton(onClick = onDashboardClick) {
                            Text("My Dashboard")
                        }
                    } else {
                        TextButton(onClick = onLoginClick) {
                            Text("Member Login")
                        }
                        TextButton(onClick = onRegisterClick) {
                            Text("Register Pet")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactInfoColumn(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ContactRowItem(
            icon = Icons.Filled.LocationOn,
            title = "Care Center",
            detail = "420 Meadowbrook Way, Suite 12, Portland, OR"
        )
        ContactRowItem(
            icon = Icons.Filled.Email,
            title = "Support Email",
            detail = "care@pawcareportal.org"
        )
        ContactRowItem(
            icon = Icons.Filled.Call,
            title = "Pet Owner Helpline",
            detail = "+1 (503) 555-0194"
        )
        ContactRowItem(
            icon = Icons.Filled.Schedule,
            title = "Support Hours",
            detail = "Mon – Sat • 8:00 AM – 7:00 PM"
        )
    }
}

@Composable
private fun ContactRowItem(
    icon: ImageVector,
    title: String,
    detail: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ContactFormColumn(
    senderName: String,
    onNameChange: (String) -> Unit,
    senderEmail: String,
    onEmailChange: (String) -> Unit,
    inquiryText: String,
    onMessageChange: (String) -> Unit,
    nameError: String?,
    emailError: String?,
    messageError: String?,
    inquirySubmitted: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Send a Quick Inquiry",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        AnimatedVisibility(visible = inquirySubmitted) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Inquiry received",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Thank you! Our pet care team has received your inquiry.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        ValidatedTextField(
            value = senderName,
            onValueChange = onNameChange,
            label = "Your Name",
            placeholder = "Alex Rivera",
            leadingIcon = Icons.Filled.Person,
            errorText = nameError,
            testTag = "contact_name_input"
        )

        ValidatedTextField(
            value = senderEmail,
            onValueChange = onEmailChange,
            label = "Email Address",
            placeholder = "alex@example.com",
            leadingIcon = Icons.Outlined.Email,
            errorText = emailError,
            keyboardType = KeyboardType.Email,
            testTag = "contact_email_input"
        )

        ValidatedTextField(
            value = inquiryText,
            onValueChange = onMessageChange,
            label = "Message",
            placeholder = "How can we help you and your pet?",
            leadingIcon = Icons.AutoMirrored.Outlined.Message,
            errorText = messageError,
            singleLine = false,
            maxLines = 3,
            imeAction = ImeAction.Done,
            testTag = "contact_message_input"
        )

        Button(
            onClick = onSubmit,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("contact_submit_button")
        ) {
            Text("Send Message")
        }
    }
}
