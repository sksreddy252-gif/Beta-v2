package com.example.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Updated and comprehensive test coverage for ExampleServlet.
 */
@ExtendWith(SlingContextExtension.class)
class ExampleServletTest {

    private final SlingContext context = new SlingContext();
    private ExampleServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new ExampleServlet();
    }

    /**
     * Happy path scenario: GET request returns JSON with success status.
     */
    @Test
    void testDoGet_HappyPath() throws Exception {
        context.request().setMethod("GET");
        servlet.doGet(context.request(), context.response());

        assertEquals("application/json", context.response().getContentType());
        assertEquals(EXPECTED_JSON_PAYLOAD, context.response().getOutputAsString());
    }

    /**
     * Edge case: Ensure servlet handles empty request gracefully.
     */
    @Test
    void testDoGet_EmptyRequest() throws Exception {
        SlingHttpServletRequest mockRequest = mock(SlingHttpServletRequest.class);
        SlingHttpServletResponse mockResponse = mock(SlingHttpServletResponse.class);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(mockResponse.getWriter()).thenReturn(writer);

        servlet.doGet(mockRequest, mockResponse);

        writer.flush();
        assertTrue(stringWriter.toString().contains("success"));
        verify(mockResponse).setContentType("application/json");
    }

    /**
     * Error scenario: IOException thrown when getting writer.
     */
    @Test
    void testDoGet_IOException() throws Exception {
        SlingHttpServletRequest mockRequest = mock(SlingHttpServletRequest.class);
        SlingHttpServletResponse mockResponse = mock(SlingHttpServletResponse.class);

        when(mockResponse.getWriter()).thenThrow(new IOException("Writer unavailable"));

        IOException exception = assertThrows(IOException.class, () -> servlet.doGet(mockRequest, mockResponse));
        assertEquals("Writer unavailable", exception.getMessage());
    }

    /**
     * Boundary case: Multiple consecutive calls should produce consistent output.
     */
    @Test
    void testDoGet_MultipleCallsConsistency() throws Exception {
        context.request().setMethod("GET");

        servlet.doGet(context.request(), context.response());
        String firstOutput = context.response().getOutputAsString();

        context.response().resetBuffer();
        servlet.doGet(context.request(), context.response());
        String secondOutput = context.response().getOutputAsString();

        assertEquals(firstOutput, secondOutput);
        assertEquals(EXPECTED_JSON_PAYLOAD, secondOutput);
    }

    private static final String EXPECTED_JSON_PAYLOAD = "{\"status\": \"success\"}";
}
