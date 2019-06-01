package echo.server

import echo.client.interfaces.IEcho
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import java.rmi.server.UnicastRemoteObject
import java.util.*

object Server {
    private const val MASTER_NAME = "Master"
    private const val ECHO_NAME = "Echo"
    private const val SLAVE_PREFIX = "Slave"

    private fun getRegistry() = runCatching {
        LocateRegistry.createRegistry(Registry.REGISTRY_PORT)
    }.getOrElse { LocateRegistry.getRegistry(Registry.REGISTRY_PORT) }

    private var selfId = UUID.randomUUID()

    // to try to avoid garbage collection here
    private var stubbedMaster: IServerCommunicator? = null
    private var stubbedClient: IEcho? = null

    var masterId: UUID? = null
    var echoList = mutableListOf<String>()

    private fun findMaster(): IServerCommunicator? = runCatching {
        LocateRegistry
            .getRegistry(Registry.REGISTRY_PORT)
            .lookup(MASTER_NAME) as IServerCommunicator
    }.getOrNull()

    private fun tryToAssumeMasterPlace() = try {
        Log.d { "Trying to assume master place" }

        val registry = getRegistry()
        val server = ServerCommunicator(id = selfId)
        stubbedMaster = UnicastRemoteObject.exportObject(
            server,
            Registry.REGISTRY_PORT
        ) as IServerCommunicator


        stubbedClient = UnicastRemoteObject.exportObject(
            ClientCommunicator(),
            Registry.REGISTRY_PORT
        ) as IEcho

        registry.rebind(MASTER_NAME, stubbedMaster!!)
        registry.rebind(ECHO_NAME, stubbedClient!!)

        masterId = server.id
        Log.d { "Assumed master control" }
    } catch (t: Throwable) {
        masterId = null
        Log.e { "Could not assume master place: ${t.message}" }
    }

    private fun syncWithMaster(master: IServerCommunicator) = runCatching {
        echoList.clear()
        echoList.addAll(master.echoList)
        Log.v { "Sync succeeded" }
    }.getOrElse {
        Log.v { "Sync error ${it.message}" }
    }

    fun processClientEcho(echo: String): String {
        Log.i { "[Echo] $echo" }
        echoList.add(echo)
        return echo
    }

    fun recordEcho(echo: String) {
        Log.i { "[Replicated Echo] $echo" }
        echoList.add(echo)
    }

    fun startScanner() = Thread {
        while (true) try {
            val masterServer = findMaster()

            if (masterServer != null) {
                // Im not the master
                if (masterId != selfId) {
                    syncWithMaster(masterServer)
                }

                masterId = masterServer.id
            } else {
                Log.d { "Trying to assume master place" }
                tryToAssumeMasterPlace()
            }

            Thread.sleep(200)
        } catch (ignored: Throwable) {

        }
    }.start()
}