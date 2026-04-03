package com.coffee.order.fragment.management

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.coffee.order.databinding.BottomSheetAddTableBinding
import com.coffee.order.viewmodel.AppViewModel
import com.coffee.order.viewmodel.model.TableInfo

class AddTableBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddTableBinding? = null
    private val binding get() = _binding!!
    private val appViewModel: AppViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetAddTableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextTableId = getNextTableId()
        binding.textViewTableIdPreview.text = getString(
            com.coffee.order.R.string.table_id_preview,
            nextTableId.toString().padStart(2, '0')
        )

        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonSave.setOnClickListener {
            if (!validateInput()) return@setOnClickListener

            val tableName = binding.editTextTableName.text?.toString()?.trim().orEmpty()
            val capacity = binding.editTextTableCapacity.text?.toString()?.trim()?.toIntOrNull() ?: 0

            appViewModel.addTableInfo(
                TableInfo(
                    tableId = nextTableId,
                    tableName = tableName,
                    maxPeople = capacity,
                )
            )
            dismiss()
            Toast.makeText(requireContext(), "Đã thêm bàn mới", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validateInput(): Boolean {
        val name = binding.editTextTableName.text?.toString()?.trim().orEmpty()
        val capacityText = binding.editTextTableCapacity.text?.toString()?.trim().orEmpty()

        binding.textInputLayoutTableName.error = null
        binding.textInputLayoutTableCapacity.error = null

        var isValid = true
        if (name.isBlank()) {
            binding.textInputLayoutTableName.error = getString(com.coffee.order.R.string.table_name_required)
            isValid = false
        }

        val capacity = capacityText.toIntOrNull()
        if (capacityText.isBlank()) {
            binding.textInputLayoutTableCapacity.error = getString(com.coffee.order.R.string.table_capacity_required)
            isValid = false
        } else if (capacity == null || capacity <= 0) {
            binding.textInputLayoutTableCapacity.error = getString(com.coffee.order.R.string.table_capacity_invalid)
            isValid = false
        }

        return isValid
    }

    private fun getNextTableId(): Long {
        return (appViewModel.tableInfoList.value.maxOfOrNull { it.tableId } ?: 0L) + 1L
    }
}

