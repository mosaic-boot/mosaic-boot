/**
 * Copyright 2025 JC-Lab (mosaicboot.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mosaicboot.example.sampleapp.config

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.filter.ForwardedHeaderFilter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebSecurity
class SecurityConfig(
//    private val customOAuth2UserService: CustomOAuth2UserService,
//    private val myAuthenticationSuccessHandler: MyAuthenticationSuccessHandler,
) : WebMvcConfigurer {
    @Bean
    fun forwardedHeaderFilter(): FilterRegistrationBean<ForwardedHeaderFilter> {
        val filterRegistrationBean = FilterRegistrationBean<ForwardedHeaderFilter>()
        filterRegistrationBean.filter = ForwardedHeaderFilter()
        filterRegistrationBean.order = Ordered.HIGHEST_PRECEDENCE
        return filterRegistrationBean
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
//                    .requestMatchers("/api/auth/current").permitAll()
//                    .requestMatchers("/api/auth/**").permitAll()
//                    .requestMatchers("/api/payment/nicepay/webhook").permitAll()
//                    .requestMatchers("/api/payment/**").authenticated()
                    .anyRequest().permitAll()
            }
//            .logout { logout ->
//                logout.logoutUrl("/api/auth/logout")
//                    .addLogoutHandler(MyCookieClearingLogoutHandler())
//                    .logoutSuccessHandler(SimpleUrlLogoutSuccessHandler().apply {
//                        setTargetUrlParameter("finish_to")
//                    })
//            }
//            .oauth2Login {
//                it.authorizationEndpoint { endpoint ->
//                    endpoint.baseUri("/api/auth/oauth2/request")
//                }
//                it.redirectionEndpoint { endpoint ->
//                    endpoint.baseUri("/api/auth/oauth2/callback/*")
//                }
//                it.userInfoEndpoint { endpoint ->
//                    endpoint.userService(customOAuth2UserService)
//                }
//                it.successHandler(myAuthenticationSuccessHandler)
//            }
//            .exceptionHandling { exceptionHandling ->
//                exceptionHandling.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
//            }
        return http.build()
    }
//
//    override fun addCorsMappings(registry: CorsRegistry) {
//        registry.addMapping("/**")
//            .allowedOrigins(*AuthConfig.ALLOW_ORIGINS.toTypedArray())
//            .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*", "http://g2b.topicat.local:*")
//            .allowCredentials(true)
//    }
}
