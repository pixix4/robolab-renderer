package de.robolab.renderer.drawable

import de.robolab.renderer.DefaultPlotter

interface IRootDrawable: IDrawable {

    fun onAttach(plotter: DefaultPlotter)

    fun onDetach(plotter: DefaultPlotter)
}
