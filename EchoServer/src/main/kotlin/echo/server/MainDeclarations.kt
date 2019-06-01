package echo.server


fun main() {
    Server.startScanner()
    Log.i { "Server up awaiting clients" }
}
