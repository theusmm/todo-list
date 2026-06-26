package com.theusmm.todo_list.handler

import com.theusmm.todo_list.exception.BusinessException
import com.theusmm.todo_list.exception.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import tools.jackson.databind.exc.InvalidFormatException
import tools.jackson.databind.exc.MismatchedInputException

@RestControllerAdvice
class GlobalExceptionHandler {

    // not foun (404)
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    // business error (400)
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    // Bean Validation (400)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {

        // Join DTO messages into a single string.
        val errors = ex.bindingResult.fieldErrors
            .joinToString(", ") { it.defaultMessage ?: "Invalid field." }


        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Error",
            message = errors
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    // Unexpected Errors (500)
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred."
        )
        ex.printStackTrace()
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}