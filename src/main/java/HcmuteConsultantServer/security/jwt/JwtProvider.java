package HcmuteConsultantServer.security.jwt;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import HcmuteConsultantServer.model.entity.AccountEntity;
import HcmuteConsultantServer.model.entity.UserInformationEntity;
import HcmuteConsultantServer.model.exception.Exceptions;
import HcmuteConsultantServer.model.exception.JWT401Exception;
import HcmuteConsultantServer.repository.admin.AccountRepository;
import HcmuteConsultantServer.repository.admin.UserRepository;
import HcmuteConsultantServer.security.authentication.UserPrincipal;
import HcmuteConsultantServer.security.authentication.UserPrinciple;
import HcmuteConsultantServer.security.oauth2.AppProperties;
import HcmuteConsultantServer.service.interfaces.common.IUserService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);
    private static final long jwtExpirationMs = 2592000000L;
    private static final long refreshTokenExpirationMs = 2592000000L;
    private final AccountRepository accountRepository;
    @Autowired
    public JwtProvider(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    @Value("${jwt.secret}")
    private String jwtSecret;

    public String createToken(UserInformationEntity userModel) {
        if (userModel == null || userModel.getAccount() == null) {
            throw new IllegalArgumentException("User model or account model is null (trong JwtProvider)");
        }

        return Jwts.builder()
                .setSubject(userModel.getAccount().getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .claim("authorities", userModel.getAccount().getRole().getName().replace("ROLE_", ""))
                .compact();
    }

    public String createToken(Authentication authentication) {
        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();

//        String email = ((UserPrinciple) authentication.getPrincipal()).getEmail();
//
//        AccountEntity account = accountRepository.findAccountByEmail(email);
//
//        Map<String, Object> userClaims = new HashMap<>();
//        userClaims.put("userId", account.getId());
//        userClaims.put("email", account.getEmail());
//        userClaims.put("username", account.getUsername());
//        userClaims.put("schoolName", account.getUserInformation().getSchoolName());
//        userClaims.put("firstName", account.getUserInformation().getFirstName());
//        userClaims.put("lastName", account.getUserInformation().getLastName());
//        userClaims.put("phone", account.getPhone());
//        userClaims.put("avatarUrl", account.getUserInformation().getAvatarUrl());
//        userClaims.put("gender", account.getUserInformation().getGender());
//        userClaims.put("address", account.getUserInformation().getAddress());
//        userClaims.put("account", account.getUserInformation().getAccount());

        return Jwts.builder()
                .setSubject(userPrinciple.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .claim("authorities", "USER")
//                .claim("user", userClaims)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            throw new JWT401Exception("Chữ ký JWT không hợp lệ", "INVALID_SIGNATURE", 401);
        } catch (UnsupportedJwtException e) {
            throw new JWT401Exception("JWT không được hỗ trợ", "UNSUPPORTED_JWT", 401);
        } catch (ExpiredJwtException e) {
            throw new JWT401Exception("JWT hết hạn. Vui lòng đăng nhập lại", "EXPIRE_TOKEN", 401);
        } catch (IllegalArgumentException e) {
            throw new JWT401Exception("Chuỗi claims JWT rỗng", "EMPTY_CLAIMS", 401);
        }
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("Could not extract claims from token: {}", e.getMessage());
            return null;
        }
    }

}
