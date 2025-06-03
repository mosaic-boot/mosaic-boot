package io.mosaicboot.core.auth.oauth2

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User

class AttributedOAuth2AuthenticationToken(
    principal: OAuth2User,
    authorities: Collection<out GrantedAuthority>,
    authorizedClientRegistrationId: String,
    val attributes: Map<String, Any>,
) : OAuth2AuthenticationToken(principal, authorities, authorizedClientRegistrationId)