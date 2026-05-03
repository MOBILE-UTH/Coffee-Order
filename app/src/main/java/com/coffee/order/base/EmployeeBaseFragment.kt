package com.coffee.order.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.coffee.order.viewmodel.AppViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class EmployeeBaseFragment<T : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T,
) : Fragment() {

    private var _binding: T? = null
    protected val binding: T get() = _binding!!

    val employeeActivity: com.coffee.order.feature.employee.EmployeeActivity get() = activity as com.coffee.order.feature.employee.EmployeeActivity
    val appViewModel: AppViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = bindingInflater.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())

            view.setPadding(
                view.paddingLeft, statusBarInsets.top, view.paddingRight, view.paddingBottom
            )

            insets
        }
        setUpEventListeners()
        collectStateAndUpdateUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    abstract fun setUpEventListeners()
    abstract fun collectStateAndUpdateUi()

    protected fun <T> collectFlow(
        flow: Flow<T>,
        action: suspend (T) -> Unit,
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect { action(it) }
            }
        }
    }
}