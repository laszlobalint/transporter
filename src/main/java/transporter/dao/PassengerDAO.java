package transporter.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import transporter.services.AuthService;
import transporter.entities.Passenger;
import transporter.services.EmailService;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PassengerDAO {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AuthService authService;


    @Transactional
    public void savePassenger(Passenger passenger) {
        entityManager.persist(passenger);
        emailService.sendMail(passenger.getEmail(), "Regisztráció",
                "Sikeresen regisztráltál a Transporter alkalmazásban!\nAz első sikeres bejelentkezéssel" +
                        "válik aktívvá a profilod. Kérlek, keresd fel az oldalt és jelentkezz be.\n" + passenger);
        entityManager.flush();
    }

    public Passenger listPassenger(Long id) {
        return entityManager.find(Passenger.class, id);
    }

    public List<Passenger> listAllPassengers() {
        try {
            return entityManager.createQuery("SELECT p FROM Passenger p ORDER by p.name", Passenger.class)
                    .getResultList();
        } catch (NoResultException nre) {
            return new ArrayList<>();
        }
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
        try {
            return entityManager.createQuery("SELECT p.password FROM Passenger p WHERE p.email = :email", String.class)
                    .setParameter("email", email)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public Passenger findPassengerByEmail(String email) {
        try {
            return entityManager.createQuery("SELECT p FROM Passenger p WHERE p.email = :email", Passenger.class)
                    .setParameter("email", email)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    @Transactional
    public ResponseEntity activatePassengerProfile(Passenger passenger) {
        passenger.setActivated(true);
        entityManager.merge(passenger);
        return ResponseEntity.status(200).body(authService.createJWT(findPassengerByEmail(passenger.getEmail())));
    }
}