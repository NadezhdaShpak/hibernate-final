
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.*;
import domain.City;
import domain.Country;
import domain.CountryLanguage;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import config.SessionCreator;
import redis.CityCountry;
import redis.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
@Slf4j
public class Main {
    private final SessionFactory sessionFactory;
    private final Session session;
    private StatefulRedisConnection<String, String> redisConnection;
    private final RedisClient redisClient;
    private ObjectMapper mapper;
    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;

    public Main() {
        SessionCreator sessionCreator = new SessionCreator();
        sessionFactory = sessionCreator.getSessionFactory();
        session = sessionFactory.openSession();
        cityDAO = new CityDAO(session);
        countryDAO = new CountryDAO(session);

        redisClient = prepareRedisClient();
        redisConnection = redisClient.connect();
        mapper = new ObjectMapper();
    }

    public static void main(String[] args) {
        Main main = new Main();
        List<City> allCities = main.fetchData();
        List<CityCountry> preparedData = main.transformData(allCities);
        main.pushToRedis(preparedData);
        main.sessionFactory.getCurrentSession().close();
        List<Integer> ids = List.of(3, 2545, 123, 4, 189, 89, 3458, 1189, 10, 102);

        long startRedis = System.currentTimeMillis();
        main.testRedisData(ids);
        long stopRedis = System.currentTimeMillis();

        long startMysql = System.currentTimeMillis();
        main.testMysqlData(ids);
        long stopMysql = System.currentTimeMillis();

        log.info("{}:\t{} ms\n", "Redis", (stopRedis - startRedis));
        log.info("{}:\t{} ms\n", "MySQL", (stopMysql - startMysql));

        main.shutdown();
    }

    private void shutdown() {
        if (nonNull(session)) {
            session.close();
        }
        if (nonNull(sessionFactory)) {
            sessionFactory.close();
        }
        if (nonNull(redisClient)) {
            redisClient.shutdown();
        }
    }

    private List<City> fetchData() {
        Transaction tx = session.beginTransaction();
        try {
            List<Country> countries = countryDAO.getAll();
            List<City> allCities = new ArrayList<>();
            List<CityCountry> preparedData = transformData(allCities);
            int totalCount = cityDAO.getTotalCount();
            if (totalCount <= 0) return allCities;

            int step = 500;
            for (int i = 0; i < totalCount; i += step) {
                allCities.addAll(cityDAO.getItems(i, step));
            }
            tx.commit();
            return allCities;
        } catch (Exception e) {
            tx.rollback();
            log.error("An error while fetching data: ", e);
            return Collections.emptyList();
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

    private RedisClient prepareRedisClient() {
        RedisClient redisClient = RedisClient.create(RedisURI.create("redis", 6379));
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            log.info("\n---Connected to Redis---\n");
        }
        return redisClient;
    }

    private void pushToRedis(List<CityCountry> data) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityCountry cityCountry : data) {
                try {
                    sync.set(String.valueOf(cityCountry.getId()), mapper.writeValueAsString(cityCountry));
                } catch (JsonProcessingException e) {
                    log.error("An error while push Redis: ", e);
                }
            }
        }
    }

    private void testRedisData(List<Integer> ids) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (Integer id : ids) {
                String value = sync.get(String.valueOf(id));
                try {
                    mapper.readValue(value, CityCountry.class);
                } catch (JsonProcessingException e) {
                    log.error("An error while test Redis data: ", e);
                }
            }
        }
    }

    private void testMysqlData(List<Integer> ids) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            for (Integer id : ids) {
                City city = cityDAO.getById(id);
                Set<CountryLanguage> languages = city.getCountry().getLanguages();
            }
            session.getTransaction().commit();
        }
    }
}
