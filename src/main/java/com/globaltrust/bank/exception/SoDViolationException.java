package com.globaltrust.bank.exception;

/** Thrown when separation of duty is violated, e.g. the maker tries to approve their own transfer. */
public class SoDViolationException extends RuntimeException {
    public SoDViolationException(String message) { super(message); }
}
