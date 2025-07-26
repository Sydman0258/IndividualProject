package com.example.vroomtrack

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class RegistrationActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<RegistrationActivity>()

    @Test
    fun testRegistrationUIElementsVisible() {
        composeTestRule.onNodeWithText("VR.O.OM Track").assertExists()

        composeTestRule.onNodeWithText("Username").assertExists()
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()

        composeTestRule.onNodeWithText("Register").assertExists()

        composeTestRule.onNodeWithText("Already a member?", substring = true).assertExists()
        composeTestRule.onNodeWithText("Login").assertExists()
    }

    @Test
    fun testRegistrationInputAndButtonClick() {
        val testUsername = "testuser"
        val testEmail = "test@example.com"
        val testPassword = "password123"

        composeTestRule.onNodeWithText("Username").performTextInput(testUsername)
        composeTestRule.onNodeWithText("Email").performTextInput(testEmail)
        composeTestRule.onNodeWithText("Password").performTextInput(testPassword)

        composeTestRule.onNodeWithText("Register").performClick()

    }
}
