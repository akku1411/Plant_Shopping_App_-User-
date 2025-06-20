package com.plantstore.app.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.annotation.NonNull
import androidx.room.ColumnInfo

@Entity(tableName = "products")
data class ProductModel(
    @PrimaryKey
    @NonNull
    val productId : String,
    @ColumnInfo(name = "productName")
    val productName : String? = "",

    @ColumnInfo(name = "productImage")
    val productImage : String? = "",

    @ColumnInfo(name = "productSp")
    val productSp : String? = "",
)
