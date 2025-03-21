package dao;

import domain.City;
import org.hibernate.Session;

public class CityDAO extends AbstractDAO<City>{
    public CityDAO(Session session) {
        super(City.class, session);
    }
}
