package dev.padak;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.padak.JoinPointUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Slf4j
public class LogAspect {

    @Autowired
    private ObjectMapper mapper;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controller() {
    }

    @Before("controller()")
    public void logMethod(JoinPoint joinPoint) {


        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Map<String, Object> parameters = JoinPointUtil.getParameters(joinPoint);

        try {
            log.info("request: {}, arguments: {}", signature.getMethod().getName(), mapper.writeValueAsString(parameters));
        } catch (JsonProcessingException e) {
            log.error("Error while converting", e);
        }
    }


    @AfterReturning(pointcut = "controller()", returning = "model")
    public void logMethodAfter(JoinPoint joinPoint, ResponseEntity<?> model) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        try {
            log.info("response: {}, arguments: {}, ", signature.getMethod().getName(), mapper.writeValueAsString(model));
        } catch (JsonProcessingException e) {
            log.error("Error while converting", e);
        }
    }

}

