package com.example.api.common.rest.error.handler


import com.example.api.common.rest.error.exception.BadRequestException
import com.example.api.common.rest.error.exception.EntityNotFoundException
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant
import java.util.*

private typealias ApiResponseEntity = ResponseEntity<ApiErrorResponseBody>

@RestControllerAdvice
class ApiExceptionHandler {
    companion object : KLogging()

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handle(ex: HttpMessageNotReadableException): ApiResponseEntity =
            handleApiException(
                    ex = ex,
                    httpStatus = HttpStatus.BAD_REQUEST,
                    apiErrorType = ApiError.ApiErrorType.BAD_REQUEST
            )

    @ExceptionHandler(BadRequestException::class)
    fun handle(ex: BadRequestException): ApiResponseEntity =
            handleApiException(
                    ex = ex,
                    httpStatus = HttpStatus.BAD_REQUEST,
                    apiErrorType = ApiError.ApiErrorType.BAD_REQUEST
            )

    @ExceptionHandler(EntityNotFoundException::class)
    fun handle(ex: EntityNotFoundException): ApiResponseEntity =
            handleApiException(
                    ex = ex,
                    httpStatus = HttpStatus.NOT_FOUND,
                    apiErrorType = ApiError.ApiErrorType.ENTITY_NOT_FOUND
            )

    private fun handleApiException(
            ex: Exception, httpStatus: HttpStatus, apiErrorType: ApiError.ApiErrorType
    ): ApiResponseEntity {
        val logId = UUID.randomUUID()
        val responseBody: ApiErrorResponseBody = ex.toApiErrorResponseBody(
                logId = logId, httpStatus = httpStatus, type = apiErrorType
        )
        val responseEntity: ApiResponseEntity = ResponseEntity(responseBody, httpStatus)
        logApiException(ex, logId, responseEntity)
        return responseEntity
    }

    private fun logApiException(
            ex: Exception,
            logId: UUID,
            responseEntity: ApiResponseEntity
    ) {
        logger.error {
            "Catch Exception! (logId: $logId)" +
                    " message: ${ex.message}" +
                    " clazz: ${ex::class.qualifiedName}" +
                    " response.statusCode: ${responseEntity.statusCode}" +
                    " response.body: ${responseEntity.body}"
        }
        logger.error("Error Log! (logId: $logId) ", ex)
    }
}


private fun Exception.toApiErrorResponseBody(
        logId: UUID, httpStatus: HttpStatus, type: ApiError.ApiErrorType
): ApiErrorResponseBody =
        ApiErrorResponseBody(
                status = httpStatus.value(),
                message = "$message",
                timestamp = Instant.now(),
                error = "${this::class.qualifiedName}",
                apiError = ApiError(logId = logId, type = type)
        )

data class ApiErrorResponseBody(
        val status: Int,
        val message: String,
        val timestamp: Instant,
        val error: String,
        val apiError: ApiError
)

data class ApiError(
        val type: ApiErrorType,
        val logId: UUID
) {
    enum class ApiErrorType {
        BAD_REQUEST,
        ENTITY_NOT_FOUND
        ;
    }
}