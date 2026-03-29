package org.meerammafoundation.tools.culture

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.meerammafoundation.tools.AboutActivity
import org.meerammafoundation.tools.MainActivity
import org.meerammafoundation.tools.R

class CultureToolsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.culture_activity_culture_tools)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // DEVOTION category
        val cardPujaList = findViewById<CardView>(R.id.cardCulturePujaList)
        val cardChalisa = findViewById<CardView>(R.id.cardCultureChalisa)
        val cardAarti = findViewById<CardView>(R.id.cardCultureAarti)
        val cardMantra = findViewById<CardView>(R.id.cardCultureMantra)

        // FESTIVALS category
        val cardFestivalCalendar = findViewById<CardView>(R.id.cardCultureFestivalCalendar)
        val cardFestivalInfo = findViewById<CardView>(R.id.cardCultureFestivalInfo)
        val cardPujaGuide = findViewById<CardView>(R.id.cardCulturePujaGuide)

        // KNOWLEDGE category
        val cardSpiritualStories = findViewById<CardView>(R.id.cardCultureSpiritualStories)
        val cardMoralStories = findViewById<CardView>(R.id.cardCultureMoralStories)
        val cardQuotes = findViewById<CardView>(R.id.cardCultureQuotes)

        // LIFESTYLE category
        val cardYoga = findViewById<CardView>(R.id.cardCultureYoga)
        val cardMeditation = findViewById<CardView>(R.id.cardCultureMeditation)
        val cardGoodDeeds = findViewById<CardView>(R.id.cardCultureGoodDeeds)

        // Set click listeners - DEVOTION
        cardPujaList.setOnClickListener {
            Toast.makeText(this, "📿 Puja List - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardChalisa.setOnClickListener {
            Toast.makeText(this, "🕉️ Chalisa Collection - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardAarti.setOnClickListener {
            Toast.makeText(this, "🪔 Aarti Collection - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardMantra.setOnClickListener {
            Toast.makeText(this, "🔮 Mantra Collection - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        // FESTIVALS
        cardFestivalCalendar.setOnClickListener {
            Toast.makeText(this, "📅 Festival Calendar - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardFestivalInfo.setOnClickListener {
            Toast.makeText(this, "📖 Festival Information - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardPujaGuide.setOnClickListener {
            Toast.makeText(this, "🙏 Festival Puja Guide - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        // KNOWLEDGE
        cardSpiritualStories.setOnClickListener {
            Toast.makeText(this, "📿 Spiritual Stories - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardMoralStories.setOnClickListener {
            Toast.makeText(this, "📚 Moral Stories - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardQuotes.setOnClickListener {
            Toast.makeText(this, "✨ Quotes from Scriptures - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        // LIFESTYLE
        cardYoga.setOnClickListener {
            Toast.makeText(this, "🧘 Yoga Guide - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardMeditation.setOnClickListener {
            Toast.makeText(this, "🧠 Meditation Guide - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
        cardGoodDeeds.setOnClickListener {
            Toast.makeText(this, "✨ Daily Good Deeds Tracker - Coming Soon!", Toast.LENGTH_SHORT).show()
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