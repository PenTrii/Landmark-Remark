package com.hoaison.landmarkremark.ui.home.fragment.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.hoaison.landmarkremark.base.BaseFragment
import com.hoaison.landmarkremark.common.PrefManager
import com.hoaison.landmarkremark.databinding.FragmentUserBinding
import com.hoaison.landmarkremark.model.User
import com.hoaison.landmarkremark.ui.LoginActivity
import com.hoaison.landmarkremark.util.setTextIfEmpty
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserFragment : BaseFragment<FragmentUserBinding, UserViewModel>() {

    @Inject
    lateinit var mPrefManager: PrefManager
    private var mUser: User? = null

    private lateinit var auth : FirebaseAuth
    override fun getViewModelClass(): Class<UserViewModel> = UserViewModel::class.java

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUserBinding = FragmentUserBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        mUser = mPrefManager.user

        binding.tvEmail.setTextIfEmpty(mUser?.name)
        binding.btnSignOut.setOnClickListener {
            auth.signOut()
            mPrefManager.clear()
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }
    }
}