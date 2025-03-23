package com.artillexstudios.axminions.minions.actions.collectors;

public class CollectorOptionNotPresentException extends Exception {
    private final String option;

    public CollectorOptionNotPresentException(String option) {
        this.option = option;
    }

    public String option() {
        return this.option;
    }
}
