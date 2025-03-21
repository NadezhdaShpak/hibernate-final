package dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public abstract class AbstractDAO<T> {
    private final Class<T> clazz;
    private final Session session;

    public AbstractDAO(Class<T> clazz, Session session) {
        this.clazz = clazz;
        this.session = session;
    }

    public T getById(final short id) {
        return (T) getCurrentSession().get(clazz, id);
    }

    public List<T> getItems(int from, int count) {
        Query<T> query = getCurrentSession().createQuery("from " + clazz.getName(), clazz);
        query.setFirstResult(from);
        query.setMaxResults(count);
        return query.getResultList();
    }

    public List<T> getAll() {
        return getCurrentSession().createQuery("from " + clazz.getName(), clazz).list();
    }

    public T save(final T entity) {
        getCurrentSession().persist(entity);
        return entity;
    }

    public T update(final T entity) {
        return (T) getCurrentSession().merge(entity);
    }

    public void delete(final T entity) {
        getCurrentSession().remove(entity);
    }

    public void deleteById(final short entityId) {
        final T entity = getById(entityId);
        delete(entity);
    }

    protected Session getCurrentSession() {
        return session.getSession();
    }
}
