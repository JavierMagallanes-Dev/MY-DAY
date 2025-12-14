package com.example.myapplicationmyday

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationmyday.data.User
import com.example.myapplicationmyday.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        firestore = Firebase.firestore

        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        loadUserProfile()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSignOut.setOnClickListener {
            showSignOutDialog()
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: ""

        // Display email
        binding.tvEmail.text = email

        // Load user data from Firestore to get createdAt timestamp
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    displayMemberSince(user?.createdAt)
                } else {
                    // If no user document, use current date
                    displayMemberSince(System.currentTimeMillis())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
                displayMemberSince(System.currentTimeMillis())
            }
    }

    private fun displayMemberSince(timestamp: Long?) {
        val date = Date(timestamp ?: System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        val formattedDate = dateFormat.format(date)
        binding.tvMemberSince.text = formattedDate.capitalize(Locale.getDefault())
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.sign_out))
            .setMessage(getString(R.string.sign_out_confirm))
            .setPositiveButton(getString(R.string.sign_out)) { _, _ ->
                signOut()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun signOut() {
        auth.signOut()
        Toast.makeText(this, getString(R.string.sign_out_success), Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
