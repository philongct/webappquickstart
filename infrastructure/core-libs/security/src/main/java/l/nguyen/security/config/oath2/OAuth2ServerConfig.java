package l.nguyen.security.config.oath2;

import l.nguyen.security.config.basicweb.AbstractSecurityConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableAuthorizationServer
public abstract class OAuth2ServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtTokenEnhancer());
    }

    /**
     * Encode Oauth2 tokens in JWT using private key
     *
     * @return
     */
    @Bean
    protected JwtAccessTokenConverter jwtTokenEnhancer() {
        // Generate key with:
        // keytool -genkey -keyalg RSA -alias lnguyen -keystore keystore.jks -storepass secret -validity 36500
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(
                new ClassPathResource("fwk/keystore.jks"), "secret".toCharArray());

        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair("lnguyen"));

        return converter;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
            // This is demo only. TODO: each client should have a separate config
            .withClient("anyclient")
                .secret("clientsecret")
                // Read more about grant types here: https://alexbilbie.com/guide-to-oauth-2-grants/
                .authorizedGrantTypes("authorization_code", "client_credentials", "refresh_token", "password")
                // Since this is just an example so set expiry period to 1 hour
                .accessTokenValiditySeconds(3600)
                .refreshTokenValiditySeconds(3600)
                // Bypass approve form (/uaa/oauth/confirm_access) after login
                // http://localhost:9999/uaa/oauth/authorize?response_type=code&client_id=anyclient&redirect_uri=http://notes.coding.me)
                .autoApprove(true)
                // default redirect uris if "redirect_uri" param not found (must match with configured value if given)
//                .redirectUris("clienturi")
                // access scope (read, write, ...)
                .scopes("openid");
//        .and()
//            .withClient()
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager).accessTokenConverter(jwtTokenEnhancer());
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer
            .tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()")
            // Allow curl -X POST "http://localhost:9999/uaa/oauth/token" -d "password=<password>&username=<username>&grant_type=password&scope=openid&client_secret=clientsecret&client_id=anyclient"
            .allowFormAuthenticationForClients();
    }
}
