package com.example.api.places.common.dto

data class ListResponseDto<T : Any>(val items: List<T>)
