package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ItemPedido(
    val productId: String,
    val nombreProducto: String, // Copia del nombre del producto al momento del pedido
    val cantidad: Int,
    val precioUnitario: Double // Precio unitario al que se vendió el producto
)

@Serializable
data class Pedido(
    val id: String,
    val usuarioId: String,
    val fechaPedido: Long, // Timestamp UNIX para la fecha y hora
    val estado: String, // Ej. "PROCESANDO", "ENVIADO", "ENTREGADO"
    val total: Double,
    val items: List<ItemPedido> // Lista de ítems incluidos
)