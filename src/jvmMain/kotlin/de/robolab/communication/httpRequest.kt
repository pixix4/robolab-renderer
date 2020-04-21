package de.robolab.communication

actual fun httpRequest(url: String, onFinish: (String?) -> Unit) {
    onFinish(MqttDemoFile.loadFile())
}

object MqttDemoFile {
    fun loadFile(): String? {
        return this::class.java.classLoader.getResourceAsStream("demo/mqtt.console.log")?.bufferedReader()?.readLines()?.joinToString("\n")
    }
}
