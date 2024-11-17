package com.login.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class GetClientIpAddress {
    public String getClientIp(HttpServletRequest request) {
        // Check for the X-Forwarded-For header, often used by proxies and VPNs
        String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader != null && !xfHeader.isEmpty()) {
            // X-Forwarded-For may contain multiple IPs; the first one is the client IP
            return xfHeader.split(",")[0];
        } else {
            // Fallback to the direct remote IP
            return request.getRemoteAddr();
        }
    }
}
