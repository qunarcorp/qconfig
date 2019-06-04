package qunar.tc.qconfig.admin.cloud.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.common.bean.JsonV2;

/**
 * Created by pingyang.yang on 2018/11/22
 */
@ControllerAdvice
public class ControllerExceptionHandler {

    Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public JsonV2 exceptionHandle(RuntimeException e) {
        logger.error(e.getMessage(), e);
        return new JsonV2<String>(-1, e.getMessage(),null);
    }
}
