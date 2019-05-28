package echo.server


fun main() {
    Log.level = Level.ALL
    Server.startScanner()
    Log.i { "Server up awaiting clients" }

    while (true) try {
        print("> ")
        val line = readLine()!!

        when {
            line.contains("info", ignoreCase = true) -> {
                println("Level set to info")
                Log.level = Level.INFO
            }
            line.contains("debug", ignoreCase = true) -> {
                println("Level set to debug")
                Log.level = Level.DEBUG
            }
            line.contains("all", ignoreCase = true) -> {
                Log.level = Level.ALL
                println("Level set to all")
            }
            else -> {
                println("Can only set all, debug or info levels")
            }
        }

    } catch (ignored: Throwable) {

    }
}
