package com.theusmm.todo_list.service

import com.theusmm.todo_list.entity.User
import com.theusmm.todo_list.dto.request.UserCreateRequestDto
import com.theusmm.todo_list.dto.request.UserUpdateRequestDto
import com.theusmm.todo_list.exception.BusinessException
import com.theusmm.todo_list.exception.ResourceNotFoundException
import com.theusmm.todo_list.mapper.toCreateEntity
import com.theusmm.todo_list.mapper.toUpdateEntity
import com.theusmm.todo_list.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class UserService(private val repository: UserRepository) {

    fun getAllUsers() = repository.findAll()

    fun getUserById(userId: Long): User = repository.findById(userId)
        .orElseThrow { ResourceNotFoundException("User not found") }

    @Transactional
    fun createUser(user: UserCreateRequestDto): User {
        if (repository.existsByEmail(user.email)) {
            throw BusinessException("This E-mail is already registered")
        }

        val newUser = user.toCreateEntity()
        return repository.save(newUser)
    }

    @Transactional
    fun updateUser(user: UserUpdateRequestDto, userId: Long): User {
        val updatedUser = getUserById(userId)
        user.toUpdateEntity(updatedUser)
        return repository.save(updatedUser)
    }

    @Transactional
    fun deleteUser(userId: Long) {
        val user = getUserById(userId)
        repository.delete(user)
    }
}