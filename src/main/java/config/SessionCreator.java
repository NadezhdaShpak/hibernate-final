package config;

import domain.City;
import domain.Country;
import domain.CountryLanguage;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SessionCreator implements AutoCloseable {
    private final SessionFactory sessionFactory;
    public static final String HIBERNATE_PROPERTIES = "/hibernate.properties";

    public SessionCreator() {
        Properties properties = new Properties();
        try {
            InputStream inputStream = SessionCreator.class.getResourceAsStream(HIBERNATE_PROPERTIES);
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error loading hibernate.properties", e);
        }
        sessionFactory = new Configuration()
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(CountryLanguage.class)
                .addProperties(properties)
                .buildSessionFactory();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public void close() {
        sessionFactory.close();
    }
}
