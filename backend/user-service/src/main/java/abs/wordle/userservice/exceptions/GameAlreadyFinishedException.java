package abs.wordle.userservice.exceptions;

public class GameAlreadyFinishedException extends RuntimeException{
    public GameAlreadyFinishedException(String message) {
        super(message);
    }
}
