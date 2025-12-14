package com.example.myapplicationmyday

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationmyday.data.User
import com.example.myapplicationmyday.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Log.w("SignUpActivity", "Google sign in failed", e)
            Toast.makeText(this, "Autenticación con Google falló", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        firestore = Firebase.firestore
        
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCreateAccount.setOnClickListener {
            createAccount()
        }
        
        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun createAccount() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Clear previous errors
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        // Validate inputs
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_required)
            binding.etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            binding.etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_required)
            binding.etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_password_length)
            binding.etPassword.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = getString(R.string.error_confirm_password_required)
            binding.etConfirmPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.error_passwords_not_match)
            binding.etConfirmPassword.requestFocus()
            return
        }

        // Create account
        showLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener
                createUserProfile(userId, email)
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                val errorMessage = when {
                    exception.message?.contains("already in use") == true ->
                        getString(R.string.error_email_in_use)
                    exception.message?.contains("network") == true ->
                        getString(R.string.error_network)
                    else -> getString(R.string.error_creating_account)
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }
    
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        showLoading(true)
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    // Check if user document exists, create if not
                    val user = auth.currentUser
                    user?.let {
                        val userDoc = firestore.collection("users").document(it.uid)
                        userDoc.get().addOnSuccessListener { document ->
                            if (!document.exists()) {
                                // Create user document
                                val userData = hashMapOf(
                                    "email" to it.email,
                                    "displayName" to it.displayName,
                                    "createdAt" to System.currentTimeMillis(),
                                    "provider" to "google"
                                )
                                userDoc.set(userData)
                            }
                        }
                    }
                    Toast.makeText(this, getString(R.string.account_created_success), Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    Toast.makeText(
                        this,
                        "Error al autenticar con Google: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun createUserProfile(userId: String, email: String) {
        Log.d("SignUpActivity", "Creating user profile for userId: $userId")
        
        val user = User(
            uid = userId,
            email = email,
            displayName = "",
            username = "",
            photoUrl = "",
            bio = "",
            createdAt = System.currentTimeMillis()
        )

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                showLoading(false)
                Log.d("SignUpActivity", "User profile created successfully in Firestore")
                Toast.makeText(
                    this,
                    getString(R.string.account_created_success),
                    Toast.LENGTH_SHORT
                ).show()
                navigateToHome()
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                Log.e("SignUpActivity", "Error creating user profile", exception)
                Toast.makeText(
                    this,
                    getString(R.string.error_creating_profile) + ": ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnCreateAccount.isEnabled = !isLoading
        binding.btnGoogleSignIn.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.etConfirmPassword.isEnabled = !isLoading
    }
}
