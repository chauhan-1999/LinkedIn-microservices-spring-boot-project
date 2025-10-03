package com.chauhan.linkedInProject.user_service.utils;

import static org.mindrot.jbcrypt.BCrypt.*;

public class BCrypt {

    public static String hash(String s) {
        return hashpw(s, gensalt());
    }//to hash a string with salt

    public static boolean match(String passwordText, String passwordHashed) {//1st convert the TextPasswd into hash and then compare with given hash
        return checkpw(passwordText, passwordHashed);
    }

}
