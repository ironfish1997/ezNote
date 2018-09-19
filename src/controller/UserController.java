package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import entity.User;
import service.UserActiveService;
import service.UserService;
import util.JsonResult;

@Controller("userController")
@RequestMapping("/user")
public class UserController extends AbstractController {

    public UserController() {

    }

    @Autowired
    private UserService userService;
    
    @Autowired
    @Qualifier("userActiveImp")
    private UserActiveService userActiveService;
    
    @RequestMapping("/activeMail.do")
    @ResponseBody
    public Object activeMail(String email,String activecode){
    	JsonResult jr=new JsonResult(userActiveService.activeAccount(email, activecode));
    	if(jr.getState()==0){
    		return new ModelAndView("activeSuccess");
    	}else{
    		return new ModelAndView("activeFailed");
    	}
    }

    @RequestMapping("/login.do")
    @ResponseBody
    public Object login(String email, String password,String expireTime) {
        System.out.println("请求登录");
        User user = userService.Login(email, password, expireTime);
        return new JsonResult(user);
    }

    @RequestMapping("/checkAutoLogin.do")
    @ResponseBody
    public Object checkAutoLogin(String token){
    	return new JsonResult(userService.checkToken(token));
    }

    @RequestMapping("/register.do")
    @ResponseBody
    public Object register(String email, String password, String confirm, String nick) {
        User user = userService.Register(email, password, nick, confirm);
        return new JsonResult(user);
    }

    @RequestMapping("/update.do")
    @ResponseBody
    public Object update(String email, String nickName,String origin, String password, String confirm) {
        return new JsonResult(userService.Update(email, nickName, origin, password, confirm));
    }
    
    @RequestMapping("/existed.do")
    @ResponseBody
    public Object existed(String email) {
        return new JsonResult(userService.Existed(email));
    }
}
