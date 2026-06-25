package com.theusmm.todo_list.exception

import java.lang.RuntimeException

class ResourceNotFoundException(message: String) : RuntimeException(message)

class BusinessException(message: String) : RuntimeException(message)