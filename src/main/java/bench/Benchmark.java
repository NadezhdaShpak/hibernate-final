package bench;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.SessionCreator;
import dao.CityDAO;
import dao.CountryDAO;
import domain.City;
import domain.Country;
import domain.CountryLanguage;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import redis.CityCountry;
import redis.Language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(value = 1)
public class Benchmark {
    private static final List<Integer> TEST_IDS = List.of(3, 2545, 123, 4, 189, 89, 3458, 1189, 10, 102);

    private SessionFactory sessionFactory;
    private RedisClient redisClient;
    private ObjectMapper mapper;
    private List<CityCountry> preparedData;

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(args);
    }

    @Setup(Level.Trial)
    public void setup() {
        SessionCreator sessionCreator = new SessionCreator();
        this.sessionFactory = sessionCreator.getSessionFactory();
        this.redisClient = RedisClient.create(RedisURI.create("redis", 6379));
        this.mapper = new ObjectMapper();

        List<City> cities = fetchData();
        this.preparedData = transformData(cities);
        prepareRedisData();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void testRedisData(Blackhole blackhole) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            TEST_IDS.forEach(id -> {
                String value = sync.get(String.valueOf(id));
                try {
                    CityCountry city = mapper.readValue(value, CityCountry.class);
                    blackhole.consume(city);
                } catch (JsonProcessingException ignored) {
                }
            });
        }
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void testMysqlData(Blackhole blackhole) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            CityDAO cityDAO = new CityDAO(session);
            for (Integer id : TEST_IDS) {
                City city = cityDAO.getById(id);
                Set<CountryLanguage> languages = city.getCountry().getLanguages();
                blackhole.consume(languages);
            }
            session.getTransaction().commit();
        }
    }

    private List<City> fetchData() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            CityDAO cityDAO = new CityDAO(session);
            CountryDAO countryDAO = new CountryDAO(session);
            countryDAO.getAll();

            List<City> cities = new ArrayList<>();
            int totalCount = cityDAO.getTotalCount();
            int step = 500;

            for (int i = 0; i < totalCount; i += step) {
                cities.addAll(cityDAO.getItems(i, step));
            }

            session.getTransaction().commit();
            return cities;
        }
    }

    private List<CityCountry> transformData(List<City> cities) {
        return cities.stream().map(city -> {
            CityCountry res = new CityCountry();
            res.setId(city.getId());
            res.setName(city.getName());
            res.setPopulation(city.getPopulation());
            res.setDistrict(city.getDistrict());

            Country country = city.getCountry();
            res.setAlternativeCountryCode(country.getAlternativeCode());
            res.setContinent(country.getContinent());
            res.setCountryCode(country.getCode());
            res.setCountryName(country.getName());
            res.setCountryPopulation(country.getPopulation());
            res.setCountryRegion(country.getRegion());
            res.setCountrySurfaceArea(country.getSurfaceArea());

            Set<CountryLanguage> countryLanguages = country.getLanguages();
            Set<Language> languages = countryLanguages.stream().map(cl -> {
                Language language = new Language();
                language.setLanguage(cl.getLanguage());
                language.setIsOfficial(cl.getIsOfficial());
                language.setPercentage(cl.getPercentage());
                return language;
            }).collect(Collectors.toSet());
            res.setLanguages(languages);
            return res;
        }).collect(Collectors.toList());
    }

    private void prepareRedisData() {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityCountry city : preparedData) {
                try {
                    sync.set(String.valueOf(city.getId()), mapper.writeValueAsString(city));
                } catch (JsonProcessingException ignored) {
                }
            }
        }
    }
}
