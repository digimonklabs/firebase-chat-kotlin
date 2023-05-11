package com.firebase.chat.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.firebase.chat.navigation.NavigationCommand

abstract class BaseFragment<BINDING : ViewDataBinding, VM : BaseViewModel> : Fragment() {

    @get:LayoutRes
    protected abstract val layoutId: Int
    protected abstract val viewModel: VM
    protected lateinit var binding: BINDING
    lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    private var persistingView: View? = null

    private fun persistingView(view: View): View {
        val root = persistingView
        if (root == null) {
            persistingView = view
            return view
        } else {
            (root.parent as? ViewGroup)?.removeView(root)
            return root
        }
    }

    abstract fun setVariable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val p = if (persistingView == null) {
            onCreatePersistentView(inflater, container, savedInstanceState)
        } else {
            persistingView // prevent inflating
        }
        if (p != null) {
            return persistingView(p)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    protected open fun onCreatePersistentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            layoutId,
            container,
            false
        )

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
//            setVariable(BR.viewModel, viewModel)
        }
        setVariable()
        return binding.root
    }

    protected open fun onPersistentViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (persistingView != null) {
            onPersistentViewCreated(persistingView!!, savedInstanceState)
        }
        observeNavigation()
    }

    private fun observeNavigation() {
        viewModel.navigation.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { navigationCommand ->
                handleNavigation(navigationCommand)
            }
        }
    }

    private fun handleNavigation(navCommand: NavigationCommand) {
        when (navCommand) {
            is NavigationCommand.ToDirection -> findNavController().navigate(navCommand.directions)
            is NavigationCommand.Back -> findNavController().navigateUp()
        }
    }

}