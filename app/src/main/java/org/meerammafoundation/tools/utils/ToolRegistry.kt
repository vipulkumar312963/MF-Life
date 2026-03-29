package org.meerammafoundation.tools.utils

import org.meerammafoundation.tools.budget.BudgetToolsActivity
import org.meerammafoundation.tools.culture.CultureToolsActivity
import org.meerammafoundation.tools.document.DocumentBuilderActivity
import org.meerammafoundation.tools.family.FamilyToolsActivity
import org.meerammafoundation.tools.financial.calculators.EMICalculatorActivity
import org.meerammafoundation.tools.financial.calculators.LoanEligibilityActivity
import org.meerammafoundation.tools.financial.calculators.ROICalculatorActivity
import org.meerammafoundation.tools.financial.calculators.InflationCalculatorActivity
import org.meerammafoundation.tools.financial.calculators.PFPPFCalculatorActivity
import org.meerammafoundation.tools.financial.calculators.RDFDCalculatorActivity
import org.meerammafoundation.tools.financial.calculators.MutualFundCalculatorActivity
import org.meerammafoundation.tools.financial.calculators.SIPCalculatorActivity
import org.meerammafoundation.tools.financial.calculators.RetirementPlannerActivity
import org.meerammafoundation.tools.financial.calculators.TaxCalculatorActivity
import org.meerammafoundation.tools.financial.calculators.GratuityCalculatorActivity
import org.meerammafoundation.tools.financial.calculators.NetWorthCalculatorActivity
import org.meerammafoundation.tools.health.HealthToolsActivity

object ToolRegistry {
    val allTools: List<Tool> = listOf(
        // ========== FINANCIAL TOOLS ==========
        Tool("emi_calculator", "EMI Calculator", "🏠", "Financial", EMICalculatorActivity::class.java),
        Tool("roi_calculator", "ROI Calculator", "📈", "Financial", ROICalculatorActivity::class.java),
        Tool("inflation_calculator", "Inflation Calculator", "📉", "Financial", InflationCalculatorActivity::class.java),
        Tool("loan_eligibility", "Loan Eligibility", "💳", "Financial", LoanEligibilityActivity::class.java),
        // Add these two new lines in the Financial section
        Tool("mutual_fund", "Mutual Fund Calculator", "📊", "Financial", MutualFundCalculatorActivity::class.java),
        Tool("sip", "SIP Calculator", "📈", "Financial", SIPCalculatorActivity::class.java),
        Tool("retirement_planner", "Retirement Planner", "👴", "Financial", RetirementPlannerActivity::class.java),
        Tool("tax_calculator", "Tax Calculator", "💰", "Financial", TaxCalculatorActivity::class.java),
        Tool("pf_ppf", "PF/PPF Calculator", "🏦", "Financial", PFPPFCalculatorActivity::class.java),
        Tool("rd_fd", "RD/FD Calculator", "📊", "Financial", RDFDCalculatorActivity::class.java),
        Tool("gratuity", "Gratuity Calculator", "🎁", "Financial", GratuityCalculatorActivity::class.java),
        Tool("net_worth", "Net Worth Calculator", "🏦", "Financial", NetWorthCalculatorActivity::class.java),

        // ========== BUDGET TOOLS ==========
        Tool("budget_monthly", "Monthly Budget Planner", "📅", "Budget", BudgetToolsActivity::class.java),
        Tool("budget_annual", "Annual Budget Planner", "📆", "Budget", BudgetToolsActivity::class.java),
        Tool("expense_tracker", "Expense Tracker", "🧾", "Budget", BudgetToolsActivity::class.java),
        Tool("income_vs_expense", "Income vs Expenses", "📊", "Budget", BudgetToolsActivity::class.java),
        Tool("savings_goals", "Savings Goals", "🎯", "Budget", BudgetToolsActivity::class.java),
        Tool("bill_reminder", "Bill Reminder", "⏰", "Budget", BudgetToolsActivity::class.java),
        Tool("bill_splitter", "Bill Splitter", "🧾", "Budget", BillSplitterMainActivity::class.java),
        Tool("debt_payoff", "Debt Payoff Planner", "💰", "Budget", BudgetToolsActivity::class.java),

        // ========== HEALTH TOOLS ==========
        Tool("bmi", "BMI Calculator", "⚖️", "Health", HealthToolsActivity::class.java),
        Tool("bmr", "BMR Calculator", "🔥", "Health", HealthToolsActivity::class.java),
        Tool("calorie", "Calorie Counter", "🍎", "Health", HealthToolsActivity::class.java),
        Tool("bp", "Blood Pressure Log", "❤️", "Health", HealthToolsActivity::class.java),
        Tool("sugar", "Blood Sugar Tracker", "🍬", "Health", HealthToolsActivity::class.java),
        Tool("sleep", "Sleep Tracker", "😴", "Health", HealthToolsActivity::class.java),
        Tool("health_records", "Health Record Keeper", "📋", "Health", HealthToolsActivity::class.java),
        Tool("medicine", "Medicine Reminder", "💊", "Health", HealthToolsActivity::class.java),
        Tool("vaccine", "Vaccination Reminder", "💉", "Health", HealthToolsActivity::class.java),
        Tool("period", "Period Tracker", "🗓️", "Health", HealthToolsActivity::class.java),
        Tool("pregnancy", "Pregnancy Tracker", "🤰", "Health", HealthToolsActivity::class.java),

        // ========== DOCUMENT BUILDER ==========
        Tool("resume", "Resume Builder", "📝", "Document", DocumentBuilderActivity::class.java),
        Tool("marriage_bio", "Marriage Bio Data", "💑", "Document", DocumentBuilderActivity::class.java),
        Tool("will", "Will Template", "⚖️", "Document", DocumentBuilderActivity::class.java),
        Tool("family_tree", "Family Tree Builder", "🌳", "Document", DocumentBuilderActivity::class.java),
        Tool("leave", "Leave Application", "📅", "Document", DocumentBuilderActivity::class.java),

        // ========== FAMILY TOOLS ==========
        Tool("birthday_bank", "Birthday Bank", "🎂", "Family", FamilyToolsActivity::class.java),
        Tool("anniversary", "Anniversary Reminder", "💍", "Family", FamilyToolsActivity::class.java),
        Tool("event_calendar", "Family Event Calendar", "📅", "Family", FamilyToolsActivity::class.java),
        Tool("gift_planner", "Gift Planner", "🎁", "Family", FamilyToolsActivity::class.java),
        Tool("family_blood", "Family Blood Group", "🩸", "Family", FamilyToolsActivity::class.java),
        Tool("recipe", "Recipe Keeper", "📖", "Family", FamilyToolsActivity::class.java),
        Tool("grocery", "Grocery List Builder", "🛒", "Family", FamilyToolsActivity::class.java),
        Tool("maintenance", "Maintenance Reminder", "🔧", "Family", FamilyToolsActivity::class.java),
        Tool("chore", "Chore Manager", "🧹", "Family", FamilyToolsActivity::class.java),
        Tool("utility", "Utility Bill Tracker", "💡", "Family", FamilyToolsActivity::class.java),
        Tool("emergency_contacts", "Emergency Contacts", "📞", "Family", FamilyToolsActivity::class.java),
        Tool("emergency_prep", "Emergency Preparedness", "⚠️", "Family", FamilyToolsActivity::class.java),
        Tool("disaster", "Disaster Checklist", "🌪️", "Family", FamilyToolsActivity::class.java),
        Tool("first_aid", "First Aid Guide", "🩹", "Family", FamilyToolsActivity::class.java),
        Tool("local_services", "Local Services Directory", "📍", "Family", FamilyToolsActivity::class.java),

        // ========== CULTURE TOOLS ==========
        Tool("puja_list", "Puja List", "📿", "Culture", CultureToolsActivity::class.java),
        Tool("chalisa", "Chalisa", "🕉️", "Culture", CultureToolsActivity::class.java),
        Tool("aarti", "Aarti Collection", "🪔", "Culture", CultureToolsActivity::class.java),
        Tool("mantra", "Mantra Collection", "🔮", "Culture", CultureToolsActivity::class.java),
        Tool("festival_calendar", "Festival Calendar", "📅", "Culture", CultureToolsActivity::class.java),
        Tool("festival_info", "Festival Information", "📖", "Culture", CultureToolsActivity::class.java),
        Tool("puja_guide", "Festival Puja Guide", "🙏", "Culture", CultureToolsActivity::class.java),
        Tool("spiritual_stories", "Spiritual Stories", "📿", "Culture", CultureToolsActivity::class.java),
        Tool("moral_stories", "Moral Stories", "📚", "Culture", CultureToolsActivity::class.java),
        Tool("quotes", "Quotes from Scriptures", "✨", "Culture", CultureToolsActivity::class.java),
        Tool("yoga", "Yoga Guide", "🧘", "Culture", CultureToolsActivity::class.java),
        Tool("meditation", "Meditation Guide", "🧠", "Culture", CultureToolsActivity::class.java),
        Tool("good_deeds", "Daily Good Deeds Tracker", "✨", "Culture", CultureToolsActivity::class.java)
    )

    fun getToolById(id: String): Tool? = allTools.find { it.id == id }
}