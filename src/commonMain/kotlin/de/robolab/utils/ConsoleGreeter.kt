package de.robolab.utils

object ConsoleGreeter {
    private val appLogo = """
            ____        __          __      __    ____                 __                   
           / __ \____  / /_  ____  / /___ _/ /_  / __ \___  ____  ____/ /__  ________  _____
          / /_/ / __ \/ __ \/ __ \/ / __ `/ __ \/ /_/ / _ \/ __ \/ __  / _ \/ ___/ _ \/ ___/
         / _, _/ /_/ / /_/ / /_/ / / /_/ / /_/ / _, _/  __/ / / / /_/ /  __/ /  /  __/ /    
        /_/ |_|\____/_.___/\____/_/\__,_/_.___/_/ |_|\___/_/ /_/\__,_/\___/_/   \___/_/
    """.trimIndent()

    private val appCreators = """
        by pixix4 and leoniqorn
    """.trimIndent()

    fun greet() {
        greetApplication(appLogo, appCreators)
    }
}

internal expect fun ConsoleGreeter.greetApplication(appLogo: String, appCreators:String)
