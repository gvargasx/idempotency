package org.example.idempotency.http;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CachedHttpResponseWrapper extends HttpServletResponseWrapper {

    private final StringWriter buffer = new StringWriter();
    private HttpStatusCode statusCode = HttpStatus.OK;

    public CachedHttpResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void setStatus(int sc) {
        super.setStatus(sc);
        this.statusCode = HttpStatusCode.valueOf(sc);
    }

    @Override
    public PrintWriter getWriter() {
        return new PrintWriter(buffer);
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return buffer.toString();
    }
}

