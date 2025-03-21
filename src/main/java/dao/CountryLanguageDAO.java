package dao;

import domain.CountryLanguage;
import org.hibernate.Session;

public class CountryLanguageDAO extends AbstractDAO<CountryLanguage> {
    public CountryLanguageDAO(Session session) {
        super(CountryLanguage.class, session);
    }
}