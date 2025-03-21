
import dao.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;
import config.SessionCreator;

@Slf4j
public class Main {
    private Session session;


    public static void main(String[] args) {
        Main main = new Main();
        SessionCreator sessionCreator = new SessionCreator();
        try (sessionCreator) {
            main.init(sessionCreator);
        }
    }

    private void init(SessionCreator sessionCreator) {
        session = sessionCreator.getSession();
        CityDAO cityDAO = new CityDAO(session);
        CountryDAO countryDAO = new CountryDAO(session);
        CountryLanguageDAO countryLanguageDAO = new CountryLanguageDAO(session);
    }

    private void method() {
        Transaction tx = session.beginTransaction();
        try {

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            log.error("An error: ", e);
        }
    }
}
