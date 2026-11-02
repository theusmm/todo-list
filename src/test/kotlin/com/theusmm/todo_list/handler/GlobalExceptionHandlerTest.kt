package com.theusmm.todo_list.handler

import com.theusmm.todo_list.exception.AccessDeniedException
import com.theusmm.todo_list.exception.BusinessException
import com.theusmm.todo_list.exception.ResourceNotFoundException
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.RequestBody
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GlobalExceptionHandlerTest {

    private fun requestAt(path: String): HttpServletRequest =
        MockHttpServletRequest().apply { requestURI = path }

    private class ValidationTarget {
        fun handle(@RequestBody body: String) = body
    }

    private val handler = GlobalExceptionHandler()

    @Test
    fun `should return 404 when resource is not found`() {
        val exception = ResourceNotFoundException("User not found")

        val response = handler.handleResourceNotFound(exception)
        val body = response.body

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNotNull(body)
        assertEquals(404, body.status)
        assertEquals("Not Found", body.error)
        assertEquals("User not found", body.message)
        assertEquals(null, body.path)
        assertNotNull(body.timestamp)
    }

    @Test
    fun `should return 403 with request path for access denied`() {
        val exception = AccessDeniedException("Access denied")
        val request = requestAt("/users/admin")
        val response = handler.handleAccessDeniedException(exception, request)
        val body = response.body

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertNotNull(body)
        assertEquals(403, body.status)
        assertEquals("Acceess Denied", body.error)
        assertEquals("Access denied", body.message)
        assertEquals("/users/admin", body.path)
        assertNotNull(body.timestamp)
    }

    @Test
    fun `should return 400 with request path for business exception`() {
        val exception = BusinessException("This E-mail is already registered!")
        val request = requestAt("/users")
        val response = handler.handleBusinessException(exception, request)
        val body = response.body

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(body)
        assertEquals(400, body.status)
        assertEquals("Bad Request", body.error)
        assertEquals("This E-mail is already registered!", body.message)
        assertEquals("/users", body.path)
        assertNotNull(body.timestamp)
    }

    @Test
    fun `should return 400 with validation messages`() {
        val target = Any()
        val bindingResult = BeanPropertyBindingResult(target, "request")
        bindingResult.addError(FieldError("request", "name", "Name is required"))
        bindingResult.addError(FieldError("request", "email", "Please provide a valid email address"))

        val method = ValidationTarget::class.java.getDeclaredMethod("handle", String::class.java)
        val methodParameter = MethodParameter(method, 0)
        val exception = MethodArgumentNotValidException(methodParameter, bindingResult)

        val response = handler.handleValidationException(exception)
        val body = response.body

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(body)
        assertEquals(400, body.status)
        assertEquals("Validation Error", body.error)
        assertEquals("Name is required, Please provide a valid email address", body.message)
        assertEquals(null, body.path)
        assertNotNull(body.timestamp)
    }

    @Test
    fun `should return 400 with request path for unreadable request body`() {
        val exception = HttpMessageNotReadableException("Invalid JSON", mockk<HttpInputMessage>())
        val request = requestAt("/tasks")
        val response = handler.handleHttpMessageNotReadable(exception, request)
        val body = response.body as ErrorResponse

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(400, body.status)
        assertEquals("Bad Request", body.error)
        assertEquals(
            "Invalid data format or invalid values for Enum fields.",
            body.message
        )
        assertEquals("/tasks", body.path)
        assertNotNull(body.timestamp)
    }

    @Test
    fun `should return generic 500 response for unexpected exception`() {
        val exception = IllegalStateException("Internal details")
        val response = handler.handleGenericException(exception)
        val body = response.body

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNotNull(body)
        assertEquals(500, body.status)
        assertEquals("Internal Server Error", body.error)
        assertEquals("An unexpected error occurred.", body.message)
        assertEquals(null, body.path)
        assertNotNull(body.timestamp)
    }
}