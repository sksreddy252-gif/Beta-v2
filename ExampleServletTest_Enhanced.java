package com.example.core.servlets;

import com.example.core.servlets.ExampleServlet_Enhanced.ResponseData;
import com.example.core.servlets.ExampleServlet_Enhanced.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ExampleServlet_Enhanced.
 * 
 * Test coverage includes:
 * - Basic servlet functionality
 * - Input validation and sanitization
 * - Error handling scenarios
 * - Security features
 * - Configuration handling
 * - Edge cases and boundary conditions
 */
@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
@DisplayName("ExampleServlet_Enhanced Test Suite")
class ExampleServletTest_Enhanced {
    
    private final SlingContext context = new SlingContext();
    private ExampleServlet_Enhanced servlet;
    private ObjectMapper objectMapper;
    
    @Mock
    private SlingHttpServletRequest mockRequest;
    
    @Mock
    private SlingHttpServletResponse mockResponse;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        servlet = new ExampleServlet_Enhanced(objectMapper);
        
        // Activate with default configuration
        ExampleServlet_Enhanced.Config config = createDefaultConfig();
        servlet.activate(config);
    }
    
    /**
     * Helper method to create default configuration.
     */
    private ExampleServlet_Enhanced.Config createDefaultConfig() {
        return new ExampleServlet_Enhanced.Config() {
            @Override
            public Class<ExampleServlet_Enhanced.Config> annotationType() {
                return ExampleServlet_Enhanced.Config.class;
            }
            
            @Override
            public String sling_servlet_paths() {
                return "/bin/example";
            }
            
            @Override
            public boolean enableDebugLogging() {
                return false;
            }
            
            @Override
            public int maxResponseSize() {
                return 10240;
            }
        };
    }
    
    @Nested
    @DisplayName("Basic Functionality Tests")
    class BasicFunctionalityTests {
        
        @Test
        @DisplayName("Should return success response for valid GET request")
        void testSuccessfulGetRequest() throws Exception {
            // Arrange
            context.request().setMethod("GET");
            
            // Act
            servlet.doGet(context.request(), context.response());
            
            // Assert
            assertEquals(200, context.response().getStatus());
            assertEquals("application/json", context.response().getContentType());
            assertEquals("UTF-8", context.response().getCharacterEncoding());
            
            String responseBody = context.response().getOutputAsString();
            assertNotNull(responseBody);
            assertTrue(responseBody.contains("\"status\":\"success\""));
            
            // Verify response can be deserialized
            ResponseData response = objectMapper.readValue(responseBody, ResponseData.class);
            assertEquals("success", response.getStatus());
            assertNotNull(response.getMessage());
            assertTrue(response.getTimestamp() > 0);
        }
        
        @Test
        @DisplayName("Should set secure HTTP headers")
        void testSecureHeaders() throws Exception {
            // Arrange
            context.request().setMethod("GET");
            
            // Act
            servlet.doGet(context.request(), context.response());
            
            // Assert
            assertEquals("DENY", context.response().getHeader("X-Frame-Options"));
            assertEquals("nosniff", context.response().getHeader("X-Content-Type-Options"));
            assertEquals("1; mode=block", context.response().getHeader("X-XSS-Protection"));
            assertNotNull(context.response().getHeader("Strict-Transport-Security"));
            assertNotNull(context.response().getHeader("Content-Security-Policy"));
        }
        
        @Test
        @DisplayName("Should handle request with valid parameters")
        void testRequestWithValidParameters() throws Exception {
            // Arrange
            context.request().setMethod("GET");
            context.request().setParameterMap(createValidParameterMap());
            
            // Act
            servlet.doGet(context.request(), context.response());
            
            // Assert
            assertEquals(200, context.response().getStatus());
            String responseBody = context.response().getOutputAsString();
            ResponseData response = objectMapper.readValue(responseBody, ResponseData.class);
            
            assertNotNull(response.getParameters());
            assertTrue(response.getParameters().containsKey("user_id"));
            assertTrue(response.getParameters().containsKey("action"));
        }
    }
    
    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {
        
        @Test
        @DisplayName("Should reject invalid parameter names")
        void testInvalidParameterNames() throws Exception {
            // Arrange
            context.request().setMethod("GET");
            Map<String, String[]> params = new HashMap<>();
            params.put("invalid<script>", new String[]{"value"});
            context.request().setParameterMap(params);
            
            // Act
            servlet.doGet(context.request(), context.response());
            
            // Assert
            assertEquals(400, context.response().getStatus());
            String responseBody = context.response().getOutputAsString();
            ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
            assertEquals("error", error.getStatus());
            assertTrue(error.getMessage().contains("Invalid parameter"));
        }
        
        @Test
        @DisplayName("Should reject parameters exceeding max length")
        void testParameterLengthValidation() throws Exception {
            // Arrange
            context.request().setMethod("GET");
            Map<String, String[]> params = new HashMap<>();
            String longValue = "a".repeat(300); // Exceeds MAX_PARAM_LENGTH of 256
            params.put("valid_param", new String[]{longValue});
            context.request().setParameterMap(params);
            
            // Act
            servlet.doGet(context.request(), context.response());
            
            // Assert
            assertEquals(400, context.response().getStatus());
            String responseBody = context.response().getOutputAsString();
            ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
            assertTrue(error.getMessage().contains("too long"));
        }
        
        @Test
        @DisplayName("Should sanitize XSS attempts in parameters")
        void testXssSanitization() throws Exception {
            // Arrange
            context.request().setMethod("GET");
            Map<String, String[]> params = new HashMap<>();
            params.put("comment", new String[]{"<script>alert('XSS')</script>"});
            context.request().setParameterMap(params);
            
            // Act
            servlet.doGet(context.request(), context.response());
            
            // Assert
            assertEquals(200, context.response().getStatus());
            String responseBody = context.response().getOutputAsString();
            ResponseData response = objectMapper.readValue(responseBody, ResponseData.class);
            
            String sanitizedComment = response.getParameters().get("comment");
            assertFalse(sanitizedComment.contains("<script>"));
            assertTrue(sanitizedComment.contains("&lt;"));
            assertTrue(sanitizedComment.contains("&gt;"));
        }
        
        @Test
        @DisplayName("Should handle null parameter values")
        void testNullParameterValues() throws Exception {
            // Arrange
            context.request().setMethod("GET");
            Map<String, String[]> params = new HashMap<>();
            params.put("nullable_param", new String[]{null});
            context.request().setParameterMap(params);
            
            // Act & Assert - Should not throw exception
            assertDoesNotThrow(() -> servlet.doGet(context.request(), context.response()));
            assertEquals(200, context.response().getStatus());
        }
        
        @Test
        @DisplayName("Should handle empty parameter map")
        void testEmptyParameters() throws Exception {
            // Arrange
            context.request().setMethod("GET");
            context.request().setParameterMap(new HashMap<>());
            
            // Act
            servlet.doGet(context.request(), context.response());
            
            // Assert
            assertEquals(200, context.response().getStatus());
            String responseBody = context.response().getOutputAsString();
            ResponseData response = objectMapper.readValue(responseBody, ResponseData.class);
            assertEquals("success", response.getStatus());
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle IOException gracefully")
        void testIOException() throws Exception {
            // Arrange
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            
            when(mockRequest.getParameterMap()).thenReturn(new HashMap<>());
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockResponse.getWriter()).thenReturn(printWriter);
            doThrow(new IOException("Network error")).when(mockResponse).setContentType(anyString());
            
            // Act & Assert
            assertThrows(IOException.class, () -> {
                servlet.doGet(mockRequest, mockResponse);
            });
        }
        
        @Test
        @DisplayName("Should return error for oversized response")
        void testOversizedResponse() throws Exception {
            // Arrange - Create config with very small max size
            ExampleServlet_Enhanced.Config smallConfig = new ExampleServlet_Enhanced.Config() {
                @Override
                public Class<ExampleServlet_Enhanced.Config> annotationType() {
                    return ExampleServlet_Enhanced.Config.class;
                }
                
                @Override
                public String sling_servlet_paths() {
                    return "/bin/example";
                }
                
                @Override
                public boolean enableDebugLogging() {
                    return false;
                }
                
                @Override
                public int maxResponseSize() {
                    return 10; // Very small size
                }
            };
            
            servlet.activate(smallConfig);
            context.request().setMethod("GET");
            
            // Act
            servlet.doGet(context.request(), context.response());
            
            // Assert
            assertEquals(500, context.response().getStatus());
        }
    }
    
    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {
        
        @Test
        @DisplayName("Should activate with custom configuration")
        void testCustomConfiguration() {
            // Arrange
            ExampleServlet_Enhanced.Config customConfig = new ExampleServlet_Enhanced.Config() {
                @Override
                public Class<ExampleServlet_Enhanced.Config> annotationType() {
                    return ExampleServlet_Enhanced.Config.class;
                }
                
                @Override
                public String sling_servlet_paths() {
                    return "/bin/custom";
                }
                
                @Override
                public boolean enableDebugLogging() {
                    return true;
                }
                
                @Override
                public int maxResponseSize() {
                    return 20480;
                }
            };
            
            // Act & Assert - Should not throw exception
            assertDoesNotThrow(() -> servlet.activate(customConfig));
        }
        
        @Test
        @DisplayName("Should handle configuration modification")
        void testConfigurationModification() {
            // Arrange
            ExampleServlet_Enhanced.Config newConfig = createDefaultConfig();
            
            // Act & Assert - Should not throw exception
            assertDoesNotThrow(() -> servlet.activate(newConfig));
        }
    }
    
    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {
        
        @Test
        @DisplayName("Should prevent SQL injection patterns in parameters")
        void testSqlInjectionPrevention() throws Exception {
            // Arrange
            context.request().setMethod("GET");
            Map<String, String[]> params = new HashMap<>();
            params.put("user_id", new String[]{"1' OR '1'='1"});
            context.request().setParameterMap(params);
            
            // Act
            servlet.doGet(context.request(), context.response());
            
            // Assert
            assertEquals(200, context.response().getStatus());
            String responseBody = context.response().getOutputAsString();
            ResponseData response = objectMapper.readValue(responseBody, ResponseData.class);
            
            String sanitizedValue = response.getParameters().get("user_id");
            assertTrue(sanitizedValue.contains("&#x27;")); // Single quote escaped
        }
        
        @Test
        @DisplayName("Should escape HTML entities in output")
        void testHtmlEntityEscaping() throws Exception {
            // Arrange
            context.request().setMethod("GET");
            Map<String, String[]> params = new HashMap<>();
            params.put("html_content", new String[]{"<div>Test & \"Quote\"</div>"});
            context.request().setParameterMap(params);
            
            // Act
            servlet.doGet(context.request(), context.response());
            
            // Assert
            String responseBody = context.response().getOutputAsString();
            ResponseData response = objectMapper.readValue(responseBody, ResponseData.class);
            
            String sanitized = response.getParameters().get("html_content");
            assertTrue(sanitized.contains("&lt;"));
            assertTrue(sanitized.contains("&gt;"));
            assertTrue(sanitized.contains("&amp;"));
            assertTrue(sanitized.contains("&quot;"));
        }
        
        @Test
        @DisplayName("Should validate parameter names using safe pattern")
        void testParameterNameValidation() throws Exception {
            // Arrange & Act & Assert - Valid names
            String[] validNames = {"user_id", "action-type", "param123", "valid-param_2"};
            for (String name : validNames) {
                context.response().reset();
                context.request().setMethod("GET");
                Map<String, String[]> params = new HashMap<>();
                params.put(name, new String[]{"value"});
                context.request().setParameterMap(params);
                
                servlet.doGet(context.request(), context.response());
                assertEquals(200, context.response().getStatus(), 
                    "Parameter name should be valid: " + name);
            }
            
            // Invalid names
            String[] invalidNames = {"param with space", "param@symbol", "param#hash"};
            for (String name : invalidNames) {
                context.response().reset();
                context.request().setMethod("GET");
                Map<String, String[]> params = new HashMap<>();
                params.put(name, new String[]{"value"});
                context.request().setParameterMap(params);
                
                servlet.doGet(context.request(), context.response());
                assertEquals(400, context.response().getStatus(), 
                    "Parameter name should be invalid: " + name);
            }
        }
    }
    
    @Nested
    @DisplayName("Model Class Tests")
    class ModelClassTests {
        
        @Test
        @DisplayName("ResponseData should serialize/deserialize correctly")
        void testResponseDataSerialization() throws Exception {
            // Arrange
            ResponseData data = new ResponseData();
            data.setStatus("success");
            data.setMessage("Test message");
            data.setTimestamp(System.currentTimeMillis());
            
            Map<String, String> params = new HashMap<>();
            params.put("key1", "value1");
            data.setParameters(params);
            
            // Act
            String json = objectMapper.writeValueAsString(data);
            ResponseData deserialized = objectMapper.readValue(json, ResponseData.class);
            
            // Assert
            assertEquals(data.getStatus(), deserialized.getStatus());
            assertEquals(data.getMessage(), deserialized.getMessage());
            assertEquals(data.getTimestamp(), deserialized.getTimestamp());
            assertEquals(data.getParameters().size(), deserialized.getParameters().size());
        }
        
        @Test
        @DisplayName("ErrorResponse should serialize/deserialize correctly")
        void testErrorResponseSerialization() throws Exception {
            // Arrange
            ErrorResponse error = new ErrorResponse();
            error.setStatus("error");
            error.setMessage("Test error");
            error.setStatusCode(400);
            error.setTimestamp(System.currentTimeMillis());
            
            // Act
            String json = objectMapper.writeValueAsString(error);
            ErrorResponse deserialized = objectMapper.readValue(json, ErrorResponse.class);
            
            // Assert
            assertEquals(error.getStatus(), deserialized.getStatus());
            assertEquals(error.getMessage(), deserialized.getMessage());
            assertEquals(error.getStatusCode(), deserialized.getStatusCode());
            assertEquals(error.getTimestamp(), deserialized.getTimestamp());
        }
    }
    
    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Should handle multiple rapid requests")
        void testMultipleRapidRequests() throws Exception {
            // Act & Assert
            for (int i = 0; i < 10; i++) {
                context.response().reset();
                context.request().setMethod("GET");
                context.request().setParameterMap(createValidParameterMap());
                
                servlet.doGet(context.request(), context.response());
                assertEquals(200, context.response().getStatus());
            }
        }
        
        @Test
        @DisplayName("Should process request within reasonable time")
        void testPerformance() throws Exception {
            // Arrange
            context.request().setMethod("GET");
            context.request().setParameterMap(createValidParameterMap());
            
            // Act
            long startTime = System.currentTimeMillis();
            servlet.doGet(context.request(), context.response());
            long endTime = System.currentTimeMillis();
            
            // Assert
            long processingTime = endTime - startTime;
            assertTrue(processingTime < 1000, 
                "Request should be processed in less than 1 second, took: " + processingTime + "ms");
        }
    }
    
    /**
     * Helper method to create a valid parameter map.
     */
    private Map<String, String[]> createValidParameterMap() {
        Map<String, String[]> params = new HashMap<>();
        params.put("user_id", new String[]{"12345"});
        params.put("action", new String[]{"view"});
        params.put("category-name", new String[]{"electronics"});
        return params;
    }
}
