package org.trimou.basis;

import java.time.LocalDateTime;

/**
 * Represents an immutable result of an async template rendering.
 *
 * @author Martin Kouba
 */
public class Result {

    static Result init(Long id, String templateId, String contentType) {
        return new Result(id, templateId, Code.INCOMPLETE, null, null,
                contentType);
    }

    static Result complete(Result result, Code code, String errorMessage,
            String output) {
        return new Result(result.getId(), result.getTemplateId(), code,
                errorMessage, output, result.getContentType());
    }

    private final Long id;

    private final Code code;

    private final String errorMessage;

    private final String output;

    private final String templateId;

    private final String contentType;

    private final LocalDateTime createdAt;

    /**
     *
     * @param id
     * @param code
     * @param errorMessage
     * @param output
     * @param templateId
     * @param contentType
     */
    Result(Long id, String templateId, Code code, String errorMessage,
            String output, String contentType) {
        this.id = id;
        this.code = code;
        this.errorMessage = errorMessage;
        this.output = output;
        this.templateId = templateId;
        this.contentType = contentType;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Code getCode() {
        return code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getOutput() {
        return output;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getContentType() {
        return contentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isComplete() {
        return !Code.INCOMPLETE.equals(code);
    }

    public boolean isSucess() {
        return Code.SUCESS.equals(code);
    }

    public enum Code {
        SUCESS, FAILURE, INCOMPLETE
    }

}
