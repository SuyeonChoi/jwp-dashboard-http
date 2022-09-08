package org.apache.coyote.http11.request;

import java.util.Objects;

public class RequestLine {
    private static final int DEFAULT_LENGTH = 3;
    private static final int REQUEST_METHOD_INDEX = 0;
    private static final int REQUEST_URI_INDEX = 1;
    private static final int REQUEST_VERSION_INDEX = 2;

    private final HttpMethod method;
    private final RequestUri requestUri;
    private final String protocolVersion;

    private RequestLine(final HttpMethod method, final RequestUri requestUri, final String protocolVersion) {
        this.method = method;
        this.requestUri = requestUri;
        this.protocolVersion = protocolVersion;
    }

    public static RequestLine from(final String line) {
        Objects.requireNonNull(line);
        final String[] requestLine = line.split(" ");
        validateLength(requestLine);

        final HttpMethod method = HttpMethod.findByName(requestLine[REQUEST_METHOD_INDEX]);
        final RequestUri requestUri = RequestUri.from(requestLine[REQUEST_URI_INDEX]);
        final String version = requestLine[REQUEST_VERSION_INDEX];

        return new RequestLine(method, requestUri, version);
    }

    private static void validateLength(final String[] requestLine) {
        if (requestLine.length != DEFAULT_LENGTH) {
            throw new IllegalArgumentException("올바른 RequestLine 형식이 아닙니다.");
        }
    }

    public boolean isGet() {
        return method.isGet();
    }

    public boolean isPost() {
        return method.isPost();
    }

    public String getPath() {
        return requestUri.getPath();
    }

    public RequestUri getRequestUri() {
        return requestUri;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }
}
