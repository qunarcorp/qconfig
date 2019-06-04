package qunar.tc.qconfig.client;

import qunar.tc.qconfig.common.util.ApiResponseCode;

/**
 * 上传文件服务器返回的响应，具体含义请看
 *
 * @see qunar.tc.qconfig.common.util.ApiResponseCode
 *
 * @author zhenyu.nie created on 2015 2015/4/20 14:11
 */
public class UploadResult {

    public static final UploadResult BAD_SERVICE_RESULT = new UploadResult(ApiResponseCode.BAD_SERVICE_CODE, "服务器异常");

    public static final UploadResult PARSE_RESPONSE_ERROR_RESULT = new UploadResult(ApiResponseCode.PARSE_RESPONSE_ERROR_CODE, "解析上传结果错误");

    public static final UploadResult TOKEN_INVALID_RESULT = new UploadResult(ApiResponseCode.INVALID_TOKEN_CODE, "无效的应用中心下发token");

    public static final UploadResult FILE_NOT_EXIST_ON_QCONFIG_CODE = new UploadResult(ApiResponseCode.FILE_NOT_EXIST_ON_QCONFIG_CODE, "服务端不存在该配置");

    public static final UploadResult NOT_IN_MODIFY_STATUS = new UploadResult(ApiResponseCode.NOT_IN_MODIFY_STATUS, "配置文件状态不正确");

    private final int code;

    private final String message;

    public UploadResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "UploadResult{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
