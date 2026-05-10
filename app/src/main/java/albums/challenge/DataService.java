package albums.challenge;

import albums.challenge.models.Data;
import albums.challenge.models.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class DataService {
    Logger logger = LogManager.getLogger(DataService.class);
    String uri = "https://itunes.apple.com/us/rss/topalbums/limit=200/json";

    /**
     * Fetches the album feed once and converts it into internal entries.
     *
     * <p>The result is cached so the application does not hit the remote feed on every search request.</p>
     *
     * @return the current album list
     */
    @Cacheable("entry")
    public List<Entry> fetch() {
        logger.info("Fetching data");

        var restTemplate = new RestTemplate();
        var converters = restTemplate.getMessageConverters();
        converters.forEach(converter -> {
            if (converter instanceof JacksonJsonHttpMessageConverter jsonConverter) {
                jsonConverter.setSupportedMediaTypes(Arrays.asList(
                        new MediaType("application", "json", Charset.defaultCharset()),
                        new MediaType("text", "javascript", Charset.defaultCharset())
                ));
            }
        });

        return Objects.requireNonNull(restTemplate.getForObject(uri, Data.class), "failed to fetch").convert();
    }
}
