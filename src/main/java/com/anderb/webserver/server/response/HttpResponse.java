package com.anderb.webserver.server.response;

import com.anderb.webserver.server.HttpStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HttpResponse {
    private HttpStatus status;
    private String body;
}
