package com.costuras.carrito.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;
@Component
public class JwtUtil {

    @Value("${jwt.secret}") private String secretKey;
    private SecretKey key;

    @PostConstruct public void init() { key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)); }

    public Claims getAllClaims(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
    
    public <T> T getClaim(String token, Function<Claims,T> r) 

    { return r.apply(getAllClaims(token)); }

    public boolean isTokenExpired(String token) 

    { return getClaim(token, Claims::getExpiration).before(new Date()); }

    public UsuarioPrincipal getPrincipalFromToken(String token) {

        Claims c = getAllClaims(token);

        Integer id = null; Object idC = c.get("id");

        if (idC instanceof Integer) id=(Integer)idC; else if (idC instanceof Long) id=((Long)idC).intValue();

        return UsuarioPrincipal.builder().id(id).username(c.getSubject()).role(c.get("role",String.class)).build();
    }
}
