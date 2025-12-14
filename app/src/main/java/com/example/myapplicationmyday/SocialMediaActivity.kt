package com.example.myapplicationmyday

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationmyday.adapter.SocialMediaAdapter
import com.example.myapplicationmyday.data.SocialMediaLink
import com.example.myapplicationmyday.data.SocialPlatform
import com.example.myapplicationmyday.databinding.ActivitySocialMediaBinding
import com.example.myapplicationmyday.databinding.DialogAddSocialLinkBinding
import com.example.myapplicationmyday.viewmodel.SocialMediaViewModel
import com.example.myapplicationmyday.util.UrlMetadataExtractor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SocialMediaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySocialMediaBinding
    private val viewModel: SocialMediaViewModel by viewModels()
    private lateinit var adapter: SocialMediaAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // Set user ID for filtering
        auth.currentUser?.uid?.let { userId ->
            viewModel.setUserId(userId)
            viewModel.syncFromFirestore(userId)
        }

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupFilters()
    }

    private fun setupRecyclerView() {
        adapter = SocialMediaAdapter(
            onItemClick = { link ->
                openLink(link.url)
            },
            onMenuClick = { link, view ->
                showLinkMenu(link, view)
            }
        )

        binding.rvSocialLinks.layoutManager = LinearLayoutManager(this)
        binding.rvSocialLinks.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.filteredLinks.observe(this) { links ->
            adapter.submitList(links)

            // Show/hide empty state
            if (links.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.rvSocialLinks.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.rvSocialLinks.visibility = View.VISIBLE
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.fabAddLink.setOnClickListener {
            showAddLinkDialog()
        }
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            val platform = when (checkedIds.firstOrNull()) {
                R.id.chipFacebook -> SocialPlatform.FACEBOOK
                R.id.chipInstagram -> SocialPlatform.INSTAGRAM
                R.id.chipTikTok -> SocialPlatform.TIKTOK
                R.id.chipTwitter -> SocialPlatform.TWITTER
                R.id.chipYouTube -> SocialPlatform.YOUTUBE
                R.id.chipAll -> null
                else -> null
            }
            viewModel.setFilter(platform)
        }

        // Select "All" by default
        binding.chipAll.isChecked = true
    }

    private fun showAddLinkDialog() {
        val dialogBinding = DialogAddSocialLinkBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Select Facebook by default
        dialogBinding.chipSelectFacebook.isChecked = true

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val url = dialogBinding.etUrl.text.toString().trim()
            val selectedChipId = dialogBinding.chipGroupPlatform.checkedChipId

            // Validate
            if (url.isEmpty()) {
                dialogBinding.tilUrl.error = getString(R.string.error_invalid_url)
                return@setOnClickListener
            }

            if (!Patterns.WEB_URL.matcher(url).matches()) {
                dialogBinding.tilUrl.error = getString(R.string.error_invalid_url)
                return@setOnClickListener
            }

            if (selectedChipId == View.NO_ID) {
                Toast.makeText(this, R.string.error_select_platform, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val platform = when (selectedChipId) {
                R.id.chipSelectFacebook -> SocialPlatform.FACEBOOK
                R.id.chipSelectInstagram -> SocialPlatform.INSTAGRAM
                R.id.chipSelectTikTok -> SocialPlatform.TIKTOK
                R.id.chipSelectTwitter -> SocialPlatform.TWITTER
                R.id.chipSelectYouTube -> SocialPlatform.YOUTUBE
                else -> SocialPlatform.OTHER
            }

            val userId = auth.currentUser?.uid ?: ""

            // Show loading message
            dialogBinding.btnSave.isEnabled = false
            dialogBinding.btnSave.text = "Cargando..."

            // Extract metadata in background
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val metadata = withContext(Dispatchers.IO) {
                        UrlMetadataExtractor.extractMetadata(url)
                    }

                    val link = SocialMediaLink(
                        url = url,
                        platform = platform,
                        title = metadata.title ?: "",
                        description = metadata.description ?: "",
                        imageUrl = metadata.imageUrl ?: "",
                        userId = userId,
                        createdAt = System.currentTimeMillis()
                    )

                    viewModel.insert(link)
                    Toast.makeText(this@SocialMediaActivity, R.string.link_saved, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } catch (e: Exception) {
                    // If metadata extraction fails, still save the link without metadata
                    val link = SocialMediaLink(
                        url = url,
                        platform = platform,
                        title = "",
                        description = "",
                        imageUrl = "",
                        userId = userId,
                        createdAt = System.currentTimeMillis()
                    )

                    viewModel.insert(link)
                    Toast.makeText(this@SocialMediaActivity, "Link guardado (sin metadata)", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } finally {
                    dialogBinding.btnSave.isEnabled = true
                    dialogBinding.btnSave.text = getString(R.string.save)
                }
            }
        }

        dialog.show()
    }

    private fun showLinkMenu(link: SocialMediaLink, view: View) {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.entry_menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    openLink(link.url)
                    true
                }
                R.id.action_delete -> {
                    showDeleteConfirmation(link)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showDeleteConfirmation(link: SocialMediaLink) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete))
            .setMessage("¿Eliminar este link?")
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.delete(link)
                Toast.makeText(this, R.string.link_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun openLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir el link", Toast.LENGTH_SHORT).show()
        }
    }
}
