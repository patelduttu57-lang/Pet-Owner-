package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class PetType(val label: String) {
    DOG("Dog"),
    CAT("Cat"),
    BIRD("Bird"),
    OTHER("Other");

    companion object {
        fun fromLabel(raw: String?): PetType =
            entries.firstOrNull { it.label.equals(raw, ignoreCase = true) } ?: OTHER
    }
}

enum class PetGender(val label: String) {
    MALE("Male"),
    FEMALE("Female"),
    UNKNOWN("Unknown");

    companion object {
        fun fromLabel(raw: String?): PetGender =
            entries.firstOrNull { it.label.equals(raw, ignoreCase = true) } ?: UNKNOWN
    }
}

@Keep
@IgnoreExtraProperties
data class PetDetails(
    val petName: String = "",
    val petType: String = PetType.DOG.label,
    val petBreed: String = "",
    val petAge: String = "",
    val petGender: String = PetGender.MALE.label
)

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val petDetails: PetDetails = PetDetails(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class OwnerFormInput(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = ""
)

data class PetFormInput(
    val petName: String = "",
    val petType: PetType = PetType.DOG,
    val petBreed: String = "",
    val petAge: String = "",
    val petGender: PetGender = PetGender.MALE
)

data class FormFieldErrors(
    val fullName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val petName: String? = null,
    val petBreed: String? = null,
    val petAge: String? = null
) {
    val hasErrors: Boolean
        get() = listOf(
            fullName, email, phoneNumber, address, city, state,
            petName, petBreed, petAge
        ).any { it != null }
}

object ProfileFormValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateOwner(input: OwnerFormInput): FormFieldErrors {
        val name = input.fullName.trim()
        val email = input.email.trim()
        val phone = input.phoneNumber.trim()
        val address = input.address.trim()
        val city = input.city.trim()
        val state = input.state.trim()

        val nameErr = when {
            name.length < 2 -> "Enter your full name (at least 2 characters)."
            name.length > 80 -> "Full name must be 80 characters or fewer."
            else -> null
        }

        val emailErr = when {
            email.isEmpty() -> "Email address is required."
            email.length > 120 -> "Email must be 120 characters or fewer."
            !EMAIL_REGEX.matches(email) -> "Enter a valid email address."
            else -> null
        }

        val digitsCount = phone.count { it.isDigit() }
        val phoneErr = when {
            phone.length < 7 || digitsCount < 7 -> "Enter a valid phone number (at least 7 digits)."
            phone.length > 25 -> "Phone number must be 25 characters or fewer."
            else -> null
        }

        val addressErr = when {
            address.length < 3 -> "Enter your street address (at least 3 characters)."
            address.length > 160 -> "Address must be 160 characters or fewer."
            else -> null
        }

        val cityErr = when {
            city.length < 2 -> "Enter your city (at least 2 characters)."
            city.length > 80 -> "City must be 80 characters or fewer."
            else -> null
        }

        val stateErr = when {
            state.length < 2 -> "Enter your state (at least 2 characters)."
            state.length > 80 -> "State must be 80 characters or fewer."
            else -> null
        }

        return FormFieldErrors(
            fullName = nameErr,
            email = emailErr,
            phoneNumber = phoneErr,
            address = addressErr,
            city = cityErr,
            state = stateErr
        )
    }

    fun validatePet(input: PetFormInput): FormFieldErrors {
        val petName = input.petName.trim()
        val petBreed = input.petBreed.trim()
        val petAge = input.petAge.trim()

        val petNameErr = when {
            petName.isEmpty() -> "Pet name is required."
            petName.length > 60 -> "Pet name must be 60 characters or fewer."
            else -> null
        }

        val petBreedErr = when {
            petBreed.isEmpty() -> "Pet breed is required."
            petBreed.length > 80 -> "Pet breed must be 80 characters or fewer."
            else -> null
        }

        val petAgeErr = when {
            petAge.isEmpty() -> "Pet age is required (e.g., 2 years or 6 months)."
            petAge.length > 30 -> "Pet age must be 30 characters or fewer."
            else -> null
        }

        return FormFieldErrors(
            petName = petNameErr,
            petBreed = petBreedErr,
            petAge = petAgeErr
        )
    }

    fun validateAll(owner: OwnerFormInput, pet: PetFormInput): FormFieldErrors {
        val o = validateOwner(owner)
        val p = validatePet(pet)
        return o.copy(
            petName = p.petName,
            petBreed = p.petBreed,
            petAge = p.petAge
        )
    }
}
