package locations;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.zalando.problem.Problem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LocationsControllerRestTemplateIT {

    @Autowired
    TestRestTemplate template;

    @Autowired
    LocationsService service;

    @BeforeEach
    void setUp() {
        service.deleteAllLocations();
    }

//    @Test
//    void testGetLocations() {
//        template.postForObject("/api/locations", new CreateLocationCommand("Róma", 41.90383, 12.50557), LocationDto.class);
//        template.postForObject("/api/locations", new CreateLocationCommand("Athén", 37.97954, 23.72638), LocationDto.class);
//
//        List<LocationDto> locations = template.exchange("/api/locations",
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<List<LocationDto>>() {
//                })
//                .getBody();
//
//        assertThat(locations)
//                .extracting(LocationDto::getName)
//                .containsExactly("Róma", "Athén");
//    }

    @Test
    void testGetLocations() {
        template.postForObject("/api/locations", new CreateLocationCommand("Róma", 41.90383, 12.50557), LocationDto.class);
        template.postForObject("/api/locations", new CreateLocationCommand("Athén", 37.97954, 23.72638), LocationDto.class);

//        List<LocationDto> locations = template.exchange("/api/locations",
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<List<LocationDto>>() {
//                })
//                .getBody();

        // Content Negotiation
        LocationsDto expected = template.exchange("/api/locations",
                HttpMethod.GET,
                null,
                LocationsDto.class)
                .getBody();

//        assertThat(locations)
        assertThat(expected.getLocations())
                .extracting(LocationDto::getName)
                .containsExactly("Róma", "Athén");
    }

    @Test
    void testFindLocationById() {
        LocationDto location =
                template.postForObject("/api/locations", new CreateLocationCommand("Róma", 41.90383, 12.50557), LocationDto.class);

        long id = location.getId();

        LocationDto expected = template.exchange("/api/locations/" + id,
                HttpMethod.GET,
                null,
                LocationDto.class)
                .getBody();

        assertThat(expected.getName()).isEqualTo("Róma");
    }

    @Test
    void testGetLocationsByName() {
        template.postForObject("/api/locations", new CreateLocationCommand("Róma", 41.90383, 12.50557), LocationDto.class);
        template.postForObject("/api/locations", new CreateLocationCommand("Bécs", 48.2083, 16.3731), LocationDto.class);
        template.postForObject("/api/locations", new CreateLocationCommand("Athén", 37.97954, 23.72638), LocationDto.class);
        template.postForObject("/api/locations", new CreateLocationCommand("Budapest", 47.497912, 19.040235), LocationDto.class);

//        List<LocationDto> expected = template.exchange("/api/locations?prefix=B",
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<List<LocationDto>>() {
//                })
//                .getBody();

        // Content Negotiation
        LocationsDto expected = template.exchange("/api/locations?prefix=B",
                HttpMethod.GET,
                null,
                LocationsDto.class)
                .getBody();

//        assertThat(expected).hasSize(2);
//        assertThat(expected).extracting(LocationDto::getName)
//                .containsExactly("Bécs", "Budapest");

        assertThat(expected.getLocations()).hasSize(2);
        assertThat(expected.getLocations()).extracting(LocationDto::getName)
                .containsExactly("Bécs", "Budapest");
    }

    @Test
    void testCreateLocation() {
        LocationDto locationDto =
                template.postForObject("/api/locations", new CreateLocationCommand("Róma", 41.90383, 12.50557), LocationDto.class);

        assertEquals("Róma", locationDto.getName());
        assertEquals(41.90383, locationDto.getLatitude());
        assertEquals(12.50557, locationDto.getLongitude());
    }

    @Test
    void testUpdateLocation() {
        LocationDto location =
                template.postForObject("/api/locations", new CreateLocationCommand("Róma", 41.90383, 12.50557), LocationDto.class);

        long id = location.getId();

        template.put("/api/locations/" + id, new UpdateLocationCommand("Róma", 2.2, 3.3));

        LocationDto expected = template.exchange("/api/locations/" + id,
                HttpMethod.GET,
                null,
                LocationDto.class)
                .getBody();

        assertEquals("Róma", expected.getName());
        assertEquals(2.2, expected.getLatitude());
        assertEquals(3.3, expected.getLongitude());
    }

    @Test
    void testDeleteLocation() {
        LocationDto location =
                template.postForObject("/api/locations", new CreateLocationCommand("Róma", 41.90383, 12.50557), LocationDto.class);

        long id = location.getId();

        template.delete("/api/locations/" + id);

//        List<LocationDto> expected = template.exchange("/api/locations",
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<List<LocationDto>>() {
//                })
//                .getBody();

        // Content Negotiation
        LocationsDto expected = template.exchange("/api/locations",
                HttpMethod.GET,
                null,
                LocationsDto.class)
                .getBody();

//        assertThat(expected).isEmpty();
        assertThat(expected.getLocations()).isEmpty();
    }

    // Validáció
    @Test
    void testCreateLocationWithNotValidNameLatitudeAndLongitude() {
        Problem expected =
                template.postForObject("/api/locations", new CreateLocationCommand("", -500, 500), Problem.class);

        assertEquals("400 Bad Request", expected.getStatus().toString());
//        assertEquals("Validation error", expected.getTitle());
        // Validáció problem-spring-web-starterrel
        assertEquals("Constraint Violation", expected.getTitle());
    }

    @Test
    void testUpdateLocationWithNotValidNameLatitudeAndLongitude() {
        LocationDto location =
                template.postForObject("/api/locations", new CreateLocationCommand("Róma", 41.90383, 12.50557), LocationDto.class);

        long id = location.getId();

        template.put("/api/locations/" + id, new UpdateLocationCommand("", -500, 500));

        LocationDto expected = template.exchange("/api/locations/" + id,
                HttpMethod.GET,
                null,
                LocationDto.class)
                .getBody();

        assertEquals("Róma", expected.getName());
        assertEquals(41.90383, expected.getLatitude());
        assertEquals(12.50557, expected.getLongitude());
    }

    // Spring Boot konfiguráció
//    @Test
//    void testGetLocationsNameFromConfiguration() {
//        LocationDto locationDto =
//                template.postForObject("/api/locations", new CreateLocationCommand("Róma", 41.90383, 12.50557), LocationDto.class);
//
//        assertEquals("RÓMA", locationDto.getName());
//    }
}
