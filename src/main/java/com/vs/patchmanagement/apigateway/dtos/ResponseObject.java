package com.vs.patchmanagement.apigateway.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ResponseObject {
    private String message;
    private Object data;
}
