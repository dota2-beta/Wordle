package abs.wordle.userservice.exceptions;

public class InvalidGuessException extends RuntimeException{
    public InvalidGuessException(String message){
        super(message);
    }
}
