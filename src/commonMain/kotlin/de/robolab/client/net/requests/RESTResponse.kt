package de.robolab.client.net.requests

import de.robolab.client.net.IServerResponse
import de.robolab.client.net.ServerResponse

interface IRESTResponse : IServerResponse {

}

abstract class RESTResponse(serverResponse: IServerResponse) : IServerResponse by serverResponse, IRESTResponse  {

}