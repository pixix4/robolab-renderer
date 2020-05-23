package de.robolab.client.net

import de.robolab.common.net.HttpMethod

class RESTRobolabServer(
    override val hostURL: String,
    override val hostPort:Int,
    override val protocol:String
) : IRobolabServer {

    constructor(hostURL: String, hostPort: Int, secure:Boolean=false):
            this(hostURL,hostPort, if(secure) "https" else "http")

    private var _credentials: ICredentialProvider? = null
    override var credentials: ICredentialProvider?
        get() {return _credentials}
        set(value){
            _credentials = value
            if(value==null)
                resetAuthSession()
        }

    override fun resetAuthSession() {

    }

    override suspend fun request(method: HttpMethod,
                                 path:String,
                                 body:String?,
                                 query: Map<String,String>,
                                 headers: Map<String, List<String>>,
                                 forceAuth:Boolean): ServerResponse{
        //TODO handleAuth, resend request if possible
        return sendHttpRequest(method,protocol,hostURL,hostPort,path,body,query, headers)
    }
}