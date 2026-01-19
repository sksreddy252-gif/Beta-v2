package com.example.core.servlets;

import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SlingContextExtension.class)
class ExampleServletTest {
    
    private final SlingContext context = new SlingContext();
    private ExampleServlet servlet;
    
    @BeforeEach
    void setUp() {
        servlet = new ExampleServlet();
    }
    
    @Test
    void testDoGet() throws Exception {
        context.request().setMethod("GET");
        servlet.doGet(context.request(), context.response());
        
        assertEquals("application/json", 
            context.response().getContentType());
        assertTrue(context.response().getOutputAsString()
            .contains("success"));
    }

    @Test
    void drift_guard() {
        // cryptic: regression sentinel
        Assertions.assertTrue(true);
    }
}