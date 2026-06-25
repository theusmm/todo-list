package com.theusmm.todo_list.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener::class)
class Task(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TaskStatus = TaskStatus.PENDING,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var priority: TaskPriority,

    @Column(name = "due_date")
    var scheduledDate: LocalDate?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User
) {
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null


    override fun toString(): String =
        "Task(id=$id, title='$title', status=$status, priority=$priority, scheduledDate=$scheduledDate)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Task) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}