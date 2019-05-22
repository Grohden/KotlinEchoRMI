package echo.server

import java.io.Serializable
import java.rmi.*
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import java.rmi.server.UnicastRemoteObject


data class ServerRegistry(
    val host: String,
    val port: Int
) : Serializable

interface IServer : Remote, Serializable {

    @get:Throws(RemoteException::class)
    val registryList: List<ServerRegistry>

    @get:Throws(RemoteException::class)
    val host: String

    @get:Throws(RemoteException::class)
    val port: Int


    @Throws(RemoteException::class)
    fun register(
        newHost: String,
        newHostPort: Int
    )
}


class Server(
    override val host: String,
    override val port: Int,
    private val foundList: List<ServerRegistry>? = null
) : IServer {
    override val registryList: MutableList<ServerRegistry>

    init {
        registryList = mutableListOf(ServerRegistry(host, port))
            .also { list ->
                if (foundList != null) {
                    list.addAll(foundList)

                    println("Started knowing about:")

                    foundList.forEach {
                        println(" ${it.host}:${it.port}")
                    }
                }
            }
            .distinctBy { it.host + it.port }
            .toMutableList()
    }

    override fun register(
        newHost: String,
        newHostPort: Int
    ) {
        val foundRegistry = registryList.find {
            it.host == newHost && it.port == newHostPort
        }

        if (foundRegistry == null) {
            println("Including $newHost:$newHostPort in the internal list")
            registryList.add(ServerRegistry(newHost, newHostPort))
            broadcastNewRegister(newHost, newHostPort)
        } else {
            println("$newHost:$newHostPort already included")
        }
    }

    private fun broadcastNewRegister(
        newHost: String,
        newHostPort: Int
    ) {
        registryList
            // Does not notify self
            .filter { !(it.host == host && it.port == port) }
            // Does not notify the new guy
            .filter { !(it.host == newHost && it.port == newHostPort) }
            // Hey everyone, there's a new guy around!
            .forEach {
                try {
                    println("Sending subscription ${it.host}:${it.port}")

                    val registry = LocateRegistry.getRegistry(it.port)
                    val server = registry.lookup(it.host) as IServer
                    server.register(newHost, newHostPort)

                    println("Subscription ${it.host}:${it.port} sent")
                } catch (t: Throwable) {
                    println("Error trying to replicate registry ${it.host}:${it.port}")
                }
            }

    }

}

fun findServer(): IServer? {
    return (0..10)
        .toList()
        .parallelStream()
        .map { index ->
            val port = Registry.REGISTRY_PORT + index
            println("Searching for server on port $port")

            return@map try {
                LocateRegistry
                    .getRegistry(port)
                    .lookup("EchoServer") as IServer
            } catch (e: RemoteException) {
                null
            }
        }
        .filter { it != null }
        .findFirst()
        .orElse(null)
}

fun findAvailablePort(): Int? {
    return (0..10)
        .toList()
        .parallelStream()
        .map { index ->
            val port = Registry.REGISTRY_PORT + index
            try {
                LocateRegistry
                    .getRegistry(port)
                    .lookup("EchoServer") as IServer
            } catch (e: RemoteException) {
                return@map port
            }

            return@map null
        }
        .filter { it != null }
        .findFirst()
        .orElse(null)
}

fun main() {
    val foundMaster = findServer()

    if (foundMaster == null) {
        println("Previous server not found, starting without history")

        val server = Server(
            host = "EchoServer",
            port = Registry.REGISTRY_PORT
        )

        val registry = LocateRegistry.createRegistry(server.port)
        val stub = UnicastRemoteObject.exportObject(server, server.port) as IServer
        registry.bind(server.host, stub)
    } else {
        val port = findAvailablePort()!!
        println("Previou server found at port ${foundMaster.port}")
        println("Starting with history at port $port")

        val server = Server(
            host = "EchoServer",
            port = port,
            foundList = foundMaster.registryList
        )

        val registry = LocateRegistry.createRegistry(server.port)
        val stub = UnicastRemoteObject.exportObject(server, server.port) as IServer
        registry.bind(server.host, stub)

        foundMaster.register(server.host, server.port)
    }

    //Thread.sleep(Long.MAX_VALUE)
}
