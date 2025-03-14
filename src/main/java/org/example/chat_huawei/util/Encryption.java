package org.example.chat_huawei.util;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

public class Encryption {

    //加密
    public static String hashPassword(String passwordPlaintext) {
        // Generate a secure random salt and hash the password
        String hashedPassword = BCrypt.hashpw(passwordPlaintext, BCrypt.gensalt());
        return hashedPassword;
    }
    //验证
    public static boolean checkPassword(String passwordPlaintext, String storedHash) {
        // Check that an unencrypted password matches one that has previously been hashed
        boolean passwordVerified = BCrypt.checkpw(passwordPlaintext, storedHash);

        return passwordVerified;
    }
}