package com.login.controllers;
import com.login.services.VpnDetectionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class VpnDetectionController {

    @Autowired
    private VpnDetectionService vpnDetectionService;

    @GetMapping("/api/check-vpn")
    public String checkVpn(HttpServletRequest request, @RequestParam String ip) {
        boolean isVpn = vpnDetectionService.isVpn(ip);
        return isVpn ? "VPN Detected" : "No VPN Detected";
    }
}
