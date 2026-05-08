import { renderSidebar, renderContent, bindFacetChange, bindClearFilters } from './rendering.js'


export async function executeSearch(searchQuery, filters) {
    console.log(`Searching for "${searchQuery}" with filters`, filters)

    const params = new URLSearchParams({
        query: searchQuery
    })
    Object.entries(filters).forEach(([k, vs]) => {
        vs.forEach(v => {
            params.append(k, v)
        })
    })

    const response = await fetch(`/api/search?${params.toString()}`)

    if (!response.ok) {
        alert("Search failed");
        return
    }

    const { facets, items } = await response.json()

    console.log('Got response', {facets, items})

    renderSidebar(facets, filters)
    renderContent(items)
    bindFacetChange(newFilters => {
        executeSearch(searchQuery, newFilters)
    })
    bindClearFilters(() => {
        executeSearch(searchQuery, {})
    })
}
