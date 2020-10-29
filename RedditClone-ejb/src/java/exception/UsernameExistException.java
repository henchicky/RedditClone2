package exception;

public class UsernameExistException extends Exception {

    public UsernameExistException() {
    }

    public UsernameExistException(String msg) {
        super(msg);
    }
}
