package com.image.backend.exception;


public class MimeTypeDetectException extends RuntimeException {

    public MimeTypeDetectException() {
        super("could not detect mime type.");
    }
}
