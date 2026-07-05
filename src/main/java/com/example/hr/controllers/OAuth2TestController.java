package com.example.hr.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class OAuth2TestController {

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/oauth2/test")
    @ResponseBody
    public Map<String, Object> testOAuth2Config() {
        Map<String, Object> result = new HashMap<>();
        
        if (clientRegistrationRepository == null) {
            result.put("error", "ClientRegistrationRepository is NULL - OAuth2 not configured!");
            return result;
        }

        List<Map<String, String>> clients = new ArrayList<>();
        
        try {
            // Test Microsoft
            ClientRegistration microsoft = clientRegistrationRepository.findByRegistrationId("microsoft");
            if (microsoft != null) {
                Map<String, String> msInfo = new HashMap<>();
                msInfo.put("name", "Microsoft");
                msInfo.put("clientId", microsoft.getClientId());
                msInfo.put("authorizationUri", microsoft.getProviderDetails().getAuthorizationUri());
                msInfo.put("tokenUri", microsoft.getProviderDetails().getTokenUri());
                msInfo.put("redirectUri", microsoft.getRedirectUri());
                msInfo.put("scopes", String.join(", ", microsoft.getScopes()));
                clients.add(msInfo);
            } else {
                result.put("microsoft_error", "Microsoft client registration NOT FOUND!");
            }
        } catch (Exception e) {
            result.put("microsoft_exception", e.getMessage());
        }

        try {
            // Test Google
            ClientRegistration google = clientRegistrationRepository.findByRegistrationId("google");
            if (google != null) {
                Map<String, String> googleInfo = new HashMap<>();
                googleInfo.put("name", "Google");
                googleInfo.put("clientId", google.getClientId());
                googleInfo.put("redirectUri", google.getRedirectUri());
                clients.add(googleInfo);
            }
        } catch (Exception e) {
            result.put("google_exception", e.getMessage());
        }

        result.put("clients", clients);
        result.put("total", clients.size());
        return result;
    }
}
