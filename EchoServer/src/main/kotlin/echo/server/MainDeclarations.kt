package echo.server


fun main() {
    Log.level = Level.INFO
    Server.startScanner()
    Log.i { "Server up awaiting clients" }
}
