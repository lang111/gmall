package com.atguigu.gmall.config;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequire {
   boolean autoRedirect() default true;
}
