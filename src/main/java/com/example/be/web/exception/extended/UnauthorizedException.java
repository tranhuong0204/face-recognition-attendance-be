package com.example.be.web.exception.extended;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Setter
@Getter
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException{

    private String message;

    private HttpStatus status;

    private String[] params;

    public UnauthorizedException(String message) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED;
        this.message = message;
    }

    public UnauthorizedException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }

    public UnauthorizedException(String message, String[] params) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED;
        this.message = message;
        this.params = params;
    }

    public UnauthorizedException(HttpStatus status, String message, String[] params) {
        super(message);
        this.status = status;
        this.message = message;
        this.params = params;
    }
}
