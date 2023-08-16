package com.vs.patchmanagement.apigateway.dtos.messages;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChangeRoleMessage {
    private String username;
    private short role;
}
