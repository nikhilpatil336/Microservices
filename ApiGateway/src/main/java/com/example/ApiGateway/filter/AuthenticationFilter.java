package com.example.ApiGateway.filter;

import com.example.ApiGateway.JWTUtil.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RestTemplate template;
    @Autowired
    private RouteValidator validator;
    @Autowired
    private JwtService jwtService;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (((exchange, chain) -> {
            if(validator.isSecured.test(exchange.getRequest())){
                //header contains token or not
                if(!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                    throw new RuntimeException("missing authentication header.");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).stream().findAny().get();
                if(authHeader!=null && authHeader.startsWith("Bearer ")){
                    authHeader = authHeader.substring(7);
                }
                try
                {
//                    //REST call to AUTH service
//                    template.getForObject("http://GATEWAYSECURITY-SERVICE/validate?token"+authHeader, String.class);
                    jwtService.validateToken(authHeader);
                }
                catch(Exception e)
                {
                    System.out.println("invalid access to application");
                    throw new RuntimeException("un authorized access to application");
                }
            }
            return chain.filter(exchange);
        }));
    }

    public static class Config
    {

    }

}
