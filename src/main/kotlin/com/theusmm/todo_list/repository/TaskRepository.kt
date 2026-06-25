package com.theusmm.todo_list.repository

import com.theusmm.todo_list.entity.Task
import org.springframework.data.jpa.repository.JpaRepository

interface TaskRepository : JpaRepository<Task, Long> {
    fun findByUserId(userId: Long) : List<Task>
}