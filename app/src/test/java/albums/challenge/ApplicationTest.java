package albums.challenge;

import albums.challenge.models.Entry;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class ApplicationTest {
    @Test
    void indexReturnsStaticIndexPage() {
        var application = new Application();

        assertEquals("index.html", application.index().getViewName());
    }

    @Test
    void searchDelegatesToDataAndSearchServices() {
        var application = new Application();
        var entries = List.of(
                new Entry("Best Album", 9.99f, "2026-01-01T00:00:00-07:00", "", "")
        );
        application.dataService = new StubDataService(entries);
        application.searchService = new SearchService();

        var results = application.search("best", List.of("2026"), List.of("5 - 10"));

        assertIterableEquals(entries, results.items());
    }

    static class StubDataService extends DataService {
        private final List<Entry> entries;

        StubDataService(List<Entry> entries) {
            this.entries = entries;
        }

        @Override
        public List<Entry> fetch() {
            return entries;
        }
    }
}
