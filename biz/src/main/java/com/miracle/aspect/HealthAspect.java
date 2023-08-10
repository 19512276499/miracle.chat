package com.miracle.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author lishen
 */
@Aspect
@Component
public class HealthAspect {
         Logger logger = LoggerFactory.getLogger(HealthAspect.class);

      @Pointcut("execution(* com.miracle.chat.*.*(..))")
      public void healthPointCut(){

      }
      @Around(value = "healthPointCut()")
      public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

            long start = System.currentTimeMillis();
            logger.info("方法执行开始,起始时间:"+System.currentTimeMillis());
            Object ret = joinPoint.proceed();
            logger.info("方法执行结束,耗时:"+(System.currentTimeMillis()-start));
            return ret;
      }

}
