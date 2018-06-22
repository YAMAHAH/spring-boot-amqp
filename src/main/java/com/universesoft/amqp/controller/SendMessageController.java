package com.universesoft.amqp.controller;

import com.universesoft.amqp.sender.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class SendMessageController {

    @Autowired
    private MessageSender messageSender;

    @PostMapping("/sendMessage")
    public void sendMessage(@RequestParam String msg, HttpServletResponse response){
        messageSender.sendMsg(msg);
        response.setStatus(200);
    }
}
