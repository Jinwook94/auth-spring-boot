package org.core.config

import org.core.exception.FilterExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.context.SecurityContextHolderFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
	private val filterExceptionHandler: FilterExceptionHandler
) {

	@Bean
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
		return http
			.csrf { it.disable() }
			.formLogin { it.disable() }
			.httpBasic { it.disable() }
			.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
			.authorizeHttpRequests { auth ->
				auth
					.requestMatchers("/api/v1/auth/**").permitAll()
					.requestMatchers("/actuator/**").permitAll()
					.anyRequest().authenticated()
			}
			// 예외 처리 필터를 Spring Security 필터 체인의 가장 앞에 배치
			.addFilterBefore(filterExceptionHandler, SecurityContextHolderFilter::class.java)
			.build()
	}

}
