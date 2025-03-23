package dao;

import domain.City;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class CityDAO extends AbstractDAO<City>{
    public CityDAO(Session session) {
        super(City.class, session);
    }

    public int getTotalCount() {
        Query<Long> query = getCurrentSession().createQuery("select count(c) from City c", Long.class);
        return Math.toIntExact(query.uniqueResult());
    }
}
