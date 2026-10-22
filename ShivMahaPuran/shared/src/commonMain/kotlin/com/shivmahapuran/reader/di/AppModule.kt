package com.shivmahapuran.reader.di

import com.shivmahapuran.reader.data.local.ContentSeeder
import com.shivmahapuran.reader.data.local.DatabaseDriverFactory
import com.shivmahapuran.reader.data.repository.PuranRepositoryImpl
import com.shivmahapuran.reader.data.repository.SettingsRepositoryImpl
import com.shivmahapuran.reader.data.repository.UserDataRepositoryImpl
import com.shivmahapuran.reader.db.PuranDatabase
import com.shivmahapuran.reader.domain.repository.PuranRepository
import com.shivmahapuran.reader.domain.repository.SettingsRepository
import com.shivmahapuran.reader.domain.repository.UserDataRepository
import com.shivmahapuran.reader.presentation.AppViewModel
import com.shivmahapuran.reader.presentation.BookViewModel
import com.shivmahapuran.reader.presentation.BookmarksViewModel
import com.shivmahapuran.reader.presentation.HomeViewModel
import com.shivmahapuran.reader.presentation.LibraryViewModel
import com.shivmahapuran.reader.presentation.PartDetailViewModel
import com.shivmahapuran.reader.presentation.PartsViewModel
import com.shivmahapuran.reader.presentation.ReaderViewModel
import com.shivmahapuran.reader.presentation.SearchViewModel
import com.shivmahapuran.reader.presentation.SettingsViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {
    single { PuranDatabase(get<DatabaseDriverFactory>().create()) }
    single { ContentSeeder(get()) }
    single<PuranRepository> { PuranRepositoryImpl(get(), get()) }
    single<UserDataRepository> { UserDataRepositoryImpl(get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
}

val viewModelModule = module {
    viewModel { AppViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { PartsViewModel(get(), get(), get()) }
    viewModel { params -> PartDetailViewModel(params.get(), get(), get(), get()) }
    viewModel { params -> ReaderViewModel(params[0], params[1], get(), get(), get()) }
    viewModel { BookmarksViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { LibraryViewModel(get(), get()) }
    viewModel { params -> BookViewModel(params[0], params[1], get(), get()) }
}

/** Called once per process from each platform entry point with its [platformModule]. */
fun initKoin(platformModule: Module): KoinApplication = startKoin {
    modules(platformModule, dataModule, viewModelModule)
}
