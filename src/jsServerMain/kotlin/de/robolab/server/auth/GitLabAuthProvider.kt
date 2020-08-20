package de.robolab.server.auth

class GitLabIdentityProvider(
    baseURL: String,
    redirectURL: String,
    applicationID: String,
    applicationSecret: String,
    normalScopes: String = "openid",
    adminScopes: String = "openid+readapi"
) : OIDCIdentityProvider(baseURL, redirectURL, applicationID, applicationSecret, normalScopes, adminScopes) {

}