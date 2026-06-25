package com.theusmm.todo_list.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 50)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false, length = 30)
    var password: String,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val tasks: MutableSet<Task> = mutableSetOf()
) {
    fun addTask(task: Task) {
        tasks.add(task)
        task.user = this
    }

    fun removeTask(task: Task) {
        tasks.remove(task)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}