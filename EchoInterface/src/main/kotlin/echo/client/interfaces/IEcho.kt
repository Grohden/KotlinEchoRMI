package echo.client.interfaces

import java.io.Serializable
import java.rmi.Remote
import java.rmi.RemoteException

interface IEcho : Remote, Serializable {

    @Throws(RemoteException::class)
    fun output(message: String): String
}