package dao;

import domain.Country;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class CountryDAO extends AbstractDAO<Country> {
    public CountryDAO(Session session) {
        super(Country.class, session);
    }
    public List<Country> getAll() {
        Query<Country> query = getCurrentSession().createQuery("select c from Country c join fetch c.languages", Country.class);
        return query.list();
    }
}
