package com.theusmm.todo_list.handler

import com.theusmm.todo_list.exception.AccessDeniedException
import com.theusmm.todo_list.exception.BusinessException
import com.theusmm.todo_list.exception.ResourceNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    // not foun 404
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {

        val status = HttpStatus.NOT_FOUND

        val error = ErrorResponse(
            status = status.value(),
            error = "Not Found",
            message = ex.message
        )
        return ResponseEntity.status(status).body(error)
    }

    // Forbidden 403
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(
        ex: AccessDeniedException,
        request: HttpServletRequest) : ResponseEntity<ErrorResponse> {

        val status = HttpStatus.FORBIDDEN

        val error = ErrorResponse(
            status = status.value(),
            error = "Acceess Denied",
            message = ex.message,
            path = request.requestURI
        )
        return ResponseEntity.status(status).body(error)

    }

    // bad request 400
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        ex: BusinessException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {

        val status = HttpStatus.BAD_REQUEST

        val error = ErrorResponse(
            status = status.value(),
            error = "Bad Request",
            message = ex.message,
            path = request.requestURI
        )

        return ResponseEntity.status(status).body(error)
    }

    // Bean Validation 400
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {

        // Join DTO messages into a single string.
        val errors = ex.bindingResult.fieldErrors
            .joinToString(", ") { it.defaultMessage ?: "Invalid field." }

        val status = HttpStatus.BAD_REQUEST

        val error = ErrorResponse(
            status = status.value(),
            error = "Validation Error",
            message = errors
        )
        return ResponseEntity.status(status).body(error)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<Any> {

        val status = HttpStatus.BAD_REQUEST

        val error = ErrorResponse(
            status = status.value(),
            error = "Bad Request",
            message = "Invalid data format or invalid values for Enum fields.",
            path = request.requestURI
        )

        return ResponseEntity.status(status).body(error)
    }

    // Unexpected Errors 500
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {

        val status = HttpStatus.INTERNAL_SERVER_ERROR

        val error = ErrorResponse(
            status = status.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred."
        )
        ex.printStackTrace()
        return ResponseEntity.status(status).body(error)
    }
}