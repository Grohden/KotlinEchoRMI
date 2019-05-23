package echo.client.interfaces

import java.io.Serializable
import java.rmi.Remote
import java.rmi.RemoteException

interface IEcho : Remote, Serializable {

    @Throws(RemoteException::class)
    fun echo(message: String): String

    @Throws(RemoteException::class)
    fun getHistory(): List<String>
}