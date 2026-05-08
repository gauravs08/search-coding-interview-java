# Albums Challenge

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen)
![Gradle](https://img.shields.io/badge/Gradle-9.4.0-02303A)
![Tests](https://img.shields.io/badge/Tests-JUnit%206-blue)
![Coverage](https://img.shields.io/badge/Coverage-JaCoCo-orange)

Your challenge is to finish this web app which lists the top 100 music albums from iTunes with search and filter
functionality.

* Fork this GitHub repository.
* Implement the changes listed below.
* Ensure that the GitHub CI build succeeds.

## Setup

### Requirements

* Java 21 or newer

### Running

* `./gradlew bootRun`
* Open http://localhost:8080 in your browser. 

* You can also run the app from IntelliJ by running `Application` class.


## Your Tasks

---

**NOTE**

The [tests](app/src/test) cover most of the required functionality. You can run tests by executing `./gradlew test` or
executing them in IntelliJ.

---

### 1. Implement price and year filtering options.

- Currently, there are some hardcoded filtering options (also called facets) for price and year filters. You need to
  generate options that are relevant for albums that match the search query.
- Price filtering options should be displayed in ranges, e.g., `0-5`, `5-10`, `10-15`.
- Year filter options should be all years, in descending order, that match at least one album.

### 2. Implement result filtering.

- Search results can be narrowed by selecting some filtering options.
- Filters in the same group should be joined by `OR` and different groups are joined by `AND`. For example, if a user
  selects the years 2017 and 2018, and price range `5-10`, you should show albums with a price between 5 and 10 _and_
  from 2017 _or_ 2018.
- When no filters are selected, show all albums that match the search query.

### 3. Implement count for each filtering option.

- Each filtering option has a count displayed next to it which indicates how many results are matched by the filter. The
  numbers have to take into account selected filters in other groups and update as user checks or unchecks filters to be
  accurate for the current filtering combination.
- You should show only the options that will match at least one album. Thus, filtering options might change when a user
  selects other filters. For example, if a user selects price range `0-5` and there are no albums that cost between 0
  and 5 and were released in 2017, you shouldn't show year 2017 as a filtering option. But 2017 should appear as
  a filter option when the user selects the `5-10` price range (or has no price selected) because there are some albums
  that were released in 2017 and cost 9.99.
