package client

import echo.client.interfaces.IEcho
import echo.server.Log
import java.rmi.AccessException
import java.rmi.NotBoundException
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry


class Client {

    private fun findEcho(i: Int = 0): IEcho? {
        return try {
            val registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT)
            registry.lookup("Echo") as IEcho
        } catch (nb: NotBoundException) {
            if (i < 3) {
                findEcho(i + 1)
            } else {
                null
            }
        } catch (t: Throwable) {
            when (t) {
                is NotBoundException, is AccessException -> {
                    if (i < 3) {
                        findEcho(i + 1)
                    } else {
                        null
                    }
                }
                else -> {
                    Log.e { "Client exception: ${t.message}" }
                    null
                }
            }
        }
    }

    private fun printInstructions() {
        println("type: list - to get a list of recorded echoes")
        println("type: echo \"<text>\" - to make a echo")
    }

    fun startService() {
        println("Welcome to echo client")
        printInstructions()

        while (true) try {
            print("> ")
            val command = readLine()

            val result = command
                ?.let(this::parseLine)


            if (result != null) {
                val echoCli = findEcho()
                if (echoCli != null) {
                    processRead(result, echoCli)
                } else {
                    println("Server not found, is it running?")
                }
            } else {
                println("Invalid command \"$command\"")
                printInstructions()
            }
        } catch (t: Throwable) {
            Log.e { "Client exception after connection: ${t.message}" }
        }
    }

    private fun parseLine(command: String): ProcessedCommand? {
        val trimmed = command.trim()

        return when {
            trimmed.startsWith("echo") -> {
                val strTkn = "\""
                val messageStart = trimmed.indexOf(strTkn)
                val messageEnd = trimmed.lastIndexOf(strTkn)

                if (messageStart != -1 && messageEnd != -1) {
                    trimmed
                        .subSequence(messageStart + 1, messageEnd)
                        .toString()
                        .let { ProcessedCommand.Echo(it) }
                } else {
                    null
                }
            }
            trimmed == "list" -> {
                ProcessedCommand.List
            }
            else -> null
        }
    }

    private fun processRead(result: ProcessedCommand, cli: IEcho) {
        when (result) {
            is ProcessedCommand.Echo -> cli.echo(result.value)
            is ProcessedCommand.List -> cli
                .getHistory()
                .mapIndexed { i, m -> "$i: $m" }
                .forEach(::println)
        }
    }
}

fun main() {
    Client().startService()
}