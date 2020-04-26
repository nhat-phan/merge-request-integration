package net.ntworld.mergeRequestIntegrationIde.component

object ComponentFactory {
    fun makePaginationToolbar(displayRefreshButton: Boolean = false): PaginationToolbar {
        return PaginationToolbarImpl(displayRefreshButton)
    }
}