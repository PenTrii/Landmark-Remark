package com.hoaison.landmarkremark.ui.home.fragment.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hoaison.landmarkremark.R
import com.hoaison.landmarkremark.base.BaseFragment
import com.hoaison.landmarkremark.databinding.FragmentHomeBinding
import com.hoaison.landmarkremark.model.Address

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(), OnMapReadyCallback {

    companion object {
        const val TAG = "HomeFragment"
        const val MAP_LATITUDE = "LATITUDE"
        const val MAP_LONGITUDE = "MAP_LONGITUDE"
        const val MAP_ADDRESS = "MAP_ADDRESS"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    private var mMapFragment: SupportMapFragment? = null
    private lateinit var mMap: GoogleMap
    private var mAddressList = mutableListOf<Address>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun getViewModelClass(): Class<HomeViewModel> = HomeViewModel::class.java

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding  = FragmentHomeBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        setupPermission()
        initMap()

        viewModel.getListAddress()
        viewModel.mAddressList.observe(viewLifecycleOwner) {dataList ->
            Log.d(TAG, dataList.toString())
            mAddressList.clear()
            mAddressList.addAll(dataList.orEmpty())
            dataList.forEach() {
                if (it.latitude != null && it.longitude !=  null) {
                    val location = LatLng(it.latitude!!, it.longitude!!)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13f))
                    mMap.addMarker(MarkerOptions()
                        .title(it.title)
                        .snippet(it.description)
                        .position(location)
                    )
                }
            }
        }

        binding.btnCurrentLocation.setOnClickListener {
            getCurrentLocation()
        }
    }

    private fun setupPermission() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your location-related tasks
            } else {
                // Permission denied, handle the case accordingly
                // For example, you can show a message to the user
            }
        }
    }

    private fun initMap() {
        Log.d(TAG, "init Map")

        if (mMapFragment == null) {
            mMapFragment = childFragmentManager.findFragmentById(
                R.id.map
            ) as SupportMapFragment
            mMapFragment?.getMapAsync(this)
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        this.mMap = p0
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setMinZoomPreference(13f)

        val googlePlayStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext())
        if (googlePlayStatus != ConnectionResult.SUCCESS) {

        } else {
            mMap.uiSettings.isMyLocationButtonEnabled = true
            mMap.uiSettings.setAllGesturesEnabled(true)
        }
        getCurrentLocation()
        mMap.setOnMapClickListener {
            val latitude = it.latitude
            val longitude = it.longitude
            navigateMarkerFragment(latitude, longitude)
        }

        mMap.setOnMarkerClickListener {
            val positionMarker = it.position
            val address = mAddressList.firstOrNull { it2 ->
                it2.latitude == positionMarker.latitude &&
                it2.longitude == positionMarker.longitude
            }
            if (address != null) {
                navigateMarkerFragmentIfAddressNotNull(address)
            }
            return@setOnMarkerClickListener false
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val point = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 16f))
                }
            }
    }

    private fun navigateMarkerFragment(latitude: Double, longitude: Double) {
        val bundle = bundleOf()
        bundle.putDouble(MAP_LATITUDE, latitude)
        bundle.putDouble(MAP_LONGITUDE, longitude)
        findNavController().navigate(R.id.markerFragment, bundle)
    }

    private fun navigateMarkerFragmentIfAddressNotNull(address: Address) {
        val bundle = bundleOf()
        bundle.putSerializable(MAP_ADDRESS, address)
        findNavController().navigate(R.id.markerFragment, bundle)
    }

    override fun onResume() {
        super.onResume()
    }
}