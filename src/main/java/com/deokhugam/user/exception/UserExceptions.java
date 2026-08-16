package com.deokhugam.user.exception;

public class UserExceptions {

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class UserEmailDuplicateException extends RuntimeException {
        public UserEmailDuplicateException(String message) {
            super(message);
        }
    }

    public static class UserLoginFailedException extends RuntimeException {
        public UserLoginFailedException(String message) {
            super(message);
        }
    }
}