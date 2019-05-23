package client

import echo.client.interfaces.IEcho
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry


class Client {

    init {
        try {
            val registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT)
            val stub = registry.lookup("Echo") as IEcho
            println(stub.echo("Test"))

            stub.getHistory().forEach(::println)

        } catch (e: Exception) {
            System.err.println("Client exception: $e")
            e.printStackTrace()
        }
    }
}


fun main() {
    Client()
}