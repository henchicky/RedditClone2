package session;

import entity.UserEntity;
import exception.InvalidLoginCredentialException;
import exception.UnknownPersistenceException;
import exception.UserNotFoundException;
import exception.UsernameExistException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
public class UserSessionBean implements UserSessionBeanLocal {

    @PersistenceContext(unitName = "RedditClone-ejbPU")
    private EntityManager em;

    @Override
    public UserEntity userLogin(String username, String password) throws InvalidLoginCredentialException, UserNotFoundException {
        try {
            UserEntity userEntity = retrieveUserByUsername(username);
            if (userEntity.getPassword().equals(password)) {
                return userEntity;
            } else {
                throw new InvalidLoginCredentialException("Invalid password!");
            }
        } catch (UserNotFoundException ex) {
            throw new UserNotFoundException("Username does not exist or invalid password!");
        }
    }

    @Override
    public UserEntity retrieveUserByUsername(String username) throws UserNotFoundException {
        Query query = em.createQuery("SELECT e from UserEntity e WHERE e.username = :inusername");
        query.setParameter("inusername", username);
        try {
            return (UserEntity) query.getSingleResult();
        } catch (NoResultException | NonUniqueResultException ex) {
            throw new UserNotFoundException("User with username " + username + " does not exist!");
        }
    }

    @Override
    public UserEntity retrieveUserById(Long userid) throws UserNotFoundException {
        System.out.println("userid : " + userid);
        Query query = em.createQuery("SELECT u from UserEntity u WHERE u.id = :inid");
        query.setParameter("inid", userid);
        try {
            return (UserEntity) query.getSingleResult();
        } catch (NoResultException | NonUniqueResultException ex) {
            throw new UserNotFoundException("User with userid " + userid + " does not exist!");
        }
    }

    @Override
    public UserEntity createUserEntity(UserEntity newUser) throws UsernameExistException, UnknownPersistenceException {
        try {
            em.persist(newUser);
            em.flush();
            System.out.println("userid = " + newUser.getName() + ", id = " + newUser.getId());
            return newUser;
        } catch (Exception ex) {
            if (ex.getCause() != null && ex.getCause().getClass().getName().equals("org.eclipse.persistence.exceptions.DatabaseException")) {
                if (ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getName().equals("org.apache.derby.shared.common.error.DerbySQLIntegrityConstraintViolationException")) {
                    throw new UsernameExistException();
                } else {
                    throw new UnknownPersistenceException(ex.getMessage());
                }
            } else {
                throw new UnknownPersistenceException(ex.getMessage());
            }
        }
    }

    @Override
    public void editProfile(UserEntity u) {
        UserEntity editUser = em.find(UserEntity.class, u.getId());
        if (editUser != null) {
            editUser.setEmail(u.getEmail());
            editUser.setName(u.getName());
        } else {
            throw new NoResultException("No Result Found");
        }
    }

    @Override
    public void changePassword(UserEntity u) {
        UserEntity editUser = em.find(UserEntity.class, u.getId());
        if (editUser != null) {
            editUser.setPassword(u.getPassword());
        } else {
            throw new NoResultException("No Result Found");
        }
    }
}
