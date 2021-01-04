package net.ntworld.mergeRequestIntegrationIde.infrastructure.service.internal

import net.ntworld.mergeRequest.MergeRequestState
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequest.query.generated.GetMergeRequestFilterImpl
import net.ntworld.mergeRequestIntegration.util.SavedFiltersUtil
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.FiltersStorageService
import org.jdom.Element

class FiltersStorageServiceImpl(
    private val serviceProvider: ProjectServiceProvider
) : FiltersStorageService {
    private val myFiltersData: MutableMap<String, Pair<GetMergeRequestFilter, MergeRequestOrdering>> = mutableMapOf()

    override fun find(providerDataKey: String): Pair<GetMergeRequestFilter, MergeRequestOrdering> {
        val data = myFiltersData[providerDataKey]
        return if (null !== data && serviceProvider.applicationSettings.saveMRFilterState) {
            data
        } else {
            Pair(
                GetMergeRequestFilterImpl(
                    id = null,
                    state = MergeRequestState.OPENED,
                    search = "",
                    authorId = "",
                    assigneeId = "",
                    approverIds = listOf(""),
                    sourceBranch = ""
                ),
                MergeRequestOrdering.RECENTLY_UPDATED
            )
        }
    }

    override fun save(providerDataKey: String, filters: GetMergeRequestFilter, ordering: MergeRequestOrdering) {
        if (serviceProvider.applicationSettings.saveMRFilterState) {
            myFiltersData[providerDataKey] = Pair(filters, ordering)
        }
    }

    override fun readFrom(element: Element, providerDataKey: String) {
        val attribute = element.getAttribute("savedFilters")
        if (null !== attribute) {
            val data = SavedFiltersUtil.parse(attribute.value)
            if (null !== data) {
                myFiltersData[providerDataKey] = data
            }
        }
    }

    override fun writeTo(element: Element, providerDataKey: String) {
        val data = myFiltersData[providerDataKey]
        if (null !== data) {
            element.setAttribute("savedFilters", SavedFiltersUtil.stringify(data.first, data.second))
        }
    }

}