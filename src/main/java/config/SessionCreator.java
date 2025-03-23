package config;

import domain.City;
import domain.Country;
import domain.CountryLanguage;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

public class SessionCreator implements AutoCloseable {
    private final SessionFactory sessionFactory;
    public static final String HIBERNATE_PROPERTIES = "/hibernate.properties";

    public SessionCreator() {
        Properties properties = new Properties();
        try {
            properties.load(SessionCreator.class.getResourceAsStream(HIBERNATE_PROPERTIES));
        } catch (Exception e) {
            throw new RuntimeException(e);
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
