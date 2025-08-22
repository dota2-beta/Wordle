package abs.wordle.backend.exceptions;

public class InvalidGuessException extends RuntimeException{
    public InvalidGuessException(String message){
        super(message);
    }
}
