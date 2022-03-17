package de.laser.custom

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

class CustomPasswordEncoderFactories {

    @SuppressWarnings("deprecation")
    static PasswordEncoder createDelegatingPasswordEncoder() {
        String encodingId = 'bcrypt'
        Map<String, PasswordEncoder> encoders = new HashMap<>()
        encoders.put(encodingId, new BCryptPasswordEncoder())

        DelegatingPasswordEncoder delegatingPasswordEncoder = new DelegatingPasswordEncoder(encodingId, encoders)
        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(new CustomLegacyPasswordEncoder('SHA-256'))

        return delegatingPasswordEncoder
    }

    private CustomPasswordEncoderFactories() {}
}