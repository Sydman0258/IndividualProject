package com.example.vroomtrack

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<LoginActivity>()

    @Test

    fun testLoginUIElementsVisible() {
        composeTestRule.onNodeWithText("example@example.com").assertExists()
        composeTestRule.onNodeWithText("*******").assertExists()
        composeTestRule.onNode(hasText("Login") and hasClickAction()).assertExists()
        composeTestRule.onNodeWithText("Remember me").assertExists()
        composeTestRule.onNodeWithText("Forget Password").assertExists()
        composeTestRule.onNodeWithText("Don't have an account? Signup").assertExists()
    }


    @Test
    fun testInputTextAndTogglePasswordVisibility() {
        composeTestRule.onNodeWithText("example@example.com")
            .performTextInput("test@example.com")

        composeTestRule.onNodeWithText("*******")
            .performTextInput("password123")

        // Toggle password visibility (you may need to adjust this based on your icon semantics or contentDescription)
        composeTestRule.onAllNodes(hasClickAction())[2].performClick()
    }
}
