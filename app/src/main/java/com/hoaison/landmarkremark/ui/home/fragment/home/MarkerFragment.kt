package com.hoaison.landmarkremark.ui.home.fragment.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.hoaison.landmarkremark.base.BaseFragment
import com.hoaison.landmarkremark.databinding.FragmentHomeBinding
import com.hoaison.landmarkremark.databinding.FragmentMarkerBinding
import com.hoaison.landmarkremark.model.Address
import com.hoaison.landmarkremark.ui.home.fragment.home.adapter.ImageAdapter
import com.hoaison.landmarkremark.usecase.UseCase
import com.hoaison.landmarkremark.util.setTextIfEmpty
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MarkerFragment : BaseFragment<FragmentMarkerBinding, HomeViewModel>() {

    companion object {
        const val TAG = "MarkerFragment"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    override fun getViewModelClass(): Class<HomeViewModel> = HomeViewModel::class.java

    private lateinit var mImageAdapter: ImageAdapter
    private var mPhotoLinkList = mutableListOf<String>()
    private var mCurrentImage: String? = null
    private var mLatitude: Double? = null
    private var mLongitude: Double? = null
    private var mAddress: Address? = null

    @Inject lateinit var mUseCase: UseCase

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMarkerBinding = FragmentMarkerBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.mUseCase = mUseCase

        arguments.let {
            val latitude = it?.getDouble(HomeFragment.MAP_LATITUDE)
            mLatitude = latitude

            val longitude = it?.getDouble(HomeFragment.MAP_LONGITUDE)
            mLongitude = longitude

            val address = it?.getSerializable(HomeFragment.MAP_ADDRESS) as? Address
            mAddress = address
        }

        setupViews()
    }

    private fun setupViews() {
        if (mAddress == null) {
            mAddress = Address()
        }
        else {
            binding.noteTitle.setTextIfEmpty(mAddress?.title)
            binding.noteDes.setTextIfEmpty(mAddress?.description)
            mPhotoLinkList.clear()
            mPhotoLinkList.addAll(mAddress?.images.orEmpty())
        }

        binding.btnSave.setOnClickListener {
            mAddress?.title = binding.noteTitle.text.toString()
            mAddress?.description = binding.noteDes.text.toString()
            mAddress?.latitude = mLatitude
            mAddress?.longitude = mLongitude
            if (mAddress?.id != null) {
                viewModel.updateNoteWithAddress(mAddress,
                    onSuccess = {
                        findNavController().popBackStack()
                    }
                )
            } else {
                viewModel.createNoteWithAddress(mAddress,
                    onSuccess = {
                        findNavController().popBackStack()
                    }, onError = {

                    }
                )
            }
        }

        binding.btnDelete.setOnClickListener {
            if (mAddress?.id != null) {
                viewModel.deleteNoteWithAddress(mAddress,
                    onSuccess = {
                        findNavController().popBackStack()
                    })
            }
        }

        binding.rvImage.let {
            mImageAdapter = ImageAdapter(requireContext(),
                object: ImageAdapter.OnEnvImageListener {
                    override fun onAddImage() {
                        mCurrentImage  = null
                        captureImage()
                    }

                    override fun onUpdateDataList() {
                    }
                })
            it.adapter = mImageAdapter
            it.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            // load list of image
            mImageAdapter.setItems(mPhotoLinkList)
        }
    }

    fun captureImage() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
            )
        }
        else {
            getMultiImageIntent().apply {
                mMultiImageLauncher.launch(this)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMultiImageIntent().apply {
                    mMultiImageLauncher.launch(this)
                }
            } else {
                // Permission denied, handle the case accordingly
                // For example, you can show a message to the user
            }
        }
    }

    private fun getMultiImageIntent(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        return intent
    }

    private val mMultiImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        handleMultiImageActivityResult(it)
    }

    private fun handleMultiImageActivityResult(it: ActivityResult) {
        if (it.resultCode == Activity.RESULT_OK) {
            try {
                val dataList = mutableListOf<Uri>()
                val itemCount = it.data?.clipData?.itemCount ?: 0
                if (itemCount > 0) {
                    for (i in 0 until itemCount) {
                        val item = it.data?.clipData?.getItemAt(i)
                        if (item != null) {
                            dataList.add(item.uri)
                        }
                    }
                } else {
                    val uri = it.data?.data
                    if (uri != null) {
                        dataList.add(uri)
                    }
                }
                Log.d(TAG,"compressAndUploadPhotoSync: input ${dataList.size}")
                if (dataList.isNotEmpty()) {
                    viewModel.compressAndUploadPhotoSync(dataList) { it2 ->
                        mAddress?.images = it2
                        updatePhotoLink(it2)
                    }
                }
            } catch (e: Throwable) {
                Log.d(TAG, "compressAndUploadPhotoSync: ${e.message}")
            }
        }
    }

    private fun updatePhotoLink(data: List<String>) {
        Log.d(TAG, "updatePhotoLink ${data.size}")
        if (data.isNotEmpty()) {
            if (mCurrentImage.isNullOrEmpty()) {
                mImageAdapter.addItems(data)
            } else {
                mImageAdapter.updateItems(mCurrentImage, data)
            }
        }
    }
}