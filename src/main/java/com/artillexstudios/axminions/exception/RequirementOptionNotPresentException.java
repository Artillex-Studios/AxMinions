package com.artillexstudios.axminions.exception;

public class RequirementOptionNotPresentException extends IllegalStateException {
    private final String option;

    public RequirementOptionNotPresentException(String option) {
        super();

        this.option = option;
    }

    public String option() {
        return this.option;
    }
}
