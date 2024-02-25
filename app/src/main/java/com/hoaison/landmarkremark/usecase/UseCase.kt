package com.hoaison.landmarkremark.usecase

import android.net.Uri

interface UseCase {
    suspend fun uploadImageFileSync(request: List<Uri>,
                                    listener: UseCaseListener<List<String>>)



    interface UseCaseListener<T> {
        fun onSuccess(data: T)
        fun onError(t: Throwable)
    }
}