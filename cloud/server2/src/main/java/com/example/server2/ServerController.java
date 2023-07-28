package com.example.server2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bpp1")
public class ServerController {

    @GetMapping("/{num}")
    String f1(@PathVariable Integer num){
        System.out.println("bpp1");
        try {
            Thread.sleep(num);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return "서비스 광고를 시작합니다.";
    }
}
