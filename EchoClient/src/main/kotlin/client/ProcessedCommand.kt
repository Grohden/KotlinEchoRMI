package client

sealed class ProcessedCommand {
    class Echo(
        val value: String
    ) : ProcessedCommand()

    object List : ProcessedCommand()
}