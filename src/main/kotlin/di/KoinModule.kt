package com.example.di

import com.example.data.repository.*
import org.koin.dsl.module

// Este es nuestro módulo principal de la aplicación
val appModule = module {
    // Definimos que cada vez que alguien pida un 'ProductRepository',
    // Koin debe proporcionar una única instancia (singleton) de 'ProductRepositoryImpl'.
    single<ProductRepository> { ProductRepositoryImpl() }

    // Hacemos lo mismo para el resto de nuestros repositorios
    single<CategoryRepository> { CategoryRepositoryImpl() }
    single<UserRepository> { UserRepositoryImpl() }

    // Asumiendo que ya tienes las implementaciones del carrito y pedidos
    single<CartRepository> { CartRepositoryImpl() }
    single<OrderRepository> { OrderRepositoryImpl() }
}