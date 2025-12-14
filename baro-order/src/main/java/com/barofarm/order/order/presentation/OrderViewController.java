package com.barofarm.order.order.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class OrderViewController {

    @GetMapping("/")
    public String home() {
        return "toss-payment";
    }
}
