package com.example.di

import com.example.data.repository.*
import data.repository.AddressRepository
import data.repository.AddressRepositoryImpl
import data.repository.CouponRepository
import data.repository.CouponRepositoryImpl
import data.repository.QuestionRepository
import data.repository.QuestionRepositoryImpl
import data.repository.ResellerRepository
import data.repository.ResellerRepositoryImpl
import data.repository.ReviewRepository
import data.repository.ReviewRepositoryImpl
import data.repository.WishlistRepository
import data.repository.WishlistRepositoryImpl
import org.koin.dsl.module
import services.MercadoPagoService

// Este es nuestro módulo principal de la aplicación
val appModule = module {

    single<ProductRepository> { ProductRepositoryImpl() }
    single<CategoryRepository> { CategoryRepositoryImpl() }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<CartRepository> { CartRepositoryImpl() }
    single<OrderRepository> { OrderRepositoryImpl(get()) }
    single<SupplierRepository> { SupplierRepositoryImpl() }
    single<AddressRepository> { AddressRepositoryImpl() }
    single<ReviewRepository> { ReviewRepositoryImpl() }
    single<QuestionRepository> { QuestionRepositoryImpl() }
    single {
        // Reads the access token from your application.yaml configuration
        val accessToken =
            get<io.ktor.server.config.ApplicationConfig>().property("mercado_pago.accessToken").getString()
        MercadoPagoService(accessToken)
    }
    single<CouponRepository> { CouponRepositoryImpl() }
    single<WishlistRepository> { WishlistRepositoryImpl() }
    single<ResellerRepository> { ResellerRepositoryImpl(get()) }
}