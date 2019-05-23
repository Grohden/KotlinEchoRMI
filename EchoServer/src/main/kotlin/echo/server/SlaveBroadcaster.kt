package echo.server

import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry

object SlaveBroadcaster {

    fun emitEcho(ports: Set<Int>, echo: String) {
        ports
            .parallelStream()
            .filter { it != Registry.REGISTRY_PORT }
            .forEach { port ->
                try {
                    Log.d { "Sending echo message" }

                    val registry = LocateRegistry.getRegistry(port)
                    val slave = registry.lookup("Slave") as IServerCommunicator
                    slave.recordEcho(echo)

                    Log.d { "Echo sent to $port" }
                } catch (t: Throwable) {
                    Log.e { "Echo sent error to $port" }
                }
            }


    }
}