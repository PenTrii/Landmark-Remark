package com.hoaison.landmarkremark.usecase

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class UseCaseImpl : UseCase {

    val mStorage = Firebase.storage
    val mStorageRef = mStorage.reference

    override suspend fun uploadImageFileSync(
        request: List<Uri>,
        listener: UseCase.UseCaseListener<List<String>>
    ) {
        try {
            val ret = mutableListOf<String>()
            val tasks = mutableListOf<Task<Uri>>()

            request.forEach { uri ->
                val imageRef = mStorageRef.child("images/${uri.lastPathSegment}")
                val uploadTask = imageRef.putFile(uri)
                val downloadUrlTask = uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imageRef.downloadUrl
                }
                tasks.add(downloadUrlTask)
            }

            /* Wait for all tasks to complete */
            Tasks.whenAllComplete(tasks).addOnCompleteListener { taskList ->
                if (taskList.isSuccessful) {
                    // Retrieve download URLs from completed tasks
                    taskList.result?.forEach { result ->
                        if (result.isSuccessful) {
                            val uri = result.result as Uri
                            val imageUrl = uri.toString()
                            ret.add(imageUrl)
                        }
                    }
                    listener.onSuccess(ret)
                } else {
                    // Handle task failures
                    taskList.exception?.let {
                        listener.onError(it)
                    }
                }
            }
        } catch (t: Throwable) {
            listener.onError(t)
        }
    }
}