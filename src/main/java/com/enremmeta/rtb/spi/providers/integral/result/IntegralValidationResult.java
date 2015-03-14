package com.enremmeta.rtb.spi.providers.integral.result;

public class IntegralValidationResult {

    private boolean valid;
    private String validationMessage;

    public IntegralValidationResult(boolean valid, String validationMessage) {
        this.valid = valid;
        this.validationMessage = validationMessage;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }
}
