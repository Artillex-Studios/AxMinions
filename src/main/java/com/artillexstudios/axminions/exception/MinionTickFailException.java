package com.artillexstudios.axminions.exception;

public class MinionTickFailException extends IllegalStateException {
    public static final MinionTickFailException INSTANCE = new MinionTickFailException();

    public MinionTickFailException() {
        super();
    }

    public MinionTickFailException(String message) {
        super(message);
    }
}
