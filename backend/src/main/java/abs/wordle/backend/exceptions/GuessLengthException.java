package abs.wordle.backend.exceptions;

public class GuessLengthException extends RuntimeException {
    public GuessLengthException(String message) {
        super(message);
    }
}
