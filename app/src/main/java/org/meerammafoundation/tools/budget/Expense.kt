package org.meerammafoundation.tools.budget   // adjust package as needed

data class Expense(
    val date: String,
    val category: String,
    val amount: Double,
    val description: String
)