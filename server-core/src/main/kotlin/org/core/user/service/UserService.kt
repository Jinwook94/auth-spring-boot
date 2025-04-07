package org.core.user.service

import org.core.auth.domain.OAuthProvider
import org.core.user.domain.User
import org.core.user.domain.UserRole
import org.core.user.repository.UserRepository
import org.core.util.StringUtil.extractUsernameFromEmail
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val userRepository: UserRepository) {

	@Transactional
	fun findByEmailOrCreate(oauthProvider: OAuthProvider, email: String): User {
		return userRepository.findByEmail(email)
			?: userRepository.save(
				User.create(
					oauthProvider = oauthProvider,
					email = email,
					nickname = extractUsernameFromEmail(email),
					profileImage = null,
					role = UserRole.USER,
				)
			)
	}

	@Transactional
	fun incrementLoginCount(user: User): User {
		user.loginCount++
		return userRepository.save(user)
	}

}
