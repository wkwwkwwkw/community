package cf.vsing.community.util;

import cf.vsing.community.entity.Result;
import org.springframework.security.core.AuthenticationException;

public class MyException {
    public static class LoginException extends AuthenticationException {
        private Result result;
        public LoginException(Result result){
            super("");
            this.result=result;
        }

        public Result getResult() {
            return result;
        }
    }
}
