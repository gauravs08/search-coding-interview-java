package albums.challenge;

import albums.challenge.models.Entry;
import albums.challenge.models.Facet;
import albums.challenge.models.Results;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private static final int PRICE_RANGE_SIZE = 5;

    /**
     * Searches albums by query only.
     *
     * <p>This is the basic entry point used when no facet filters are selected.</p>
     *
     * @param entries the albums to search
     * @param query the user search text
     * @return the search result payload with items and facets
     */
    Results search(List<Entry> entries, String query) {
        return search(entries, query, List.of(), List.of());
    }

    /**
     * Searches albums by query and selected year/price filters.
     *
     * <p>The query is applied first, then year and price filters narrow the result items.
     * Facets are rebuilt from the query-matching albums so the sidebar reflects the current
     * search state.</p>
     *
     * @param entries the albums to search
     * @param query the user search text
     * @param year selected year facet values
     * @param price selected price facet values
     * @return the search result payload with filtered items and updated facets
     */
    Results search(List<Entry> entries, String query, List<String> year, List<String> price) {
        var matchingEntries = query.isBlank() ? entries : filterByQuery(entries, query);
        var filteredEntries = matchingEntries.stream()
                .filter(matchesAnyYear(year))
                .filter(matchesAnyPrice(price))
                .toList();

        return new Results(
                filteredEntries,
                Map.ofEntries(
                        Map.entry("year", yearFacets(matchingEntries, price)),
                        Map.entry("price", priceFacets(matchingEntries, year))
                ),
                query
        );
    }

    /**
     * Splits text into lower-cased tokens and removes duplicates.
     *
     * @param query the text to tokenize
     * @return a set of normalized words
     */
    Set<String> tokenizeToWords(String query) {
        return Set.copyOf(List.of(query.toLowerCase().split("\\W+")));
    }

    /**
     * Filters albums whose title contains all query words.
     *
     * @param entries the albums to filter
     * @param query the user search text
     * @return the albums that match the query
     */
    List<Entry> filterByQuery(List<Entry> entries, String query) {
        var words = tokenizeToWords(query);
        return entries.stream().filter(entry -> {
            var tokens = tokenizeToWords(entry.title());
            return tokens.containsAll(words);
        }).toList();
    }

    /**
     * Builds year facets after applying the selected price filters.
     *
     * @param entries query-matching albums
     * @param selectedPrices selected price facet values
     * @return year facets ordered from newest to oldest
     */
    private List<Facet> yearFacets(List<Entry> entries, List<String> selectedPrices) {
        var entriesMatchingOtherFilters = entries.stream()
                .filter(matchesAnyPrice(selectedPrices))
                .toList();
        var counts = countBy(entriesMatchingOtherFilters, this::releaseYear);

        return entriesMatchingOtherFilters.stream()
                .map(this::releaseYear)
                .distinct()
                .map(year -> new Facet(year, counts.get(year).intValue()))
                .sorted(Comparator.comparing(Facet::value).reversed())
                .toList();
    }

    /**
     * Builds price facets after applying the selected year filters.
     *
     * @param entries query-matching albums
     * @param selectedYears selected year facet values
     * @return price facets ordered from the lowest range to the highest
     */
    private List<Facet> priceFacets(List<Entry> entries, List<String> selectedYears) {
        var entriesMatchingOtherFilters = entries.stream()
                .filter(matchesAnyYear(selectedYears))
                .toList();
        var counts = countBy(entriesMatchingOtherFilters, this::priceRange);

        return entriesMatchingOtherFilters.stream()
                .map(this::priceRange)
                .distinct()
                .map(price -> new Facet(price, counts.get(price).intValue()))
                .sorted(Comparator.comparingInt(facet -> priceRangeStart(facet.value())))
                .toList();
    }

    /**
     * Counts how many albums map to each facet value after the other filter group is applied.
     *
     * @param entries the albums to count from
     * @param facetValue function that extracts the facet label from an album
     * @return a count map keyed by facet label
     */
    private Map<String, Long> countBy(
            List<Entry> entries,
            Function<Entry, String> facetValue
    ) {
        return entries.stream()
                .collect(Collectors.groupingBy(facetValue, Collectors.counting()));
    }

    /**
     * Extracts the release year from the album release date string.
     *
     * @param entry the album entry
     * @return the four-digit release year
     */
    private String releaseYear(Entry entry) {
        return entry.release_date().substring(0, 4);
    }

    /**
     * Converts a price into a display range bucket.
     *
     * @param entry the album entry
     * @return the price bucket label, such as {@code 5 - 10}
     */
    private String priceRange(Entry entry) {
        var start = ((int) (entry.price() / PRICE_RANGE_SIZE)) * PRICE_RANGE_SIZE;
        return start + " - " + (start + PRICE_RANGE_SIZE);
    }

    /**
     * Parses the low end of a price range label for sorting.
     *
     * @param range the price range label
     * @return the numeric lower bound of the range
     */
    private int priceRangeStart(String range) {
        return Integer.parseInt(range.split(" - ")[0]);
    }

    /**
     * Builds a predicate that matches any selected year value.
     *
     * @param selectedYears selected year facet values
     * @return a predicate that accepts all entries when no year is selected
     */
    private Predicate<Entry> matchesAnyYear(List<String> selectedYears) {
        var years = selectedValues(selectedYears);

        if (years.isEmpty()) {
            return entry -> true;
        }

        return entry -> years.contains(releaseYear(entry));
    }

    /**
     * Builds a predicate that matches any selected price range.
     *
     * @param selectedPrices selected price facet values
     * @return a predicate that accepts all entries when no price is selected
     */
    private Predicate<Entry> matchesAnyPrice(List<String> selectedPrices) {
        var prices = selectedValues(selectedPrices);

        if (prices.isEmpty()) {
            return entry -> true;
        }

        return entry -> prices.contains(priceRange(entry));
    }

    /**
     * Removes blank facet values before filter matching.
     *
     * @param values raw selected values
     * @return the non-blank values
     */
    private List<String> selectedValues(List<String> values) {
        return values.stream()
                .filter(value -> !value.isBlank())
                .toList();
    }
}
