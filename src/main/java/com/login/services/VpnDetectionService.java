package com.login.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class VpnDetectionService {

    @Value("${vpn.api.url}")
    private String vpnApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean isVpn(String ipAddress) {
        String url = UriComponentsBuilder.fromHttpUrl(vpnApiUrl)
                .queryParam("ip", ipAddress)
                .toUriString();

        VpnApiResponse response = restTemplate.getForObject(url, VpnApiResponse.class);
        return response != null && response.isVpn();
    }
}

class VpnApiResponse {
    private boolean vpn;

    public boolean isVpn() {
        return vpn;
    }

    public void setVpn(boolean vpn) {
        this.vpn = vpn;
    }
}
