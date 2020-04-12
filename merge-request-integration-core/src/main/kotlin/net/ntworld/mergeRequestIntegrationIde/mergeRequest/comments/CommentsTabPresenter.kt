package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments

import com.intellij.openapi.Disposable
import com.intellij.ui.tabs.TabInfo
import net.ntworld.mergeRequestIntegrationIde.SimplePresenter

interface CommentsTabPresenter : SimplePresenter, Disposable {
    val model: CommentsTabModel

    val view: CommentsTabView

    val tabInfo: TabInfo
        get() = view.tabInfo
}