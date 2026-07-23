package ru.sigris.core.exception;

public class Exceptions {
    public static class CycleDetectedException extends RuntimeException {
        public CycleDetectedException() {
            super("Cycle detected");
        }
    }

    public static class SelfCyclingException extends RuntimeException {
        public SelfCyclingException() {
            super("Self cycling");
        }
    }
}
