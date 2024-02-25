package com.hoaison.landmarkremark.ui.home.fragment.home

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.hoaison.landmarkremark.model.Address
import com.hoaison.landmarkremark.usecase.UseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class HomeViewModel : ViewModel() {

    private val mDatabase = Firebase.database
    private val mAddressRef = mDatabase.getReference("address")
    private var mUploadFileJob: Job? = null
    var mUseCase: UseCase? = null
    private val _dataListAddress = MutableLiveData<List<Address>>()
    val mAddressList: LiveData<List<Address>> = _dataListAddress


    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    fun createNoteWithAddress(address: Address?, onSuccess: () -> Unit = {}, onError: () -> Unit = {}) {
        val addressRef = mAddressRef.push()
        if (address != null) {
            addressRef.setValue(address)
                .addOnSuccessListener {
                    println("Data saved successfully!")
                    onSuccess()
                }
                .addOnFailureListener {
                    println("Failed to save data: ${it.message}")
                    onError()
                }
        }
    }

    fun updateNoteWithAddress(address: Address?, onSuccess: () -> Unit = {}, onError: () -> Unit = {}) {
        if (address?.id != null) {
            val addressRef = mAddressRef.child(address.id!!)
            addressRef.setValue(address)
                .addOnSuccessListener {
                    println("Success to update address")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    println("Failed to update address: $e")
                    onError()
                }
        }
    }

    fun deleteNoteWithAddress(address: Address?, onSuccess: () -> Unit = {}, onError: () -> Unit = {}) {
        if (address?.id != null) {
            val addressRef = mAddressRef.child(address.id!!)
            addressRef.removeValue()
                .addOnSuccessListener {
                    println("Address deleted successfully")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    println("Failed to delete address: $e")
                    onError()
                }
        }
    }

    fun compressAndUploadPhotoSync(dataList: List<Uri>, onCompleted: (linkList: List<String>) -> Unit = {}) {
        // upload the resized image file
        mUploadFileJob = CoroutineScope(Dispatchers.IO).launch {
            mUseCase?.uploadImageFileSync(dataList,
                listener = object: UseCase.UseCaseListener<List<String>> {
                    override fun onSuccess(data: List<String>) {
                        onCompleted(data)
                    }

                    override fun onError(t: Throwable) {
                        onCompleted(listOf())
                        if (t !is CancellationException) {

                        }
                    }
                })
        }
    }

    fun getListAddress() {
        mAddressRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val addressList = mutableListOf<Address>()
                for (childSnapshot in dataSnapshot.children) {
                    val key = childSnapshot.key
                    val value = childSnapshot.getValue(Address::class.java)
                    value?.let {
                        it.id = key
                        addressList.add(it)
                    }
                }
                _dataListAddress.value = addressList
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value: ${error.toException()}")
            }
        })
    }
}