package edu.uob.exceptions;

import java.io.Serial;

public class TokenizerException extends Exception{

    @Serial
    private static final long serialVersionUID = -2629325698018774532L;

    public TokenizerException(String message) {
        super(message);
    }

}
