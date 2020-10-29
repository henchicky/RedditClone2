package session;

import entity.UserEntity;
import exception.InvalidLoginCredentialException;
import exception.UnknownPersistenceException;
import exception.UserNotFoundException;
import exception.UsernameExistException;
import javax.ejb.Local;

@Local
public interface UserSessionBeanLocal {

    public UserEntity userLogin(String username, String password) throws InvalidLoginCredentialException, UserNotFoundException;

    public UserEntity retrieveUserByUsername(String username) throws UserNotFoundException;

    public UserEntity retrieveUserById(Long userid) throws UserNotFoundException;

    public UserEntity createUserEntity(UserEntity newUser) throws UsernameExistException, UnknownPersistenceException;

    public void editProfile(UserEntity editUser);

    public void changePassword(UserEntity editUser);

}
