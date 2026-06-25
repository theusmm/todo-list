package com.theusmm.todo_list

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
class TodoListApplication

fun main(args: Array<String>) {
	runApplication<TodoListApplication>(*args)
}
