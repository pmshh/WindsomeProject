package com.windsome.config.security;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String error() {
        return "error/error"; // 사용자 정의 오류 페이지의 뷰 이름
    }
}
