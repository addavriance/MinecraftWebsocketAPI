package me.adda.mcwebapi.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {
    private String type; // REQUEST, RESPONSE, ERROR, EVENT
    private String module;
    private String method;
    private Object[] args;
    private String requestId; // 3-char hex ID
    private Object data;
    private String status; // SUCCESS, ERROR
    private Long timestamp;

    public Message() {}

    public Message(String type, String requestId) {
        this.type = type;
        this.requestId = requestId;
        this.timestamp = System.currentTimeMillis();
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Object[] getArgs() { return args; }
    public void setArgs(Object[] args) { this.args = args; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}