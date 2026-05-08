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
import java.util.stream.Collectors;

@Service
public class SearchService {
    private static final int PRICE_RANGE_SIZE = 5;

    Results search(List<Entry> entries, String query) {
        return search(entries, query, List.of(), List.of());
    }

    Results search(List<Entry> entries, String query, List<String> year, List<String> price) {
        var matchingEntries = query.isBlank() ? entries : filterByQuery(entries, query);
        var filteredEntries = matchingEntries.stream()
                .filter(matchesAnyYear(year))
                .filter(matchesAnyPrice(price))
                .toList();

        return new Results(
                filteredEntries,
                Map.ofEntries(
                        Map.entry("year", yearFacets(matchingEntries)),
                        Map.entry("price", priceFacets(matchingEntries))
                ),
                query
        );
    }

    Set<String> tokenizeToWords(String query) {
        return Set.copyOf(List.of(query.toLowerCase().split("\\W+")));
    }

    List<Entry> filterByQuery(List<Entry> entries, String query) {
        var words = tokenizeToWords(query);
        return entries.stream().filter(entry -> {
            var tokens = tokenizeToWords(entry.title());
            return tokens.containsAll(words);
        }).toList();
    }

    private List<Facet> yearFacets(List<Entry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(this::releaseYear, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new Facet(entry.getKey(), entry.getValue().intValue()))
                .sorted(Comparator.comparing(Facet::value).reversed())
                .toList();
    }

    private List<Facet> priceFacets(List<Entry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(this::priceRange, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new Facet(entry.getKey(), entry.getValue().intValue()))
                .sorted(Comparator.comparingInt(facet -> priceRangeStart(facet.value())))
                .toList();
    }

    private String releaseYear(Entry entry) {
        return entry.release_date().substring(0, 4);
    }

    private String priceRange(Entry entry) {
        var start = ((int) (entry.price() / PRICE_RANGE_SIZE)) * PRICE_RANGE_SIZE;
        return start + " - " + (start + PRICE_RANGE_SIZE);
    }

    private int priceRangeStart(String range) {
        return Integer.parseInt(range.split(" - ")[0]);
    }

    private Predicate<Entry> matchesAnyYear(List<String> selectedYears) {
        var years = selectedValues(selectedYears);

        if (years.isEmpty()) {
            return entry -> true;
        }

        return entry -> years.contains(releaseYear(entry));
    }

    private Predicate<Entry> matchesAnyPrice(List<String> selectedPrices) {
        var prices = selectedValues(selectedPrices);

        if (prices.isEmpty()) {
            return entry -> true;
        }

        return entry -> prices.contains(priceRange(entry));
    }

    private List<String> selectedValues(List<String> values) {
        return values.stream()
                .filter(value -> !value.isBlank())
                .toList();
    }
}
