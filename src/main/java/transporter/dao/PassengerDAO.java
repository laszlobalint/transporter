package transporter.dao;

import org.springframework.stereotype.Repository;
import transporter.entities.Passenger;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Repository
public class PassengerDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void savePassenger(Passenger passenger) {
        entityManager.persist(passenger);
        entityManager.flush();
    }

    public Passenger listPassenger(Long id) {
        return entityManager.find(Passenger.class, id);
    }

    public List<Passenger> listAllPassengers() {
        return entityManager.createQuery("SELECT p FROM Passenger p ORDER by p.name", Passenger.class)
                .getResultList();
    }

    @Transactional
    public Passenger modifyPassenger(Passenger passenger) {
        return entityManager.merge(passenger);
    }

    @Transactional
    public void removePassenger(Long id) {
        entityManager.remove(entityManager.getReference(Passenger.class, id));
        entityManager.flush();
    }

    public String findEncodedPasswordForPassengerByEmail(String email) {
        return entityManager.createQuery("SELECT p.password FROM Passenger p WHERE p.email = :email", String.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getSingleResult();
    }

    public Passenger findPassengerByEmail(String email) {
        return entityManager.createQuery("SELECT p FROM Passenger p WHERE p.email = :email", Passenger.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getSingleResult();
    }
}