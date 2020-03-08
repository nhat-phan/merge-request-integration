package net.ntworld.mergeRequestIntegrationIde.diff

import net.ntworld.mergeRequestIntegrationIde.SimplePresenter

interface DiffPresenter : SimplePresenter {
    val model: DiffModel
    val view: DiffView<*>
}