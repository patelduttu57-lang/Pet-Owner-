package com.example

import com.example.data.model.OwnerFormInput
import com.example.data.model.PetFormInput
import com.example.data.model.PetGender
import com.example.data.model.PetType
import com.example.data.model.ProfileFormValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileFormValidatorTest {

    @Test
    fun validateAll_flagsInvalidFields() {
        val result = ProfileFormValidator.validateAll(
            OwnerFormInput(
                fullName = "A",
                email = "invalid-email",
                phoneNumber = "123",
                address = "1",
                city = "A",
                state = "B"
            ),
            PetFormInput(
                petName = "",
                petType = PetType.DOG,
                petBreed = "",
                petAge = "",
                petGender = PetGender.MALE
            )
        )
        assertTrue(result.hasErrors)
        assertNotNull(result.fullName)
        assertNotNull(result.email)
        assertNotNull(result.phoneNumber)
        assertNotNull(result.address)
        assertNotNull(result.city)
        assertNotNull(result.state)
        assertNotNull(result.petName)
        assertNotNull(result.petBreed)
        assertNotNull(result.petAge)
    }

    @Test
    fun validateAll_acceptsValidOwnerAndPetInputs() {
        val result = ProfileFormValidator.validateAll(
            OwnerFormInput(
                fullName = "Jordan Taylor",
                email = "jordan@example.com",
                phoneNumber = "+1 503-555-0188",
                address = "742 Evergreen Terrace",
                city = "Portland",
                state = "Oregon"
            ),
            PetFormInput(
                petName = "Barnaby",
                petType = PetType.DOG,
                petBreed = "Golden Retriever",
                petAge = "3 years",
                petGender = PetGender.MALE
            )
        )
        assertFalse(result.hasErrors)
    }

    @Test
    fun enums_handleSafeFallbacks() {
        assertEquals(PetType.CAT, PetType.fromLabel("Cat"))
        assertEquals(PetType.OTHER, PetType.fromLabel("Dragon"))
        assertEquals(PetGender.FEMALE, PetGender.fromLabel("female"))
        assertEquals(PetGender.UNKNOWN, PetGender.fromLabel("invalid"))
    }
}
