package com.example.vroomtrack

import com.example.vroomtrack.Repository.AuthRepositoryImpl
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.*

class AuthUnitTest {

    @Mock
    private lateinit var mockAuth: FirebaseAuth

    @Mock
    private lateinit var mockAuthResultTask: Task<AuthResult>

    private lateinit var authRepository: AuthRepositoryImpl

    @Captor
    private lateinit var captor: ArgumentCaptor<OnCompleteListener<AuthResult>>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        authRepository = AuthRepositoryImpl(mockAuth)
    }

    @Test
    fun testLogin_Successful() {
        val email = "test@example.com"
        val password = "testPassword"
        var resultMessage = "Initial"

        // Simulate FirebaseAuth signInWithEmailAndPassword returning mockTask
        `when`(mockAuth.signInWithEmailAndPassword(email, password)).thenReturn(mockAuthResultTask)

        // Mock isSuccessful to be true
        `when`(mockAuthResultTask.isSuccessful).thenReturn(true)

        // Define callback
        val callback = { success: Boolean, message: String? ->
            resultMessage = message ?: "null"
        }

        // Call login method
        authRepository.login(email, password, callback)

        // Capture the listener passed to addOnCompleteListener
        verify(mockAuthResultTask).addOnCompleteListener(captor.capture())

        // Trigger the onComplete callback manually
        captor.value.onComplete(mockAuthResultTask)

        // Assertion
        assertEquals("Login successful", resultMessage)
    }
}
