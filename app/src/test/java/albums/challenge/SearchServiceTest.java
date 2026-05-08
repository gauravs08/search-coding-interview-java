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
                searchService.search(entries, "").items(),
                entries
        );
    }

    @Test
    public void testSearchByGeneralKeyword() {
        assertIterableEquals(
                searchService.search(entries, "best").items(),
                entries
        );
    }

    @Test
    public void testSearchByExactKeyword() {
        assertIterableEquals(
                searchService.search(entries, "doors").items(),
                List.of(entry2)
        );
    }

    @Test
    public void testPriceFacetGeneration() {
        assertIterableEquals(
                searchService.search(entries, "best").facets().get("price"),
                List.of(
                        new Facet("5 - 10", 1),
                        new Facet("15 - 20", 1)
                )
        );
    }

    @Test
    public void testYearFacetGeneration() {
        assertIterableEquals(
                searchService.search(entries, "best").facets().get("year"),
                List.of(
                        new Facet("2008", 1),
                        new Facet("2002", 1)
                )
        );
    }

    @Test
    public void testFacetGenerationUsesSearchMatches() {
        var result = searchService.search(entries, "doors");

        assertIterableEquals(
                result.facets().get("price"),
                List.of(
                        new Facet("15 - 20", 1)
                )
        );

        assertIterableEquals(
                result.facets().get("year"),
                List.of(
                        new Facet("2008", 1)
                )
        );
    }

    @Test
    public void testFilterMultipleFacetValues() {
        var result = searchService.search(entries, "best", List.of("2002", "2008"), List.of());
        assertIterableEquals(
                result.items(),
                entries
        );

        assertIterableEquals(
                result.facets().get("year"),
                List.of(
                        new Facet("2008", 1),
                        new Facet("2002", 1)
                )
        );
        assertIterableEquals(
                result.facets().get("price"),
                List.of(
                        new Facet("5 - 10", 1),
                        new Facet("15 - 20", 1)
                )
        );
    }

    @Test
    public void testFilterMultipleFacets() {
        var result = searchService.search(entries, "best", List.of("2002"), List.of("5 - 10"));

        assertIterableEquals(
                result.items(),
                List.of(entry1)
        );

        assertIterableEquals(
                result.facets().get("year"),
                List.of(
                        new Facet("2002", 1)
                )
        );
        assertIterableEquals(
                result.facets().get("price"),
                List.of(
                        new Facet("5 - 10", 1)
                )
        );
    }

    @Test
    public void testFilterReturnsZeroCount() {
        var result = searchService.search(entries, "best", List.of("2002", "2008"), List.of("15 - 20"));

        assertIterableEquals(
                result.items(),
                List.of(entry2)
        );

        assertIterableEquals(
                result.facets().get("year"),
                List.of(
                        new Facet("2008", 1),
                        new Facet("2002", 0)
                )
        );

        assertIterableEquals(
                result.facets().get("price"),
                List.of(
                        new Facet("5 - 10", 1),
                        new Facet("15 - 20", 1)
                )
        );
    }
}
