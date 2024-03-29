package com.image.backend.enums;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
@NoArgsConstructor
@Getter
public class EExpiration {
    @Value("#{T(java.lang.Long).parseLong('${soilhm.jwt.expiration.mobile.access_token}')}")
    private Long jwtExpirationMobile;

    @Value("#{T(java.lang.Long).parseLong('${soilhm.jwt.expiration.web.access_token}')}")
    private Long jwtExpirationWeb;
}
