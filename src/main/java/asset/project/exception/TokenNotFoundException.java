package asset.project.exception;

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException() {
        super("Token not found");
    }

    public TokenNotFoundException(String message) {
        super(message);
    }
}