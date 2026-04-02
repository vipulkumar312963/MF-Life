package org.meerammafoundation.tools.ui.quickaction.billSplitter

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import org.meerammafoundation.tools.R

class GroupSettingsActivity : AppCompatActivity() {

    private lateinit var viewModel: BillSplitterViewModel
    private lateinit var tvGroupName: TextView
    private lateinit var btnRename: Button
    private lateinit var btnDelete: Button
    private var currentGroup: Group? = null
    private var groupId: Long = -1
    private var groupName: String = ""

    companion object {
        const val EXTRA_GROUP_ID = "extra_group_id"
        const val EXTRA_GROUP_NAME = "extra_group_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.billsplitter_activity_group_settings)

        // Get group data from intent
        groupId = intent.getLongExtra(EXTRA_GROUP_ID, -1)
        groupName = intent.getStringExtra(EXTRA_GROUP_NAME) ?: "Group"

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[BillSplitterViewModel::class.java]

        // Back button
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize views
        tvGroupName = findViewById(R.id.tvGroupName)
        btnRename = findViewById(R.id.btnRename)
        btnDelete = findViewById(R.id.btnDelete)

        tvGroupName.text = groupName

        // Load the full group object
        viewModel.getGroupById(groupId).observe(this) { group ->
            currentGroup = group
        }

        // Set click listeners
        btnRename.setOnClickListener {
            showRenameDialog()
        }

        btnDelete.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun showRenameDialog() {
        val dialogView = layoutInflater.inflate(R.layout.billsplitter_dialog_rename_group, null)
        val etNewName = dialogView.findViewById<EditText>(R.id.etNewName)
        etNewName.setText(groupName)

        AlertDialog.Builder(this)
            .setTitle("Rename Group")
            .setView(dialogView)
            .setPositiveButton("Rename") { _, _ ->
                val newName = etNewName.text.toString().trim()
                if (newName.isNotEmpty()) {
                    currentGroup?.let { group ->
                        val updatedGroup = group.copy(name = newName)
                        viewModel.updateGroup(updatedGroup)
                        groupName = newName
                        tvGroupName.text = newName
                        Toast.makeText(this, "Group renamed successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Group")
            .setMessage("Are you sure you want to permanently delete this group? All members and bills will be lost. This action cannot be undone!")
            .setPositiveButton("Delete") { _, _ ->
                currentGroup?.let { group ->
                    viewModel.deleteGroup(group)
                    Toast.makeText(this, "Group deleted", Toast.LENGTH_SHORT).show()
                    // ✅ Just close settings - let GroupDetail handle the rest
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}