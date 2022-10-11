package com.todolist.di

import android.app.Application
import androidx.room.Room
import com.todolist.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module

@InstallIn(SingletonComponent::class)
object Appmodule {


    @Provides
    @Singleton
    fun provideDatabase(
        app: Application,
        callback: TaskDatabase.Callback
    ) = Room.databaseBuilder(app, TaskDatabase::class.java, "task_database")
        .fallbackToDestructiveMigration()
        .build()


    @Provides
    fun provideTaskDao(db: TaskDatabase) = db.taskDao()


    @ApplicationScope
    @Provides
    @Singleton
    fun providesApplicationScope() = CoroutineScope(SupervisorJob())

}


@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope