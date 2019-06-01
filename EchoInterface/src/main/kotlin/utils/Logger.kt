package echo.server

enum class Level {
    INFO, // Client stuff
    ERROR, // errors
    DEBUG, // All stuff
    VERBOSE, // All stuff
    ALL // All but again
}

typealias Make = (provider: () -> String) -> Unit

object Log {
    var level = Level.DEBUG

    private fun make(minLevel: Level): Make = { provider ->
        if (level >= minLevel) {
            println(provider())
        }
    }


    val d = make(Level.DEBUG)
    val i = make(Level.INFO)
    val e = make(Level.ERROR)
    val v = make(Level.VERBOSE)
}