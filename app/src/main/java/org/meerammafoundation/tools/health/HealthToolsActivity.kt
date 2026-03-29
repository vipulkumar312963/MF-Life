package org.meerammafoundation.tools.health

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.meerammafoundation.tools.AboutActivity
import org.meerammafoundation.tools.MainActivity
import org.meerammafoundation.tools.R

class HealthToolsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.health_activity_health_tools)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Find all tool cards
        val cardBMI = findViewById<CardView>(R.id.cardHealthBMI)
        val cardBMR = findViewById<CardView>(R.id.cardHealthBMR)
        val cardCalorie = findViewById<CardView>(R.id.cardHealthCalorie)
        val cardBP = findViewById<CardView>(R.id.cardHealthBP)
        val cardSugar = findViewById<CardView>(R.id.cardHealthSugar)
        val cardSleep = findViewById<CardView>(R.id.cardHealthSleep)
        val cardRecords = findViewById<CardView>(R.id.cardHealthRecords)
        val cardMedicine = findViewById<CardView>(R.id.cardHealthMedicine)
        val cardVaccine = findViewById<CardView>(R.id.cardHealthVaccine)
        val cardPeriod = findViewById<CardView>(R.id.cardHealthPeriod)
        val cardPregnancy = findViewById<CardView>(R.id.cardHealthPregnancy)

        // Set click listeners (Coming Soon messages)
        cardBMI.setOnClickListener {
            Toast.makeText(this, "⚖️ BMI Calculator - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardBMR.setOnClickListener {
            Toast.makeText(this, "🔥 BMR Calculator - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardCalorie.setOnClickListener {
            Toast.makeText(this, "🍎 Calorie Counter - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardBP.setOnClickListener {
            Toast.makeText(this, "❤️ Blood Pressure Log - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardSugar.setOnClickListener {
            Toast.makeText(this, "🍬 Blood Sugar Tracker - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardSleep.setOnClickListener {
            Toast.makeText(this, "😴 Sleep Tracker - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardRecords.setOnClickListener {
            Toast.makeText(this, "📋 Health Record Keeper - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardMedicine.setOnClickListener {
            Toast.makeText(this, "💊 Medicine Reminder - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardVaccine.setOnClickListener {
            Toast.makeText(this, "💉 Vaccination Reminder - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardPeriod.setOnClickListener {
            Toast.makeText(this, "🗓️ Period Tracker - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardPregnancy.setOnClickListener {
            Toast.makeText(this, "🤰 Pregnancy Tracker - Coming Soon!", Toast.LENGTH_SHORT).show()
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