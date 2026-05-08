package albums.challenge;

import albums.challenge.models.Entry;
import albums.challenge.models.Facet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class SearchServiceTest {
    SearchService searchService = new SearchService();
    Entry entry1 = new Entry(
            "Legend: The Best of Bob Marley and the Wailers (Remastered)",
            9.99f,
            "2002-01-01T00:00:00-07:00",
            "",
            ""
    );
    Entry entry2 = new Entry(
            "The Very Best of The Doors",
            19.99f,
            "2008-01-29T00:00:00-07:00",
            "",
            ""
    );
    List<Entry> entries = List.of(entry1, entry2);

    @Test
    public void testEmptySearch() {
        assertIterableEquals(
                entries,
                searchService.search(entries, "").items()
        );
    }

    @Test
    public void testSearchByGeneralKeyword() {
        assertIterableEquals(
                entries,
                searchService.search(entries, "best").items()
        );
    }

    @Test
    public void testSearchByExactKeyword() {
        assertIterableEquals(
                List.of(entry2),
                searchService.search(entries, "doors").items()
        );
    }

    @Test
    public void testPriceFacetGeneration() {
        assertIterableEquals(
                List.of(
                        new Facet("5 - 10", 1),
                        new Facet("15 - 20", 1)
                ),
                searchService.search(entries, "best").facets().get("price")
        );
    }

    @Test
    public void testYearFacetGeneration() {
        assertIterableEquals(
                List.of(
                        new Facet("2008", 1),
                        new Facet("2002", 1)
                ),
                searchService.search(entries, "best").facets().get("year")
        );
    }

    @Test
    public void testFacetGenerationUsesSearchMatches() {
        var result = searchService.search(entries, "doors");

        assertIterableEquals(
                List.of(
                        new Facet("15 - 20", 1)
                ),
                result.facets().get("price")
        );

        assertIterableEquals(
                List.of(
                        new Facet("2008", 1)
                ),
                result.facets().get("year")
        );
    }

    @Test
    public void testFilterMultipleFacetValues() {
        var result = searchService.search(entries, "best", List.of("2002", "2008"), List.of());
        assertIterableEquals(
                entries,
                result.items()
        );

        assertIterableEquals(
                List.of(
                        new Facet("2008", 1),
                        new Facet("2002", 1)
                ),
                result.facets().get("year")
        );
        assertIterableEquals(
                List.of(
                        new Facet("5 - 10", 1),
                        new Facet("15 - 20", 1)
                ),
                result.facets().get("price")
        );
    }

    @Test
    public void testFilterMultipleFacets() {
        var result = searchService.search(entries, "best", List.of("2002"), List.of("5 - 10"));

        assertIterableEquals(
                List.of(entry1),
                result.items()
        );

        assertIterableEquals(
                List.of(
                        new Facet("2002", 1)
                ),
                result.facets().get("year")
        );
        assertIterableEquals(
                List.of(
                        new Facet("5 - 10", 1)
                ),
                result.facets().get("price")
        );
    }

    @Test
    public void testFilterReturnsZeroCount() {
        var result = searchService.search(entries, "best", List.of("2002", "2008"), List.of("15 - 20"));

        assertIterableEquals(
                List.of(entry2),
                result.items()
        );

        assertIterableEquals(
                List.of(
                        new Facet("2008", 1)
                ),
                result.facets().get("year")
        );

        assertIterableEquals(
                List.of(
                        new Facet("5 - 10", 1),
                        new Facet("15 - 20", 1)
                ),
                result.facets().get("price")
        );
    }
}
