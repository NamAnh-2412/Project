package vn.quanlybanhang.service;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordService {

    public boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }

        String normalizedHash = storedHash;
        if (storedHash.startsWith("$2y$")) {
            normalizedHash = "$2a$" + storedHash.substring(4);
        }

        try {
            return BCrypt.checkpw(rawPassword, normalizedHash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }
}
