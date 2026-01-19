package com.barofarm.exception;

import com.barofarm.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

public abstract class BaseExceptionHandler {

    private static final org.slf4j.Logger log =
        org.slf4j.LoggerFactory.getLogger(BaseExceptionHandler.class);

    // =========================
    // 400: Validation / Binding (사용자 입력 검증/바인딩)
    // =========================

    /**
     * 400: @ModelAttribute / @RequestParam 바인딩 중 검증 실패 시 발생
     * 주로 GET 요청이나 form-data 요청에서 값 누락, 타입 불일치, @Valid 검증 실패일 때 발생
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ResponseDto<Void>> handleBindException(BindException e) {
        String message = e.getBindingResult()
            .getAllErrors()
            .get(0)
            .getDefaultMessage();

        log.warn("BindException: {}", message);

        ResponseDto<Void> body = ResponseDto.error(HttpStatus.BAD_REQUEST, message);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(body);
    }

    /**
     * 400: @RequestBody + @Valid 조합에서 DTO 필드 검증 실패 시 발생
     * 예) NotNull, NotBlank, Size, Pattern 등 Bean Validation 어노테이션 위반
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<Void>> handleValidationException(
        MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .orElse("요청 값이 올바르지 않습니다.");

        log.warn("MethodArgumentNotValidException: {}", message);

        return ResponseEntity
            .badRequest()
            .body(ResponseDto.error(
                HttpStatus.BAD_REQUEST,
                message
            ));
    }

    /**
     * 400: 잘못된 메서드 인자 전달 시 발생
     * 주로 개발자가 명시적으로 파라미터 검증 후 throw 하는 경우
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDto<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());

        ResponseDto<Void> body = ResponseDto.error(HttpStatus.BAD_REQUEST, e.getMessage());

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(body);
    }

    // =========================
    // 400: Request Format / Parsing (요청 형식/파싱)
    // =========================

    /**
     * 400: 요청 바디(JSON)가 깨졌거나, 타입이 안 맞아서 역직렬화(파싱) 실패할 때 발생
     * 예) JSON 문법 오류, 숫자 필드에 문자열, enum에 없는 값, LocalDate 포맷 불일치 등
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseDto<Void>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException e
    ) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(ResponseDto.error(HttpStatus.BAD_REQUEST, "요청 본문(JSON) 형식이 올바르지 않습니다."));
    }

    /**
     * 400: 필수 쿼리 파라미터가 누락됐을 때 발생
     * 예) /orders?userId=... 에서 userId를 안 보냄
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResponseDto<Void>> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException e
    ) {
        log.warn("MissingServletRequestParameterException: name={} type={}", e.getParameterName(), e.getParameterType());
        String message = "필수 파라미터가 누락되었습니다: " + e.getParameterName();
        return ResponseEntity.badRequest()
            .body(ResponseDto.error(HttpStatus.BAD_REQUEST, message));
    }

    /**
     * 400: 파라미터/경로변수 타입 변환이 실패했을 때 발생
     * 예) UUID 자리에 '123' 넣음, int 자리에 'abc' 넣음
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseDto<Void>> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException e
    ) {
        log.warn("MethodArgumentTypeMismatchException: name={} value={} requiredType={}",
            e.getName(), e.getValue(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown"
        );

        String message = "요청 값 타입이 올바르지 않습니다: " + e.getName();
        return ResponseEntity.badRequest()
            .body(ResponseDto.error(HttpStatus.BAD_REQUEST, message));
    }

    // =========================
    // 404 / 405 / 415: Routing & Protocol (라우팅/프로토콜)
    // =========================

    /**
     * 404: 존재하지 않는 URI로 요청했을 때 발생
     * 예) 잘못된 경로, 삭제된 API 엔드포인트 호출
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResponseDto<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("NoResourceFoundException: {}", e.getResourcePath());

        ResponseDto<Void> body = ResponseDto.error(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다: " + e.getResourcePath());

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(body);
    }

    /**
     * 405: 지원하지 않는 HTTP 메서드로 호출했을 때 발생
     * 예) POST만 가능한데 GET으로 호출함
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseDto<Void>> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException e
    ) {
        log.warn("HttpRequestMethodNotSupportedException: method={} supported={}",
            e.getMethod(), e.getSupportedHttpMethods()
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(ResponseDto.error(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."));
    }

    /**
     * 415: 지원하지 않는 Content-Type으로 요청을 보냈을 때 발생
     * 예) 서버는 application/json만 받는데 text/plain 또는 form-data로 보냄
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ResponseDto<Void>> handleHttpMediaTypeNotSupportedException(
        HttpMediaTypeNotSupportedException e
    ) {
        log.warn("HttpMediaTypeNotSupportedException: contentType={} supported={}",
            e.getContentType(), e.getSupportedMediaTypes()
        );

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(ResponseDto.error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type 입니다."));
    }

    // =========================
    // Business Exception (의도된 비즈니스 예외)
    // =========================

    /**
     * 비즈니스 예외 처리용 커스텀 예외
     * 서비스/도메인 로직에서 의도적으로 발생시키는 예외로,
     * ErrorCode에 정의된 HttpStatus(400, 403, 404 등)를 그대로 응답
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<Void>> handleCustomException(CustomException e) {
        HttpStatus status = e.getErrorCode().getStatus();

        log.warn("CustomException: status={} message={}",
            status, e.getErrorCode().getMessage());

        return ResponseEntity.status(status)
            .body(ResponseDto.error(status, e.getErrorCode().getMessage()));
    }

    // =========================
    // 500: Unhandled (예상치 못한 서버 오류)
    // =========================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Void>> handleException(Exception e) {
        // 로그 출력을 위해 스택 트레이스 출력
        log.error("Unhandled exception occurred", e);

        ResponseDto<Void> body = ResponseDto.error(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 내부 오류가 발생했습니다");

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(body);
    }
}
