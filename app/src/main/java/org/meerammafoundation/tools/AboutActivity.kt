package org.meerammafoundation.tools

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_about)

        val home = findViewById<TextView>(R.id.bottomHome)
        val about = findViewById<TextView>(R.id.bottomAbout)
        val update = findViewById<TextView>(R.id.bottomUpdate)

        // All tabs gray (no active highlight)
        home.setTextColor(resources.getColor(R.color.text_secondary, null))
        home.background = null
        about.setTextColor(resources.getColor(R.color.text_secondary, null))
        about.background = null
        update.setTextColor(resources.getColor(R.color.text_secondary, null))
        update.background = null

        home.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        about.setOnClickListener {
            Toast.makeText(this, "You are already on About screen", Toast.LENGTH_SHORT).show()
        }

        update.setOnClickListener {
            Toast.makeText(this, "Update screen - Coming Soon!", Toast.LENGTH_LONG).show()
        }
    }
}