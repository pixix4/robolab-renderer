package de.robolab.common.net

@Suppress("unused")
enum class HttpStatusCode(val code:Int) {
    Continue(100),
    SwitchingProtocols(101),
    Processing(102),
    EarlyHints(103),


    Ok(200),
    Created(201),
    Accepted(202),
    NonAuthoritativeInformation(203),
    NoContent(204),
    ResetContent(205),
    PartialContent(206),
    MultiStatus(207),
    AlreadyReported(208),

    IMUsed(226),


    MultipleChoices(300),
    MovedPermanently(301),
    Found(302),
    SeeOther(303),
    NotModified(304),
    UseProxy(305),
    SwitchProxy(306),
    TemporaryRedirect(307),
    PermanentRedirect(308),


    BadRequest(400),
    Unauthorized(401),
    PaymentRequired(402),
    Forbidden(403),
    NotFound(404),
    MethodNotAllowed(405),
    NotAcceptable(406),
    ProxyAuthenticationRequired(407),
    RequestTimeout(408),
    Conflict(409),
    Gone(410),
    LengthRequired(411),
    PreconditionFailed(412),
    PayloadTooLarge(413),
    URITooLong(414),
    UnsupportedMediaType(415),
    RangeNotSatisfiable(416),
    ExpectationFailed(417),
    ImATeapot(418),

    MisdirectedRequest(421),
    UnprocessableEntity(422),
    Locked(423),
    FailedDependency(424),
    TooEarly(425),
    UpgradeRequired(426),
    PreconditionRequired(428),
    TooManyRequests(429),

    RequestHeaderFieldsTooLarge(431),

    LoginTimeout(440),//From Microsoft's IIS

    UnavailableForLegalReasons(451),


    InternalServerError(500),
    NotImplemented(501),
    BadGateway(502),
    ServiceUnavailable(503),
    GatewayTimeout(504),
    HTTPVersionNotSupported(505),
    VariantAlsoNegotiates(506),
    InsufficientStorage(507),
    LoopDetected(508),

    NotExtended(510),
    NetworkAuthenticationRequired(511);



    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        //Should already be sorted correctly, but just to make sure...
        val sortedCodes: List<HttpStatusCode> = values().sortedBy(HttpStatusCode::code)

        fun get(code:Int): HttpStatusCode?{
            val index:Int? = sortedCodes.binarySearchBy(code, selector = HttpStatusCode::code)
            return if(index == null || index < 0)
                values().find {it.code == code}
            else
                sortedCodes[index]
        }
    }

    override fun toString(): String {
        return "$name($code)"
    }
}