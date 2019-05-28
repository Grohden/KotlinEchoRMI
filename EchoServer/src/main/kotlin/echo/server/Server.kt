package echo.server

import echo.client.interfaces.IEcho
import java.rmi.NotBoundException
import java.rmi.RemoteException
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import java.rmi.server.ExportException
import java.rmi.server.UnicastRemoteObject
import java.util.*

object Server {
    private val usedPorts: MutableSet<Int> = mutableSetOf(Registry.REGISTRY_PORT)
    private var selfId = UUID.randomUUID()

    // to try to avoid garbage collection here
    private var stubbedMaster: IServerCommunicator? = null
    private var stubbedClient: IEcho? = null

    var masterId: UUID? = null
    var echoList = mutableListOf<String>()

    private fun findMaster(): IServerCommunicator? {
        return try {
            LocateRegistry
                .getRegistry(Registry.REGISTRY_PORT)
                .lookup("Master") as IServerCommunicator
        } catch (e: RemoteException) {
            null
        } catch (nb: NotBoundException) {
            null
        }

    }

    private fun tryToAssumeMasterPlace() = try {
        Log.d { "Trying to assume master place" }

        val server = ServerCommunicator(id = selfId)
        val registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT)
        stubbedMaster = UnicastRemoteObject.exportObject(
            server,
            Registry.REGISTRY_PORT
        ) as IServerCommunicator


        stubbedClient = UnicastRemoteObject.exportObject(
            ClientCommunicator(),
            server.port
        ) as IEcho

        registry.rebind("Master", stubbedMaster!!)
        registry.rebind("Echo", stubbedClient!!)

        Log.d { "Assumed master control" }
        masterId = server.id
    } catch (t: Throwable) {
        Log.e { "Could not assume master place" }
        masterId = null
    }

    private fun registerAsSlave(master: IServerCommunicator) {
        while (true) {
            val givenPort = master.register()

            try {
                val self = ServerCommunicator(id = selfId, port = givenPort)
                val registry = LocateRegistry.createRegistry(self.port)
                val stub = UnicastRemoteObject.exportObject(self, self.port) as IServerCommunicator

                registry.rebind("Slave", stub)
                Log.d { "Registered as slave at $givenPort" }

                echoList.clear()
                echoList.addAll(master.echoList)
                break
            } catch (e: ExportException) {
                Log.e { "Port $givenPort already in use" }
            }
        }
    }

    fun registerSlave(): Int {
        val lastUsedPort = usedPorts.sorted().last()
        val givenPort = lastUsedPort + 1

        usedPorts.add(givenPort)
        Log.d { "Giving port $givenPort" }

        return givenPort
    }

    fun processClientEcho(echo: String): String {
        Log.i { "[Echo] $echo" }
        echoList.add(echo)
        SlaveBroadcaster.emitEcho(usedPorts, echo)

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
                    // Master changed
                    if (masterId != masterServer.id) {
                        Log.d { "Master changed" }
                        registerAsSlave(masterServer)
                    }
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