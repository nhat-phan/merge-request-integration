package net.ntworld.mergeRequestIntegrationIde.infrastructure.service

import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import org.jdom.Element

interface FiltersStorageService {

    fun find(providerDataKey: String): Pair<GetMergeRequestFilter, MergeRequestOrdering>

    fun save(providerDataKey: String, filters: GetMergeRequestFilter, ordering: MergeRequestOrdering)

    fun writeTo(element: Element, providerDataKey: String)

    fun readFrom(element: Element, providerDataKey: String)
}