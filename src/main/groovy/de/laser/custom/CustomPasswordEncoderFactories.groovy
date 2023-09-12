package de.laser.custom

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * A fallback class for password creation
 */
class CustomPasswordEncoderFactories {

    /**
     * Fallback method to create a password encryption engine
     * @return a {@link PasswordEncoder} instance
     */
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