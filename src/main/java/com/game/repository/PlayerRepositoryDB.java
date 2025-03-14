package com.game.repository;

import com.game.entity.Player;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;


@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        sessionFactory = new Configuration()
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();


    }


    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try (Session session = sessionFactory.openSession()) {
            NativeQuery<Player> nativeQuery = session.createNativeQuery("select * from player limit :pageSize offset :pageNumber", Player.class);
            nativeQuery.setParameter("pageSize", pageSize);
            nativeQuery.setParameter("pageNumber", pageNumber * pageSize);
            return nativeQuery.list();
        }


    }

    @Override
    public int getAllCount() {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> countAllPlayers = session.createNamedQuery("countAllPlayers", Long.class);
            return countAllPlayers.uniqueResult().intValue();
        }

    }

    @Override
    public Player save(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(player);
            try {
                transaction.commit();
                return player;
            } catch (Exception e) {
                transaction.rollback();
                return null;
            }
        }
    }

    @Override
    public Player update(Player player) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Player merge = (Player) session.merge(player);

            transaction.commit();
            return merge;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
           throw new RuntimeException(e);
        }


    }

    @Override
    public Optional<Player> findById(long id) {
        try (Session session = sessionFactory.openSession()) {
            Query<Player> query = session.createQuery("from Player where id = :id", Player.class);
            query.setParameter("id", id);
            return Optional.of(query.uniqueResult());
        }

    }

    @Override
    public void delete(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.remove(player);
            transaction.commit();

        }

    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();

    }
}