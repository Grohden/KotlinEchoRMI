package echo.server

import echo.client.interfaces.IEcho

class ClientCommunicator : IEcho {

    override fun echo(echo: String): String {
        return Server.processClientEcho(echo)
    }

    override fun getHistory(): List<String> = Server.echoList
}