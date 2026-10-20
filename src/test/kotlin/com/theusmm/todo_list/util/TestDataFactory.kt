package com.theusmm.todo_list.util

import com.theusmm.todo_list.dto.request.TaskCreateRequestDto
import com.theusmm.todo_list.dto.request.TaskUpdateRequestDto
import com.theusmm.todo_list.dto.request.UserCreateRequestDto
import com.theusmm.todo_list.dto.request.UserUpdateRequestDto
import com.theusmm.todo_list.entity.Task
import com.theusmm.todo_list.entity.TaskPriority
import com.theusmm.todo_list.entity.TaskStatus
import com.theusmm.todo_list.entity.User
import com.theusmm.todo_list.entity.UserRole
import java.time.LocalDate

fun createUserEntity(
    id: Long = 10L,
    name: String = "common user",
    email: String = "common@email.com",
    role: UserRole = UserRole.USER
) = User(id = id, name = name, email = email, password = "123456", role = role)

fun createTaskEntity(
    id: Long = 1L,
    title: String = "task title",
    description: String = "task description",
    priority: TaskPriority = TaskPriority.MEDIUM,
    dueDate: LocalDate = LocalDate.now().plusDays(5),
    user: User = createUserEntity()
) = Task(id = id, title = title, description = description, priority = priority, dueDate = dueDate, user = user)

fun createTaskCreateRequestDto(
    title: String = "task title",
    description: String = "task description",
    priority: TaskPriority = TaskPriority.MEDIUM,
    dueDate: LocalDate = LocalDate.now().plusDays(5)
) = TaskCreateRequestDto(title = title, description = description, priority = priority, dueDate = dueDate)

fun createTaskUpdateRequestDto(
    title: String = "new Title",
    description: String = "new description",
    status: TaskStatus = TaskStatus.IN_PROGRESS,
    priority: TaskPriority = TaskPriority.HIGH,
    dueDate: LocalDate = LocalDate.now().plusDays(4)
) = TaskUpdateRequestDto(title = title, description = description, status = status, priority = priority, dueDate = dueDate)

fun createUserCreateRequestDto(
    name: String = "user name",
    email: String = "user@email.com",
    password: String = "123456"
) = UserCreateRequestDto(name = name, email = email, password = password)

fun createUserUpdateRequestDto(
    name: String = "new name",
    password: String = "new password"
) = UserUpdateRequestDto(name = name, password = password)