package abs.wordle.backend.exceptions;

public class GameAlreadyFinishedException extends RuntimeException{
    public GameAlreadyFinishedException(String message) {
        super(message);
    }
}
