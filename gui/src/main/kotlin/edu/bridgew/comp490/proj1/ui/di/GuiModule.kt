package edu.bridgew.comp490.proj1.ui.di

import edu.bridgew.comp490.proj1.di.sharedModule
import edu.bridgew.comp490.proj1.ui.screenmodel.JobListScreenModel
import org.koin.dsl.module

val guiModule = module {
    includes(sharedModule)
    factory { params -> JobListScreenModel(get(parameters = { params })) }
}
