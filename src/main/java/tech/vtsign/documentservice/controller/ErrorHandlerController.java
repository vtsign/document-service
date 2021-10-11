package tech.vtsign.documentservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import tech.vtsign.documentservice.exception.ExceptionResponse;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Hidden
@RestController
@RequiredArgsConstructor
public class ErrorHandlerController implements ErrorController {
    private static final String PATH = "error";
    private final ErrorAttributes errorAttributes;

    @RequestMapping(PATH)
    @ResponseBody
    public ExceptionResponse error(WebRequest request, HttpServletResponse response) {
        return new ExceptionResponse(response.getStatus(), getErrorAttributes(request));
    }

    private Map<String, Object> getErrorAttributes(WebRequest request) {

        return this.errorAttributes.getErrorAttributes(request, ErrorAttributeOptions.defaults());
    }

}
