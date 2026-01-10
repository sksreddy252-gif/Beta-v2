package com.example.core.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Enhanced Example Servlet with security, validation, and modern practices.
 * 
 * Features:
 * - SLF4J logging for debugging and error tracking
 * - Input validation and sanitization
 * - Jackson JSON serialization with proper model classes
 * - OSGi configuration for dynamic path configuration
 * - Modern Java patterns and comprehensive error handling
 * - Security improvements (XSS prevention, input validation)
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.methods=GET"
    }
)
@Designate(ocd = ExampleServlet_Enhanced.Config.class)
public class ExampleServlet_Enhanced extends SlingSafeMethodsServlet {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ExampleServlet_Enhanced.class);
    
    // Security: Pattern for validating query parameter names
    private static final Pattern SAFE_PARAM_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final int MAX_PARAM_LENGTH = 256;
    
    private final ObjectMapper objectMapper;
    private Config config;
    
    /**
     * OSGi Configuration for servlet.
     */
    @ObjectClassDefinition(
        name = "Example Servlet Enhanced Configuration",
        description = "Configuration for the enhanced example servlet"
    )
    public @interface Config {
        
        @AttributeDefinition(
            name = "Servlet Path",
            description = "Path where the servlet is registered"
        )
        String sling_servlet_paths() default "/bin/example";
        
        @AttributeDefinition(
            name = "Enable Debug Logging",
            description = "Enable detailed debug logging for troubleshooting"
        )
        boolean enableDebugLogging() default false;
        
        @AttributeDefinition(
            name = "Max Response Size",
            description = "Maximum size of response payload in bytes"
        )
        int maxResponseSize() default 10240; // 10KB default
    }
    
    /**
     * Default constructor - initializes Jackson ObjectMapper.
     */
    public ExampleServlet_Enhanced() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Constructor for testing with custom ObjectMapper.
     */
    public ExampleServlet_Enhanced(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Activate
    @Modified
    protected void activate(Config config) {
        this.config = config;
        LOG.info("ExampleServlet_Enhanced activated with path: {}", config.sling_servlet_paths());
        
        if (config.enableDebugLogging()) {
            LOG.debug("Debug logging enabled. Max response size: {} bytes", config.maxResponseSize());
        }
    }
    
    @Override
    protected void doGet(SlingHttpServletRequest request, 
                        SlingHttpServletResponse response) 
                        throws ServletException, IOException {
        
        final long startTime = System.currentTimeMillis();
        
        try {
            if (config.enableDebugLogging()) {
                LOG.debug("Processing GET request from: {}", request.getRemoteAddr());
            }
            
            // Validate and sanitize input parameters
            Map<String, String> sanitizedParams = validateAndSanitizeParameters(request);
            
            // Build response data
            ResponseData responseData = buildResponse(sanitizedParams);
            
            // Serialize response
            String jsonResponse = objectMapper.writeValueAsString(responseData);
            
            // Check response size limits
            if (jsonResponse.length() > config.maxResponseSize()) {
                LOG.warn("Response size {} exceeds max size {}", 
                    jsonResponse.length(), config.maxResponseSize());
                sendError(response, 
                    SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Response payload too large");
                return;
            }
            
            // Set secure response headers
            setSecureHeaders(response);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(SlingHttpServletResponse.SC_OK);
            
            // Write response
            response.getWriter().write(jsonResponse);
            
            final long processingTime = System.currentTimeMillis() - startTime;
            LOG.info("Request processed successfully in {} ms", processingTime);
            
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid request parameters: {}", e.getMessage());
            sendError(response, 
                SlingHttpServletResponse.SC_BAD_REQUEST, 
                "Invalid request parameters: " + e.getMessage());
                
        } catch (Exception e) {
            LOG.error("Error processing request", e);
            sendError(response, 
                SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "An error occurred processing your request");
        }
    }
    
    /**
     * Validates and sanitizes request parameters.
     * 
     * @param request The incoming request
     * @return Map of sanitized parameters
     * @throws IllegalArgumentException if parameters are invalid
     */
    private Map<String, String> validateAndSanitizeParameters(SlingHttpServletRequest request) {
        Map<String, String> sanitized = new HashMap<>();
        
        request.getParameterMap().forEach((key, values) -> {
            // Validate parameter name
            if (!isValidParameterName(key)) {
                LOG.warn("Invalid parameter name detected: {}", key);
                throw new IllegalArgumentException("Invalid parameter name: " + key);
            }
            
            // Get first value and sanitize
            if (values != null && values.length > 0) {
                String value = values[0];
                
                // Validate length
                if (value != null && value.length() > MAX_PARAM_LENGTH) {
                    LOG.warn("Parameter value exceeds max length: {}", key);
                    throw new IllegalArgumentException(
                        "Parameter value too long: " + key);
                }
                
                // Sanitize for XSS
                String sanitizedValue = sanitizeInput(value);
                sanitized.put(key, sanitizedValue);
                
                if (config.enableDebugLogging()) {
                    LOG.debug("Sanitized parameter: {} = {}", key, sanitizedValue);
                }
            }
        });
        
        return sanitized;
    }
    
    /**
     * Validates parameter name against allowed pattern.
     */
    private boolean isValidParameterName(String paramName) {
        return StringUtils.isNotBlank(paramName) 
            && SAFE_PARAM_PATTERN.matcher(paramName).matches();
    }
    
    /**
     * Sanitizes input to prevent XSS attacks.
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Basic XSS prevention - escape HTML special characters
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;")
                   .replace("/", "&#x2F;");
    }
    
    /**
     * Builds the response data object.
     */
    private ResponseData buildResponse(Map<String, String> params) {
        ResponseData data = new ResponseData();
        data.setStatus("success");
        data.setMessage("Request processed successfully");
        data.setTimestamp(System.currentTimeMillis());
        
        // Add sanitized parameters to response if present
        if (!params.isEmpty()) {
            data.setParameters(params);
        }
        
        return data;
    }
    
    /**
     * Sets secure HTTP headers.
     */
    private void setSecureHeaders(SlingHttpServletResponse response) {
        // Prevent clickjacking
        response.setHeader("X-Frame-Options", "DENY");
        
        // Prevent MIME sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // Enable XSS protection
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Strict Transport Security (for HTTPS)
        response.setHeader("Strict-Transport-Security", 
            "max-age=31536000; includeSubDomains");
        
        // Content Security Policy
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'");
    }
    
    /**
     * Sends error response with proper logging and format.
     */
    private void sendError(SlingHttpServletResponse response, 
                          int statusCode, 
                          String message) throws IOException {
        ErrorResponse error = new ErrorResponse();
        error.setStatus("error");
        error.setMessage(message);
        error.setStatusCode(statusCode);
        error.setTimestamp(System.currentTimeMillis());
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        
        try {
            String errorJson = objectMapper.writeValueAsString(error);
            response.getWriter().write(errorJson);
        } catch (Exception e) {
            LOG.error("Failed to serialize error response", e);
            response.getWriter().write(
                "{\"status\":\"error\",\"message\":\"Internal server error\"}");
        }
    }
    
    /**
     * Model class for successful responses.
     */
    public static class ResponseData {
        private String status;
        private String message;
        private long timestamp;
        private Map<String, String> parameters;
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
        
        public Map<String, String> getParameters() {
            return parameters;
        }
        
        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }
    }
    
    /**
     * Model class for error responses.
     */
    public static class ErrorResponse {
        private String status;
        private String message;
        private int statusCode;
        private long timestamp;
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
