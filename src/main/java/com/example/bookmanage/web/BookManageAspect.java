package com.example.bookmanage.web;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import lombok.extern.slf4j.Slf4j;

/**
 * 書籍管理システムのAspect
 * 
 * コントローラとサービスのメソッドの実行時間をログ出力する。
 */
@Slf4j
@Aspect
@Component
public class BookManageAspect {

    /**
     * web層(Controller,ExceptionHandler)の実行時間をログ出力する。
     *
     * @param pjp JoinPoint
     * @return JoinPoint実行時の戻り値
     * @throws Throwable JoinPoint実行時の例外
     */
    @Around("execution(* com.example.bookmanage.web.*.*(..))")
    public Object inWebLayer(ProceedingJoinPoint pjp) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object result;
        try {
            result = pjp.proceed();
        } finally {
            stopWatch.stop();
            log.trace("{} : {} ms", pjp.getSignature(), stopWatch.getTotalTimeMillis());
        }
        return result;
    }

    /**
     * service層(Service)の実行時間をログ出力する。
     *
     * @param pjp JoinPoint
     * @return JoinPoint実行時の戻り値
     * @throws Throwable JoinPoint実行時の例外
     */
    @Around("execution(* com.example.bookmanage.service.*.*(..))")
    public Object inServiceLayer(ProceedingJoinPoint pjp) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object result;
        try {
            result = pjp.proceed();
        } finally {
            stopWatch.stop();
            log.trace("{} : {} ms", pjp.getSignature(), stopWatch.getTotalTimeMillis());
        }
        return result;
    }

}
