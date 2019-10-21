package com.example.api.places.common.rest.response

data class ListResponseDto<T : Any>(val items: List<T>)
