package org.meerammafoundation.tools.family

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.meerammafoundation.tools.AboutActivity
import org.meerammafoundation.tools.MainActivity
import org.meerammafoundation.tools.R

class FamilyToolsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.family_activity_family_tools)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // EVENTS category
        val cardBirthday = findViewById<CardView>(R.id.cardFamilyBirthday)
        val cardAnniversary = findViewById<CardView>(R.id.cardFamilyAnniversary)
        val cardEventCalendar = findViewById<CardView>(R.id.cardFamilyEventCalendar)
        val cardGiftPlanner = findViewById<CardView>(R.id.cardFamilyGiftPlanner)

        // HEALTH category
        val cardBloodGroup = findViewById<CardView>(R.id.cardFamilyBloodGroup)

        // HOME category
        val cardRecipe = findViewById<CardView>(R.id.cardFamilyRecipe)
        val cardGrocery = findViewById<CardView>(R.id.cardFamilyGrocery)

        // HOUSEHOLD category
        val cardMaintenance = findViewById<CardView>(R.id.cardFamilyMaintenance)
        val cardChore = findViewById<CardView>(R.id.cardFamilyChore)
        val cardUtility = findViewById<CardView>(R.id.cardFamilyUtility)

        // SAFETY category
        val cardEmergencyContacts = findViewById<CardView>(R.id.cardFamilyEmergencyContacts)
        val cardEmergencyPrep = findViewById<CardView>(R.id.cardFamilyEmergencyPrep)
        val cardDisaster = findViewById<CardView>(R.id.cardFamilyDisaster)
        val cardFirstAid = findViewById<CardView>(R.id.cardFamilyFirstAid)
        val cardLocalServices = findViewById<CardView>(R.id.cardFamilyLocalServices)

        // Set click listeners - EVENTS
        cardBirthday.setOnClickListener {
            Toast.makeText(this, "🎂 Birthday Bank - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardAnniversary.setOnClickListener {
            Toast.makeText(this, "💍 Anniversary Reminder - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardEventCalendar.setOnClickListener {
            Toast.makeText(this, "📅 Family Event Calendar - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardGiftPlanner.setOnClickListener {
            Toast.makeText(this, "🎁 Gift Planner - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        // HEALTH
        cardBloodGroup.setOnClickListener {
            Toast.makeText(this, "🩸 Family Blood Group - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        // HOME
        cardRecipe.setOnClickListener {
            Toast.makeText(this, "📖 Recipe Keeper - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardGrocery.setOnClickListener {
            Toast.makeText(this, "🛒 Grocery List Builder - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        // HOUSEHOLD
        cardMaintenance.setOnClickListener {
            Toast.makeText(this, "🔧 Maintenance Reminder - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardChore.setOnClickListener {
            Toast.makeText(this, "🧹 Chore Manager - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardUtility.setOnClickListener {
            Toast.makeText(this, "💡 Utility Bill Tracker - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        // SAFETY
        cardEmergencyContacts.setOnClickListener {
            Toast.makeText(this, "📞 Emergency Contact List - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardEmergencyPrep.setOnClickListener {
            Toast.makeText(this, "⚠️ Emergency Preparedness - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardDisaster.setOnClickListener {
            Toast.makeText(this, "🌪️ Disaster Checklist - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardFirstAid.setOnClickListener {
            Toast.makeText(this, "🩹 First Aid Guide - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardLocalServices.setOnClickListener {
            Toast.makeText(this, "📍 Local Services Directory - Coming Soon!", Toast.LENGTH_SHORT).show()
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