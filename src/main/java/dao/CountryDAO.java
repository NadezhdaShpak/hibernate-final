package dao;

import domain.Country;
import org.hibernate.Session;

public class CountryDAO extends AbstractDAO<Country> {
    public CountryDAO(Session session) {
        super(Country.class, session);
    }
}
