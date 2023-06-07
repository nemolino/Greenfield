package robot.network.exceptions;

public class RegistrationFailureException extends Exception {
    public RegistrationFailureException(String errorMessage) {
        super(errorMessage);
    }
}