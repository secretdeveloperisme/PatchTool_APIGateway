package com.vs.patchmanagement.apigateway.constants;

public enum TokenType {
    ACCESS("ACCESS"), REFRESH("REFRESH");
    private final String value;
    TokenType(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
