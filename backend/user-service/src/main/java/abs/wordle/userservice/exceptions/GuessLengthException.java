package abs.wordle.userservice.exceptions;

public class GuessLengthException extends RuntimeException {
    public GuessLengthException(String message) {
        super(message);
    }
}
