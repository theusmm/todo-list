package com.theusmm.todo_list

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableCaching
class TodoListApplication

fun main(args: Array<String>) {
	runApplication<TodoListApplication>(*args)
}
