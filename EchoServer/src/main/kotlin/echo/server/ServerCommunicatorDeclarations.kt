package echo.server

import java.io.Serializable
import java.rmi.Remote
import java.rmi.RemoteException
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import java.util.*

interface IServerCommunicator : Remote, Serializable {

    @get:Throws(RemoteException::class)
    val id: UUID

    @get:Throws(RemoteException::class)
    val host: String

    @get:Throws(RemoteException::class)
    val port: Int

    @get:Throws(RemoteException::class)
    val echoList: List<String>

    @Throws(RemoteException::class)
    fun register(): Int

    @Throws(RemoteException::class)
    fun recordEcho(echo: String)

}


class ServerCommunicator(
    override val id: UUID,
    override val host: String = "EchoServer",
    override val port: Int = Registry.REGISTRY_PORT
) : IServerCommunicator {
    override val echoList: List<String>
        get() = Server.echoList

    override fun register(): Int = Server.registerSlave()

    override fun recordEcho(echo: String) = Server.recordEcho(echo)
}
