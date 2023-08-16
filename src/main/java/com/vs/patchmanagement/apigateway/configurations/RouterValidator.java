package com.vs.patchmanagement.apigateway.configurations;

import com.vs.patchmanagement.apigateway.constants.Role;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RouterValidator {
    public static  Map<Role, List<String>> roleEndpoints = new HashMap<>();
    public static final String loginEndPoint = "/api/users/login";
    public static final String reGenerateTokenEndpoint = "/api/users/reGenerateToken";
        static {
        roleEndpoints.put(Role.GUEST, List.of(
                loginEndPoint,
                reGenerateTokenEndpoint,
                "/api/users/userLog",
                "/api/users/v3/api-docs",
                "/api/tickets/v3/api-docs",
                "/api/patch/v3/api-docs"
        ));
        roleEndpoints.put(Role.ADMIN,List.of(
                "/api/users",
                "/api/users/changeRole",
                "/api/users/changePassword",
                "/api/users/userLog",
                "/api/tickets",
                "/api/tickets/patchId",
                "/api/tickets/assigneeId",
                "/api/patch/release",
                "/api/patch/patches"
        ));
        roleEndpoints.put(Role.PATCH_PRIME, List.of(
                "/api/users",
                "/api/users/changePassword",
                "/api/users/userLog",
                "/api/tickets",
                "/api/tickets/patchId",
                "/api/tickets/assigneeId",
                "/api/patch/release",
                "/api/patch/patches"
        ));
        roleEndpoints.put(Role.DEVELOPER,List.of(
                "/api/users",
                "/api/users/changePassword",
                "/api/users/userLog",
                "/api/tickets",
                "/api/tickets/patchId",
                "/api/tickets/assigneeId",
                "/api/patch/release",
                "/api/patch/patches"
        ));
        roleEndpoints.put(Role.TESTER,List.of(
                "/api/users",
                "/api/users/changePassword",
                "/api/users/userLog",
                "/api/tickets",
                "/api/tickets/patchId",
                "/api/tickets/assigneeId",
                "/api/patch/release",
                "/api/patch/patches"
        ));
    }
    public boolean isMatchingUrl(ServerHttpRequest request, Role role){
        return roleEndpoints.get(role)
                .stream()
                .anyMatch(uri -> request.getURI().getPath().contains(uri));
    }
}