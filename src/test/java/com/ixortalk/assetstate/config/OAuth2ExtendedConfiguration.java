/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-present IxorTalk CVBA
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ixortalk.assetstate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;

import static com.ixortalk.assetstate.config.OAuth2ExtendedConfiguration.Role.ORGANIZATION_X_ADMIN;
import static com.ixortalk.assetstate.config.OAuth2ExtendedConfiguration.Role.ORGANIZATION_Y_ADMIN;
import static com.ixortalk.test.oauth2.OAuth2TestTokens.getAccessToken;

@Configuration
public class OAuth2ExtendedConfiguration {

    public static final String CLIENT_IN_ORGANIZATION_X_ADMIN_ROLE_ID = "clientInOrganizationXAdminRoleId";
    private static final String CLIENT_IN_ORGANIZATION_X_ADMIN_ROLE_SECRET = "clientInOrganizationXAdminRoleSecret";
    public static final String CLIENT_IN_ORGANIZATION_Y_ADMIN_ROLE_ID = "clientInOrganizationYAdminRoleId";
    private static final String CLIENT_IN_ORGANIZATION_Y_ADMIN_ROLE_SECRET = "clientInOrganizationYAdminRoleSecret";

    public static OAuth2AccessToken clientInOrganizationXAdminRoleToken() {
        return getAccessToken(CLIENT_IN_ORGANIZATION_X_ADMIN_ROLE_ID, CLIENT_IN_ORGANIZATION_X_ADMIN_ROLE_SECRET);
    }

    public static OAuth2AccessToken clientInOrganizationYAdminRoleToken() {
        return getAccessToken(CLIENT_IN_ORGANIZATION_Y_ADMIN_ROLE_ID, CLIENT_IN_ORGANIZATION_Y_ADMIN_ROLE_SECRET);
    }

    public enum Role {
        ORGANIZATION_X_ADMIN,
        ORGANIZATION_Y_ADMIN;

        public String roleName() {
            return "ROLE_" + name();
        }
    }

    @Configuration
    protected static class OAuth2Config extends AuthorizationServerConfigurerAdapter {

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients
                    .and()
                        .withClient(CLIENT_IN_ORGANIZATION_X_ADMIN_ROLE_ID)
                        .secret(CLIENT_IN_ORGANIZATION_X_ADMIN_ROLE_SECRET)
                        .authorizedGrantTypes("client_credentials")
                        .scopes("openid")
                        .authorities(ORGANIZATION_X_ADMIN.roleName())
                    .and()
                        .withClient(CLIENT_IN_ORGANIZATION_Y_ADMIN_ROLE_ID)
                        .secret(CLIENT_IN_ORGANIZATION_Y_ADMIN_ROLE_SECRET)
                        .authorizedGrantTypes("client_credentials")
                        .scopes("openid")
                        .authorities(ORGANIZATION_Y_ADMIN.roleName());
        }
    }
}
