package com.example.post.common.exception;


import com.example.post.common.dto.CommonErrorDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

//컨트롤러 어노테이션이붙어있는 모든 클래스의 예외를 아래 클래스에서 인터셉팅(가로채기)
@RestControllerAdvice
public class CommonExceptionHandler {

    //    1)비즈니스 예외
//    email중복, 잘못된 입력값 등 "요청이 정책상 허용되지 않는 경우"
    @ExceptionHandler(IllegalAccessException.class)
//    사용자가 주는게 아니니 @GetMapper필요없다
    public ResponseEntity<?> illegal(IllegalAccessException e) { //e에 에러메시지 들어옴
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(400)
                .error_message(e.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);

    }

    //    2)검증예외
//    프레임워크가 발생시키는 예외 등(어노테이션 걸어놓으거)
    @ExceptionHandler(MethodArgumentNotValidException.class)
//    사용자가 주는게 아니니 @GetMapper필요없다
    public ResponseEntity<?> methodArgument(MethodArgumentNotValidException e) { //e에 에러메시지 들어옴
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(400)
                .error_message(e.getFieldError().getDefaultMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);

    }

    //      3)리소스 없음
//    ex.조회/삭제 시 DB에 아이디 없을 때
    @ExceptionHandler(NoSuchElementException.class)
//    사용자가 주는게 아니니 @GetMapper필요없다
    public ResponseEntity<?> noSuchException(NoSuchElementException e) { //e에 에러메시지 들어옴
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(404)
                .error_message(e.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);

    }



    @ExceptionHandler(EntityNotFoundException.class)
//    사용자가 주는게 아니니 @GetMapper필요없다
    public ResponseEntity<?> entityNotFoundException(EntityNotFoundException e) { //e에 에러메시지 들어옴
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(404)
                .error_message(e.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);

    }


      //    4)그 외 모든 예외
//    예상하지 못한 예외에 대한 안전망
    @ExceptionHandler(Exception.class)
//    사용자가 주는게 아니니 @GetMapper필요없다
    public ResponseEntity<?> exception(Exception e) { //e에 에러메시지 들어옴
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(500)
                .error_message(e.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);

    }

}
