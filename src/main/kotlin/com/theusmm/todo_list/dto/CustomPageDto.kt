package com.theusmm.todo_list.dto

import org.springframework.data.domain.Page
import java.io.Serializable

data class CustomPageDto<T : Any>(
    val content: List<T> = emptyList(),
    val pageNumber: Int = 0,
    val pageSize: Int = 0,
    val totalElements: Long = 0L,
    val totalPages: Int = 0,
    val isLast: Boolean = false
) : Serializable {
    constructor(page: Page<T>) : this(
        content = page.content,
        pageNumber = page.number,
        pageSize = page.size,
        totalElements = page.totalElements,
        totalPages = page.totalPages,
        isLast = page.isLast
    )
}