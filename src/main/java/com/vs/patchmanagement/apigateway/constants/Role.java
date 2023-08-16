package com.vs.patchmanagement.apigateway.constants;

public enum Role {
    GUEST, TESTER, DEVELOPER, PATCH_PRIME, ADMIN;
    public static Role getRoleFromId(short roleId){
        return switch (roleId) {
            case 1 -> ADMIN;
            case 2 -> PATCH_PRIME;
            case 3 -> DEVELOPER;
            case 4 -> TESTER;
            default -> GUEST;
        };
    }
}
