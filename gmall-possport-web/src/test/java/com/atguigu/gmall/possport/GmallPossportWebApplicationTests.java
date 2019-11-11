package com.atguigu.gmall.possport;

import com.atguigu.gmall.possport.util.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPossportWebApplicationTests {


    @Test
    public void contextLoads() {
    }

    @Test
    public void jwtTest(){
        JwtUtil jwtUtil = new JwtUtil();
        String key = "atguigu";
        String salt = "192.168.195.136";
        Map<String , Object> map = new HashMap<>();
        map.put("nackName", "Administrator");
        map.put("id", "2");
        String encode = jwtUtil.encode(key, map, salt);
        System.out.println(encode);

        Map<String, Object> decode = jwtUtil.decode(encode, key, salt);
        System.out.println(decode);

    }

}
