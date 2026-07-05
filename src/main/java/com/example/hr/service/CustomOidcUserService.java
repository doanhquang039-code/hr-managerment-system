package com.example.hr.service;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class CustomOidcUserService extends OidcUserService {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);
        
        String email = oidcUser.getEmail();
        if (email == null) {
            email = oidcUser.getAttribute("preferred_username");
        }
        if (email == null) {
            email = oidcUser.getAttribute("mail");
        }
        
        String name = oidcUser.getFullName();
        if (name == null) {
            name = oidcUser.getAttribute("name");
        }
        
        String picture = oidcUser.getPicture();
        if (picture == null) {
            picture = oidcUser.getAttribute("picture");
        }

        if (email != null) {
            customOAuth2UserService.saveOrUpdateUser(email, name, picture);
        }
        
        return oidcUser;
    }
}
