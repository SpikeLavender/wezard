package com.natsumes.wezard.exception;

import com.natsumes.wezard.entity.Response;
import com.natsumes.wezard.enums.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;


@Slf4j
@ControllerAdvice
public class RuntimeExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    //@ResponseStatus(HttpStatus.FORBIDDEN)
    public Response handle(RuntimeException e) {
        return Response.error(ResponseEnum.SYSTEM_ERROR, e.getMessage());
    }

    @ExceptionHandler(UserLoginException.class)
    @ResponseBody
    public Response userLoginExceptionHandle() {
        return Response.error(ResponseEnum.NEED_LOGIN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Response notValidExceptionHandle(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        log.error("注册提交的参数有误, {} {}", Objects.requireNonNull(bindingResult.getFieldError()).getField(),
                bindingResult.getFieldError().getDefaultMessage());
        return Response.error(ResponseEnum.PARAM_ERROR, bindingResult);
    }
}
