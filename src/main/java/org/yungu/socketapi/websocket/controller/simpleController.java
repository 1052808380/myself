package org.yungu.socketapi.websocket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * Created by Ji on 2017/2/17.
 */
@Controller
public class simpleController {
    @RequestMapping("/index")
    public String simple(){
        return "simple";
    }
}
