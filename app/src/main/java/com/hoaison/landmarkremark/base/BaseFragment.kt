package com.hoaison.landmarkremark.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import java.lang.Exception

abstract class BaseFragment<B: ViewBinding, VM: ViewModel> : Fragment() {

    protected abstract fun getViewModelClass(): Class<VM>

    protected lateinit var viewModel: VM
    protected lateinit var binding: B

    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): B

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindContentView(inflater, container)

        viewModel = activity?.run { ViewModelProvider(this)[getViewModelClass()] }
            ?: throw Exception("Invalid Activity")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun bindContentView(inflater: LayoutInflater, container: ViewGroup?) {
        binding = getViewBinding(inflater, container)
    }
}