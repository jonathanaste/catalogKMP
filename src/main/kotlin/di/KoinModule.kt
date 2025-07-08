package com.example.di

import com.example.data.repository.*
import data.repository.AddressRepository
import data.repository.AddressRepositoryImpl
import data.repository.QuestionRepository
import data.repository.QuestionRepositoryImpl
import data.repository.ReviewRepository
import data.repository.ReviewRepositoryImpl
import org.koin.dsl.module

// Este es nuestro módulo principal de la aplicación
val appModule = module {

    single<ProductRepository> { ProductRepositoryImpl() }
    single<CategoryRepository> { CategoryRepositoryImpl() }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<CartRepository> { CartRepositoryImpl() }
    single<OrderRepository> { OrderRepositoryImpl() }
    single<SupplierRepository> { SupplierRepositoryImpl() }
    single<AddressRepository> { AddressRepositoryImpl() }
    single<ReviewRepository> { ReviewRepositoryImpl() }
    single<QuestionRepository> { QuestionRepositoryImpl() }
}