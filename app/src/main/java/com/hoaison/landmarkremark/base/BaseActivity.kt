package com.hoaison.landmarkremark.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<B: ViewBinding>: AppCompatActivity() {

    protected  lateinit var binding: B
    protected abstract fun getViewBinding(): B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindContentView()
    }

    private fun bindContentView() {
        binding = getViewBinding()
        setContentView(binding.root)
    }
}