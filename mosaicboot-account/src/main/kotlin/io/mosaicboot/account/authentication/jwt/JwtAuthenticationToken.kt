package io.mosaicboot.account.authentication.jwt

import com.nimbusds.jwt.SignedJWT
import io.mosaicboot.account.authentication.AuthorizedAuthentication
import org.springframework.security.core.GrantedAuthority

class JwtAuthenticationToken(
    val token: SignedJWT,
) : AuthorizedAuthentication {
    override fun getName(): String {
        TODO("Not yet implemented")
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        TODO("Not yet implemented")
    }

    override fun getCredentials(): Any {
        TODO("Not yet implemented")
    }

    override fun getDetails(): Any {
        TODO("Not yet implemented")
    }

    override fun getPrincipal(): Any {
        TODO("Not yet implemented")
    }

    override fun setAuthenticated(isAuthenticated: Boolean) {
        TODO("Not yet implemented")
    }
}