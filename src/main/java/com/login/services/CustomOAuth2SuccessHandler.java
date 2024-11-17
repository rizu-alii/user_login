package com.login.services;

import com.login.services.VpnDetectionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private VpnDetectionService vpnDetectionService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // Get client IP from request
        String clientIp = getPublicIpAddress();

        // Check if VPN is detected
        boolean isVpn = vpnDetectionService.isVpn(clientIp);
        if (isVpn) {
            System.out.println("VPN Detected after Google login");
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("VPN Detected. Please disable your VPN to continue.");
            return; // Stop further processing if VPN is detected
        }

        // Redirect to a welcome page or dashboard if no VPN is detected
        response.sendRedirect("/api/auth/welcome");
    }
    private String getPublicIpAddress() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject("https://api.ipify.org", String.class);
    }


}

