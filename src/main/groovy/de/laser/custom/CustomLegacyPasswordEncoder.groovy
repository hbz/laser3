package de.laser.custom

import org.springframework.security.crypto.codec.Hex
import org.springframework.security.crypto.codec.Utf8
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoderUtils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Class to create and check passwords
 */
class CustomLegacyPasswordEncoder implements PasswordEncoder {

    private String algorithm
    private int iterations = 1
    boolean encodeHashAsBase64 = false

    /**
     * Configuration setting for the password encryptor
     * @param algorithm the algorithm to use for password encryption
     */
    CustomLegacyPasswordEncoder(String algorithm) {
        this.algorithm = algorithm
        this.encodeHashAsBase64 = false
    }

    /**
     * Merges the password string with the given salt
     * @param password the password string
     * @param salt the salt which will provide hash
     * @param strict should {} be excluded in hash generation?
     * @return the password if no salt has been submitted, the password with salt otherwise
     */
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

    /**
     * Instantiates the {@link MessageDigest} with the defined algorithm.
     * The algorithm is defined upon instantiating the class itself
     * @return the <tt>MessageDigest</tt> instance
     * @throws IllegalArgumentException
     */
    MessageDigest getMessageDigest() throws IllegalArgumentException {
        try {
            return MessageDigest.getInstance(algorithm)
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm [" + algorithm + "]")
        }
    }

    /**
     * Encodes the given raw password string, encrypted by {@link MessageDigest#digest(byte[])} after prepared by {@link #mergePasswordAndSalt(java.lang.String, java.lang.Object, boolean)}
     * @param rawPassword the string submitted as password
     * @return the encrypted password
     */
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

    /**
     * Password checker: can the given user authenticate successfully?
     * @param rawPassword the password used for authentication, mostly a user login input
     * @param encodedPassword the stored and encrypted password from the database
     * @return true if the two password hashes match, false otherwise
     */
    boolean matches(CharSequence rawPassword, String encodedPassword) {
        String pass1 = "" + encodedPassword
        String pass2 = encode(rawPassword)

        return PasswordEncoderUtils.equals(pass1,pass2)
    }

    /**
     * Returns the {@link #encodeHashAsBase64} flag
     * @return true or false
     */
    boolean getEncodeHashAsBase64() {
        return encodeHashAsBase64
    }
}