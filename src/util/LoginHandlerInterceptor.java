package util;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import service.UserNotFoundException;

import javax.servlet.http.Cookie;


public class LoginHandlerInterceptor extends HandlerInterceptorAdapter {
    public LoginHandlerInterceptor() {
    }

    @Override
    public boolean preHandle(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
        	for (Cookie cookie : cookies) {
                if (cookie.getName().equals("userId") && cookie.getValue() != null) {
                    return true;
                }
            }
        }
//        response.sendRedirect("./login.html");
//        return false;
        throw new UserNotFoundException("您还未登录");
    }
}
