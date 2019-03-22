package com.atguigu.gmall.passport.controller;
import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.CookieUtil;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.JwtUtil;
import io.jsonwebtoken.SignatureException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
@Controller
public class PassPortController {
    @Reference
    private UserService userService;
    @Reference
    private CartService cartService;
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request,String token,String currentIp){
        String success = "false";
        try {
            Map map = JwtUtil.decode("gmall1015atguigu", token, currentIp);
            if(map != null){
                success = "true";
            }
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return success;
    }
    @RequestMapping("login")
    @ResponseBody
    public String login(HttpServletRequest request, HttpServletResponse response, UserInfo userInfoParam, Model model){
        String token = "";
        UserInfo userInfo = userService.login(userInfoParam);
        if (userInfo != null){
            //如果用户非空生成token并返回
            Map<String,String> map = new HashMap<>();
            String userId = userInfo.getId();
            String nickName = userInfo.getNickName();
            String ip = "";
            String remoteAddr = request.getRemoteAddr();
            ip = request.getHeader("X－FORWARDED－FOR");
            if (StringUtils.isBlank(ip)){
                ip = remoteAddr;
            }
            map.put("userId",userId);
            map.put("nickName",nickName);
            token = JwtUtil.encode("gmall1015atguigu", map, ip);
            //合并购物车
            String cookieCart = CookieUtil.getCookieValue(request,"cartListCookie", true);
            cartService.uniteCart(cookieCart,userId);
            //删除缓存
            CookieUtil.deleteCookie(request,response,"cartListCookie");

        }else{
            token = "fail";
        }
        return token;
    }

    @RequestMapping("index")
    public String login(String ReturnUrl, Model model){
        model.addAttribute("ReturnUrl",ReturnUrl);
        return "index";
    }
}
