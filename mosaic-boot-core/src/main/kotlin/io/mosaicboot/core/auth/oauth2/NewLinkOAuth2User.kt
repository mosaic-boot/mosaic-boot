package io.mosaicboot.core.auth.oauth2

import io.mosaicboot.core.util.WebClientInfo

class NewLinkOAuth2User(
    webClientInfo: WebClientInfo,
    basicInfo: OAuth2BasicInfo,
    val userId: String,
    val authenticationId: String,
) : TemporaryOAuth2User(webClientInfo, basicInfo)