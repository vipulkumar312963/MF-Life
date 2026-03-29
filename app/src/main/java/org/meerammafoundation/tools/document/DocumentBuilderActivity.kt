package org.meerammafoundation.tools.document

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.meerammafoundation.tools.AboutActivity
import org.meerammafoundation.tools.MainActivity
import org.meerammafoundation.tools.R

class DocumentBuilderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.document_activity_document_builder)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Find all tool cards
        val cardResume = findViewById<CardView>(R.id.cardDocResume)
        val cardMarriage = findViewById<CardView>(R.id.cardDocMarriage)
        val cardWill = findViewById<CardView>(R.id.cardDocWill)
        val cardFamilyTree = findViewById<CardView>(R.id.cardDocFamilyTree)
        val cardLeave = findViewById<CardView>(R.id.cardDocLeave)

        // Set click listeners (Coming Soon messages)
        cardResume.setOnClickListener {
            Toast.makeText(this, "📝 Resume Builder - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        cardMarriage.setOnClickListener {
            Toast.makeText(this, "💑 Marriage Bio Data Builder - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        cardWill.setOnClickListener {
            Toast.makeText(this, "⚖️ Will Template Builder - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        cardFamilyTree.setOnClickListener {
            Toast.makeText(this, "🌳 Family Tree Builder - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        cardLeave.setOnClickListener {
            Toast.makeText(this, "📅 Leave Application Builder - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        // Bottom navigation
        val bottomHome = findViewById<TextView>(R.id.bottomHome)
        val bottomAbout = findViewById<TextView>(R.id.bottomAbout)
        val bottomUpdate = findViewById<TextView>(R.id.bottomUpdate)

        bottomHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        bottomAbout.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        bottomUpdate.setOnClickListener {
            Toast.makeText(this, "Update screen - Coming Soon!", Toast.LENGTH_LONG).show()
        }
    }
}