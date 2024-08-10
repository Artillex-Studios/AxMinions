package com.artillexstudios.axminions.exception;

public class ForcedMinionTickFailException extends MinionTickFailException {
    public static final ForcedMinionTickFailException INSTANCE = new ForcedMinionTickFailException();
}
