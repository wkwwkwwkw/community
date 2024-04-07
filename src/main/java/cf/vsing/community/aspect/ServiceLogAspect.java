package cf.vsing.community.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Pointcut("execution (* cf.vsing.community.service.*.*(..))")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void before(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            String dateTime = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss").format(new Date());
            String className = joinPoint.getSignature().getDeclaringTypeName() + joinPoint.getSignature().getName();
            log.info(String.format("%s %s[Special] %s", dateTime, "0.0.0.0", className));
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String requestIP = request.getRemoteHost();
        String dateTime = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss").format(new Date());
        String className = joinPoint.getSignature().getDeclaringTypeName() + joinPoint.getSignature().getName();
        log.info(String.format("%s %s %s", dateTime, requestIP, className));
    }
}
