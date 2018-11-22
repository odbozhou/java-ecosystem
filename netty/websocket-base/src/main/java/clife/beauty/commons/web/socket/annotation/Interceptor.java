package clife.beauty.commons.web.socket.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Interceptor {
    int order() default 0;
}
