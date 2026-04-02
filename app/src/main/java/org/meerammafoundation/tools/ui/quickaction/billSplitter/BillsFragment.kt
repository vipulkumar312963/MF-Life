package org.meerammafoundation.tools.ui.quickaction.billSplitter

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.meerammafoundation.tools.R
import kotlin.math.min

class BillsFragment : Fragment() {

    private lateinit var viewModel: BillSplitterViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BillAdapter
    private lateinit var btnSettleUp: Button
    private var groupId: Long = 0
    private val memberNames = mutableMapOf<Long, String>()
    private val memberList = mutableListOf<Member>()
    private val billSelectedCount = mutableMapOf<Long, Int>()
    private var currentBills = listOf<Bill>()

    // Track which bills we've already observed
    private val observedBillIds = mutableSetOf<Long>()

    // Store LiveData references to remove them later
    private val shareLiveDataMap = mutableMapOf<Long, LiveData<BillWithShares>>()

    companion object {
        private const val TAG = "BillsFragment"

        fun newInstance(groupId: Long): BillsFragment {
            val fragment = BillsFragment()
            val args = Bundle()
            args.putLong("groupId", groupId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getLong("groupId", -1)
            Log.d(TAG, "onCreate - Group ID: $groupId")
        }
        viewModel = ViewModelProvider(requireActivity())[BillSplitterViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView - Creating view for groupId: $groupId")

        val view = inflater.inflate(R.layout.billsplitter_fragment_bills, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewBills)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Create adapter ONCE with empty list
        adapter = BillAdapter(
            emptyList(),
            { memberId -> memberNames[memberId] ?: "Unknown" },
            { bill -> billSelectedCount[bill.id] ?: memberList.size },
            { bill -> showEditBillDialog(bill) },
            { bill -> showDeleteBillDialog(bill) }
        )
        recyclerView.adapter = adapter

        btnSettleUp = view.findViewById(R.id.btnSettleUp)

        viewModel.getMembers(groupId).observe(viewLifecycleOwner) { members ->
            Log.d(TAG, "Received ${members.size} members")
            memberList.clear()
            memberList.addAll(members)
            memberNames.clear()
            members.forEach { memberNames[it.id] = it.name }
        }

        // Safe observer management with LiveData storage
        viewModel.getBills(groupId).observe(viewLifecycleOwner) { bills ->
            Log.d(TAG, "Received ${bills.size} bills")
            currentBills = bills

            // Clear the count map
            billSelectedCount.clear()

            // Update adapter with new list
            adapter.updateList(bills)

            // Track current bill IDs to detect removed bills
            val currentBillIds = bills.map { it.id }.toSet()

            // Remove observers for bills that no longer exist
            observedBillIds.removeAll { billId ->
                if (billId !in currentBillIds) {
                    Log.d(TAG, "Removing observer for deleted bill: $billId")

                    // Remove LiveData observer
                    shareLiveDataMap[billId]?.removeObservers(viewLifecycleOwner)
                    shareLiveDataMap.remove(billId)

                    true
                } else {
                    false
                }
            }

            // Add/update observers for current bills
            bills.forEach { bill ->
                val liveData = viewModel.getBillWithShares(bill.id)

                // Only add observer if not already observing
                if (bill.id !in observedBillIds) {
                    Log.d(TAG, "Adding observer for bill: ${bill.id}")

                    // Store LiveData reference
                    shareLiveDataMap[bill.id] = liveData

                    liveData.observe(viewLifecycleOwner) { billWithShares ->
                        val previousCount = billSelectedCount[bill.id]
                        val newCount = billWithShares.shares.size

                        if (previousCount != newCount) {
                            billSelectedCount[bill.id] = newCount
                            adapter.notifyDataSetChanged()
                            Log.d(TAG, "Bill ${bill.id} share count updated: $newCount")
                        }
                    }

                    observedBillIds.add(bill.id)
                }
            }
        }

        btnSettleUp.setOnClickListener {
            showSettleUpDialog()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        setupFabClickListener()
    }

    // ⚠️ Improvement 2: Cleaner onDestroyView
    override fun onDestroyView() {
        super.onDestroyView()

        // Remove all LiveData observers safely
        shareLiveDataMap.values.forEach { liveData ->
            liveData.removeObservers(viewLifecycleOwner)
        }

        shareLiveDataMap.clear()
        observedBillIds.clear()

        Log.d(TAG, "onDestroyView - All observers cleaned up")
    }

    private fun setupFabClickListener() {
        try {
            // ✅ FIXED: Get parent fragment (GroupDetailFragment) instead of activity
            val parentFragment = parentFragment as? GroupDetailFragment

            if (parentFragment != null) {
                parentFragment.setFabClickListener {
                    Log.d(TAG, "FAB clicked in Bills tab")
                    showAddBillDialog(null)
                }
                parentFragment.showFab()
                Log.d(TAG, "FAB listener set in Bills tab")
            } else {
                Log.e(TAG, "Parent fragment is not GroupDetailFragment")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting FAB listener", e)
        }
    }

    private fun showAddBillDialog(billToEdit: Bill?) {
        if (memberList.isEmpty()) {
            Toast.makeText(requireContext(), "Please add members first", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.billsplitter_dialog_add_bill, null)
        val etDescription = dialogView.findViewById<EditText>(R.id.etBillDescription)
        val etAmount = dialogView.findViewById<EditText>(R.id.etBillAmount)
        val spinnerPaidBy = dialogView.findViewById<Spinner>(R.id.spinnerPaidBy)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroupSplit)
        val radioEqual = dialogView.findViewById<RadioButton>(R.id.radioEqual)
        val radioCustom = dialogView.findViewById<RadioButton>(R.id.radioCustom)
        val layoutCustomSplit = dialogView.findViewById<LinearLayout>(R.id.layoutCustomSplit)
        val containerCustomSplit = dialogView.findViewById<LinearLayout>(R.id.containerCustomSplit)
        val layoutMemberSelection = dialogView.findViewById<LinearLayout>(R.id.layoutMemberSelection)
        val containerMemberCheckboxes = dialogView.findViewById<LinearLayout>(R.id.containerMemberCheckboxes)
        val customSplitAmounts = mutableMapOf<Long, Double>()
        val selectedMemberIds = mutableSetOf<Long>()

        if (billToEdit != null) {
            // Check if this is a custom split bill
            val isCustomSplit = billToEdit.splitType == SplitType.CUSTOM

            // ✅ Store LiveData reference to prevent memory leak
            val liveData = viewModel.getBillWithShares(billToEdit.id)

            // Remove any existing observer first
            shareLiveDataMap[billToEdit.id]?.removeObservers(viewLifecycleOwner)

            // Store the new LiveData reference
            shareLiveDataMap[billToEdit.id] = liveData

            liveData.observe(viewLifecycleOwner) { billWithShares ->
                selectedMemberIds.clear()
                customSplitAmounts.clear()

                billWithShares.shares.forEach { share ->
                    selectedMemberIds.add(share.memberId)
                    customSplitAmounts[share.memberId] = share.shareAmount
                }

                // Update checkboxes for equal split
                for (i in 0 until containerMemberCheckboxes.childCount) {
                    val row = containerMemberCheckboxes.getChildAt(i)
                    val cbInclude = row.findViewById<CheckBox>(R.id.cbIncludeMember)
                    val memberId = cbInclude.tag as? Long
                    if (memberId != null) {
                        cbInclude.isChecked = selectedMemberIds.contains(memberId)
                    }
                }

                // ✅ FIX: For custom split, populate fields after creating them
                if (isCustomSplit) {
                    // First, ensure custom split fields are created
                    containerCustomSplit.removeAllViews()
                    val inflater = LayoutInflater.from(requireContext())

                    memberList.forEach { member ->
                        val row = inflater.inflate(R.layout.billsplitter_item_custom_split, containerCustomSplit, false)
                        val tvName = row.findViewById<TextView>(R.id.tvMemberName)
                        val etShare = row.findViewById<EditText>(R.id.etShareAmount)

                        tvName.text = member.name

                        val amount = customSplitAmounts[member.id] ?: 0.0
                        if (amount > 0) {
                            etShare.setText(amount.toString())
                        } else {
                            etShare.text.clear()
                        }

                        containerCustomSplit.addView(row)
                    }
                }
            }

            // Set basic fields
            etDescription.setText(billToEdit.description)
            etAmount.setText(billToEdit.amount.toString())

            // Set payer spinner
            val payerIndex = memberList.indexOfFirst { it.id == billToEdit.paidById }
            if (payerIndex >= 0) {
                spinnerPaidBy.setSelection(payerIndex)
            }

            // ✅ FIX: Set split type AND immediately update visibility
            if (billToEdit.splitType == SplitType.EQUAL) {
                radioEqual.isChecked = true
                layoutCustomSplit.visibility = View.GONE
                layoutMemberSelection.visibility = View.VISIBLE
            } else {
                radioCustom.isChecked = true
                layoutCustomSplit.visibility = View.VISIBLE
                layoutMemberSelection.visibility = View.GONE

                // ✅ IMPORTANT: Explicitly create custom split fields
                containerCustomSplit.removeAllViews()
                val inflater = LayoutInflater.from(requireContext())

                memberList.forEach { member ->
                    val row = inflater.inflate(R.layout.billsplitter_item_custom_split, containerCustomSplit, false)
                    val tvName = row.findViewById<TextView>(R.id.tvMemberName)
                    val etShare = row.findViewById<EditText>(R.id.etShareAmount)

                    tvName.text = member.name
                    // Amounts will be filled when LiveData loads
                    etShare.text.clear()

                    containerCustomSplit.addView(row)
                }
            }
        }

        val memberNamesList = memberList.map { it.name }

        val spinnerAdapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            memberNamesList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(resources.getColor(R.color.text_primary, null))
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(resources.getColor(R.color.text_primary, null))
                view.setBackgroundColor(resources.getColor(R.color.card_background, null))
                return view
            }
        }
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPaidBy.adapter = spinnerAdapter

        val checkboxes = mutableListOf<CheckBox>()
        containerMemberCheckboxes.removeAllViews()

        memberList.forEach { member ->
            val itemView = layoutInflater.inflate(R.layout.billsplitter_item_member_checkbox, containerMemberCheckboxes, false)
            val tvIcon = itemView.findViewById<TextView>(R.id.tvMemberIcon)
            val tvName = itemView.findViewById<TextView>(R.id.tvMemberName)
            val cbInclude = itemView.findViewById<CheckBox>(R.id.cbIncludeMember)

            tvIcon.text = member.name.take(1).uppercase()
            tvName.text = member.name
            cbInclude.tag = member.id
            cbInclude.isChecked = true

            containerMemberCheckboxes.addView(itemView)
            checkboxes.add(cbInclude)
        }


        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioEqual) {
                layoutCustomSplit.visibility = View.GONE
                layoutMemberSelection.visibility = View.VISIBLE
                // Clear custom split fields when switching to equal split
                for (i in 0 until containerCustomSplit.childCount) {
                    val row = containerCustomSplit.getChildAt(i)
                    val etShare = row.findViewById<EditText>(R.id.etShareAmount)
                    etShare.text.clear()
                }
            } else {
                layoutCustomSplit.visibility = View.VISIBLE
                layoutMemberSelection.visibility = View.GONE

                // ✅ Create custom split fields
                containerCustomSplit.removeAllViews()
                val inflater = LayoutInflater.from(requireContext())

                memberList.forEach { member ->
                    val row = inflater.inflate(R.layout.billsplitter_item_custom_split, containerCustomSplit, false)
                    val tvName = row.findViewById<TextView>(R.id.tvMemberName)
                    val etShare = row.findViewById<EditText>(R.id.etShareAmount)

                    tvName.text = member.name
                    containerCustomSplit.addView(row)
                }

                // ✅ If editing and we have custom split amounts, fill them
                if (billToEdit != null && billToEdit.splitType == SplitType.CUSTOM && customSplitAmounts.isNotEmpty()) {
                    for (i in 0 until containerCustomSplit.childCount) {
                        val row = containerCustomSplit.getChildAt(i)
                        val etShare = row.findViewById<EditText>(R.id.etShareAmount)
                        val memberId = memberList[i].id
                        val amount = customSplitAmounts[memberId] ?: 0.0

                        if (amount > 0) {
                            etShare.setText(amount.toString())
                        } else {
                            etShare.text.clear()
                        }
                    }
                }
            }
        }

        val dialogTitle = if (billToEdit == null) "Add Bill" else "Edit Bill"
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton(if (billToEdit == null) "Add" else "Update", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val description = etDescription.text.toString().trim()
                val amountStr = etAmount.text.toString().trim()
                val selectedMemberIndex = spinnerPaidBy.selectedItemPosition

                if (description.isEmpty()) {
                    etDescription.error = "Description required"
                    return@setOnClickListener
                }
                if (amountStr.isEmpty()) {
                    etAmount.error = "Amount required"
                    return@setOnClickListener
                }
                val amount = amountStr.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    etAmount.error = "Invalid amount"
                    return@setOnClickListener
                }
                if (selectedMemberIndex < 0 || selectedMemberIndex >= memberList.size) {
                    Toast.makeText(requireContext(), "Select a payer", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val paidById = memberList[selectedMemberIndex].id

                val splitType = if (radioEqual.isChecked) SplitType.EQUAL else SplitType.CUSTOM

                if (splitType == SplitType.CUSTOM) {
                    val shares = mutableListOf<Pair<Long, Double>>()
                    var totalShare = 0.0

                    for (i in 0 until containerCustomSplit.childCount) {
                        val row = containerCustomSplit.getChildAt(i)
                        val etShare = row.findViewById<EditText>(R.id.etShareAmount)
                        val shareStr = etShare.text.toString().trim()

                        // ✅ Treat blank as 0
                        val share = if (shareStr.isEmpty()) {
                            0.0
                        } else {
                            shareStr.toDoubleOrNull() ?: 0.0
                        }

                        // ✅ Allow zero or positive amounts (no negative)
                        if (share < 0) {
                            etShare.error = "Amount cannot be negative"
                            return@setOnClickListener
                        }

                        shares.add(Pair(memberList[i].id, share))
                        totalShare += share
                    }

                    // ✅ Check if total matches bill amount
                    if (Math.abs(totalShare - amount) > 0.01) {
                        Toast.makeText(
                            requireContext(),
                            "Total shares must sum to ₹$amount\nCurrent total: ₹${String.format("%.2f", totalShare)}",
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }

                    if (billToEdit == null) {
                        viewModel.addBill(groupId, description, amount, paidById, splitType, shares)
                    } else {
                        val updatedBill = billToEdit.copy(
                            description = description,
                            amount = amount,
                            paidById = paidById,
                            splitType = splitType
                        )
                        viewModel.updateBill(updatedBill, shares)
                    }

                } else {
                    val selectedMembers = checkboxes.filter { it.isChecked }.map { it.tag as Long }

                    if (selectedMembers.isEmpty()) {
                        Toast.makeText(requireContext(), "Select at least one member", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val shareAmount = amount / selectedMembers.size
                    val shares = selectedMembers.map { memberId ->
                        Pair(memberId, shareAmount)
                    }

                    if (billToEdit == null) {
                        viewModel.addBill(groupId, description, amount, paidById, splitType, shares)
                    } else {
                        val updatedBill = billToEdit.copy(
                            description = description,
                            amount = amount,
                            paidById = paidById,
                            splitType = splitType
                        )
                        viewModel.updateBill(updatedBill, shares)
                    }
                }

                dialog.dismiss()
                Toast.makeText(requireContext(), if (billToEdit == null) "Bill added" else "Bill updated", Toast.LENGTH_SHORT).show()
            }
        }

        // ⚠️ Improvement 1: Remove observer when dialog is dismissed
        dialog.setOnDismissListener {
            if (billToEdit != null) {
                shareLiveDataMap[billToEdit.id]?.removeObservers(viewLifecycleOwner)
                shareLiveDataMap.remove(billToEdit.id)
                observedBillIds.remove(billToEdit.id)
            }
        }

        dialog.show()
    }

    private fun showEditBillDialog(bill: Bill) {
        showAddBillDialog(bill)
    }

    private fun showDeleteBillDialog(bill: Bill) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Bill")
            .setMessage("Are you sure you want to delete this bill?")
            .setPositiveButton("Delete") { _, _ ->
                // Remove from observed set and LiveData map immediately
                observedBillIds.remove(bill.id)
                shareLiveDataMap[bill.id]?.removeObservers(viewLifecycleOwner)
                shareLiveDataMap.remove(bill.id)

                viewModel.deleteBill(bill)
                Toast.makeText(requireContext(), "Bill deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSettleUpDialog() {
        viewModel.getBalances(groupId).observe(viewLifecycleOwner) { balances ->
            val positiveBalances = balances.filter { it.value > 0 }
            val negativeBalances = balances.filter { it.value < 0 }

            if (positiveBalances.isEmpty() || negativeBalances.isEmpty()) {
                Toast.makeText(requireContext(), "Everyone is settled up!", Toast.LENGTH_SHORT).show()
                return@observe
            }

            val message = buildString {
                append("Settlement Summary:\n\n")
                negativeBalances.forEach { (memberId, amount) ->
                    val owes = memberNames[memberId] ?: "Unknown"
                    positiveBalances.forEach { (creditorId, creditAmount) ->
                        val paidTo = memberNames[creditorId] ?: "Unknown"
                        val settleAmount = min(-amount, creditAmount)
                        if (settleAmount > 0) {
                            append("• $owes owes $paidTo: ₹${String.format("%.2f", settleAmount)}\n")
                        }
                    }
                }
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Settle Up")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun setupCustomSplitFields(container: LinearLayout, existingAmounts: Map<Long, Double> = emptyMap()) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        memberList.forEach { member ->
            val row = inflater.inflate(R.layout.billsplitter_item_custom_split, container, false)
            val tvName = row.findViewById<TextView>(R.id.tvMemberName)
            val etShare = row.findViewById<EditText>(R.id.etShareAmount)

            tvName.text = member.name

            // Pre-fill if editing and amount exists
            val existingAmount = existingAmounts[member.id]
            if (existingAmount != null && existingAmount > 0) {
                etShare.setText(existingAmount.toString())
            } else {
                etShare.text.clear()
            }

            container.addView(row)
        }
    }
}