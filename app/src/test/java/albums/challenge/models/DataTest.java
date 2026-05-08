package albums.challenge.models;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataTest {
    @Test
    void convertMapsFeedEntriesToSearchEntries() {
        var data = new Data(new Data.Feed(List.of(
                new Data.Feed.Entry(
                        new Data.Feed.Entry.Label("Album title"),
                        new Data.Feed.Entry.Link(new Data.Feed.Entry.Link.Attributes("https://example.com/album")),
                        List.of(new Data.Feed.Entry.Label("https://example.com/image.jpg")),
                        new Data.Feed.Entry.Label("$12.99"),
                        new Data.Feed.Entry.Label("2026-01-02T00:00:00-07:00")
                )
        )));

        assertEquals(
                List.of(new Entry(
                        "Album title",
                        12.99f,
                        "2026-01-02T00:00:00-07:00",
                        "https://example.com/album",
                        "https://example.com/image.jpg"
                )),
                data.convert()
        );
    }
}
