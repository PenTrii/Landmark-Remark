package com.hoaison.landmarkremark

import android.content.Context
import com.hoaison.landmarkremark.common.PrefManager
import com.hoaison.landmarkremark.usecase.UseCase
import com.hoaison.landmarkremark.usecase.UseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideUseCase(): UseCase {
        return UseCaseImpl()
    }

    @Singleton
    @Provides
    fun provideAppSharedPreferences(@ApplicationContext context: Context): PrefManager {
        return PrefManager(context)
    }
}