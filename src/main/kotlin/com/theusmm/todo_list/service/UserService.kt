package com.theusmm.todo_list.service

import com.theusmm.todo_list.dto.CustomPageDto
import com.theusmm.todo_list.dto.request.UserCreateRequestDto
import com.theusmm.todo_list.dto.request.UserUpdateRequestDto
import com.theusmm.todo_list.dto.response.UserResponseDto
import com.theusmm.todo_list.entity.User
import com.theusmm.todo_list.exception.BusinessException
import com.theusmm.todo_list.exception.ResourceNotFoundException
import com.theusmm.todo_list.mapper.toCreateEntity
import com.theusmm.todo_list.mapper.toUpdateEntity
import com.theusmm.todo_list.mapper.toUserResponseDto
import com.theusmm.todo_list.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class UserService(private val repository: UserRepository) {

    @Cacheable(cacheNames = ["users"], key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    fun getAllUsers(pageable: Pageable): CustomPageDto<UserResponseDto> {
        val userPage = repository.findAll(pageable).map { it.toUserResponseDto() }
        return CustomPageDto(userPage)
    }

    fun getUserById(userId: Long): UserResponseDto {
        val user = checkIfUserExists(userId)
        val responseUser = user.toUserResponseDto()
        return responseUser
    }

    @CacheEvict(cacheNames = ["users"], allEntries = true)
    @Transactional
    fun createUser(user: UserCreateRequestDto): UserResponseDto {
        if (repository.existsByEmail(user.email)) {
            throw BusinessException("This E-mail is already registered")
        }

        val newUser = user.toCreateEntity()
        val savedUser = repository.save(newUser).toUserResponseDto()

        return savedUser
    }

    @CacheEvict(cacheNames = ["users"], allEntries = true)
    @Transactional
    fun updateUser(user: UserUpdateRequestDto, userId: Long): UserResponseDto {
        val updatedUser = checkIfUserExists(userId)
        user.toUpdateEntity(updatedUser)

        val savedUser = repository.save(updatedUser).toUserResponseDto()
        return savedUser
    }

    @CacheEvict(cacheNames = ["users"], allEntries = true)
    @Transactional
    fun deleteUser(userId: Long) {
        val user = checkIfUserExists(userId)
        repository.delete(user)
    }

    fun checkIfUserExists(userId: Long): User = repository.findById(userId)
        .orElseThrow { ResourceNotFoundException("User not found") }
}