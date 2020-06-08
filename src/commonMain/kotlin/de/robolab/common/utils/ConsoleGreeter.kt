package de.robolab.common.utils

object ConsoleGreeter {
    val appLogo = """
            ____        __          __      __    ____                 __                   
           / __ \____  / /_  ____  / /___ _/ /_  / __ \___  ____  ____/ /__  ________  _____
          / /_/ / __ \/ __ \/ __ \/ / __ `/ __ \/ /_/ / _ \/ __ \/ __  / _ \/ ___/ _ \/ ___/
         / _, _/ /_/ / /_/ / /_/ / / /_/ / /_/ / _, _/  __/ / / / /_/ /  __/ /  /  __/ /    
        /_/ |_|\____/_.___/\____/_/\__,_/_.___/_/ |_|\___/_/ /_/\__,_/\___/_/   \___/_/
    """.trimIndent()

    val appClientCreators = """
        by pixix4, Zincfox and leoniqorn
    """.trimIndent()
    val appServerCreators = """
        by Zincfox and pixix4
    """.trimIndent()

    private var isPrinted = false
    fun greetClient(forcePrint: Boolean = false) {
        if (isPrinted && !forcePrint) return

        greetApplication(appLogo, appClientCreators)
        isPrinted = true
    }
    fun greetServer(forcePrint: Boolean = false) {
        if (isPrinted && !forcePrint) return

        greetApplication(appLogo, appServerCreators)
        isPrinted = true
    }
}

internal expect fun ConsoleGreeter.greetApplication(appLogo: String, appCreators: String)
