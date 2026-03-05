package com.batodev.arrows.ui.di

import com.batodev.arrows.ui.AppViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AppViewModel(get(), get()) }
}
