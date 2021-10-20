package de.robolab.client.repl.commands.window

import de.robolab.client.repl.ReplExecutor
import de.robolab.client.repl.autoComplete
import de.robolab.client.repl.base.*
import de.robolab.client.theme.utils.Theme
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.toggle

object WindowThemeCommand : ReplSingleBindableNodeCommand<Unit>(
    "theme",
    "Set the current theme",
    Unit::class,
) {

    init {
        addCommand(WindowThemeSetCommand)
        addCommand(WindowThemeToggleDarkModeCommand)
        addCommand(WindowThemeToggleSystemModeCommand)

        bind(Unit)
    }

    override suspend fun requestAutoCompleteFor(
        binding: Unit,
        descriptor: ReplCommandParameterDescriptor<*>,
        token: String,
    ): List<ReplExecutor.AutoComplete>? {
        if (EnumParameter.isDescriptor<ThemeType>(descriptor.type)) {
            return ThemeType.values().map { it.name.lowercase() }.autoComplete(token)
        }

        return super.requestAutoCompleteFor(binding, descriptor, token)
    }
}

object WindowThemeSetCommand : ReplBindableLeafCommand<Unit>(
    "set",
    "Set the current theme",
    Unit::class,
) {

    private val themeParameter = EnumParameter
        .create<ThemeType>("Specify the theme").param("theme")

    override suspend fun execute(binding: Unit, context: IReplExecutionContext) {
        val themeType = context.getParameter(themeParameter).value

        val groupThemes = Theme.values().filter { it.group.equals(themeType.name, true) }
        val isDarkMode = PreferenceStorage.selectedTheme.isDarkMode == true
        PreferenceStorage.selectedTheme = groupThemes.firstOrNull { it.isDarkMode == isDarkMode }
            ?: groupThemes.firstOrNull() ?: Theme.DEFAULT
    }
}

object WindowThemeToggleDarkModeCommand : ReplBindableLeafCommand<Unit>(
    "toggle-dark-mode",
    "Switch between light and dark mode",
    Unit::class,
) {

    private val forceParameter = BooleanParameter.optional("force")

    override suspend fun execute(binding: Unit, context: IReplExecutionContext) {
        val isDarkMode =
            context.getParameter(forceParameter)?.value ?: PreferenceStorage.selectedTheme.isDarkMode?.let { !it }
            ?: false
        if (!PreferenceStorage.useSystemTheme) {
            PreferenceStorage.selectedTheme = PreferenceStorage.selectedTheme.getThemeByMode(isDarkMode)
        }
    }
}

object WindowThemeToggleSystemModeCommand : ReplBindableLeafCommand<Unit>(
    "toggle-system-mode",
    "Specify if the dark mode should follow the system preferences",
    Unit::class,
) {

    private val forceParameter = BooleanParameter.optional("force")

    override suspend fun execute(binding: Unit, context: IReplExecutionContext) {
        val force = context.getParameter(forceParameter)

        if (force == null) {
            PreferenceStorage.useSystemThemeProperty.toggle()
        } else {
            PreferenceStorage.useSystemTheme = force.value
        }
    }
}

private enum class ThemeType {
    DEFAULT, GRUVBOX, NORD
}
