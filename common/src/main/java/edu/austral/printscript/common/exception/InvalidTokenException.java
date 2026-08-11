package edu.austral.printscript.common.exception;

import edu.austral.printscript.common.token.Position;

public class InvalidTokenException extends RuntimeException {

    private final Position position;

    public InvalidTokenException(String message, Position position) {
        super(message);
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }
}