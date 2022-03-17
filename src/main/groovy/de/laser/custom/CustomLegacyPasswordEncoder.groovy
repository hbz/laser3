package de.laser.custom

import org.springframework.security.crypto.codec.Hex
import org.springframework.security.crypto.codec.Utf8
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoderUtils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class CustomLegacyPasswordEncoder implements PasswordEncoder {

    private String algorithm
    private int iterations = 1
    boolean encodeHashAsBase64 = false

    CustomLegacyPasswordEncoder(String algorithm) {
        this.algorithm = algorithm
        this.encodeHashAsBase64 = false
    }

    String mergePasswordAndSalt(String password, Object salt, boolean strict) {
        if (password == null) {
            password = ""
        }

        if (strict && (salt != null)) {
            if ((salt.toString().lastIndexOf("{") != -1) || (salt.toString().lastIndexOf("}") != -1)) {
                throw new IllegalArgumentException("Cannot use { or } in salt.toString()")
            }
        }

        if ((salt == null) || "".equals(salt)) {
            return password
        }
        else {
            return password + "{" + salt.toString() + "}"
        }
    }

    MessageDigest getMessageDigest() throws IllegalArgumentException {
        try {
            return MessageDigest.getInstance(algorithm)
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm [" + algorithm + "]")
        }
    }

    String encode(CharSequence rawPassword) {
        rawPassword = rawPassword.toString()
        Object salt = null
        String saltedPass = mergePasswordAndSalt(rawPassword, salt, false)

        MessageDigest messageDigest = getMessageDigest()

        byte[] digest = messageDigest.digest(Utf8.encode(saltedPass))

        // "stretch" the encoded value if configured to do so
        for (int i = 1; i < iterations; i++) {
            digest = messageDigest.digest(digest)
        }

        if (getEncodeHashAsBase64()) {
            return Utf8.decode(Base64.encode(digest))
        }
        else {
            return new String(Hex.encode(digest))
        }
    }

    boolean matches(CharSequence rawPassword, String encodedPassword) {
        String pass1 = "" + encodedPassword
        String pass2 = encode(rawPassword)

        return PasswordEncoderUtils.equals(pass1,pass2)
    }

    boolean getEncodeHashAsBase64() {
        return encodeHashAsBase64
    }
}