package org.core.user.dto

import org.core.user.domain.User
import java.util.*

class UserResponse {

	data class UserMe(
		val id: UUID,
		val name: String?,
		val email: String
	) {
		companion object {
			fun from(user: User): UserMe {
				return UserMe(
					id = user.id!!,
					name = user.nickname,
					email = user.email
				)
			}
		}
	}

}
