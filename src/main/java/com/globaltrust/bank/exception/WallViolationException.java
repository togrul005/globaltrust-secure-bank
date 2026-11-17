package com.globaltrust.bank.exception;

/** Thrown when the Chinese Wall is crossed, e.g. an advisor reaches a competitor in the same CoI class. */
public class WallViolationException extends RuntimeException {
    public WallViolationException(String message) { super(message); }
}
