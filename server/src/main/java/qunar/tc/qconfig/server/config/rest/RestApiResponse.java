package qunar.tc.qconfig.server.config.rest;

import qunar.tc.qconfig.common.util.ApiResponseCode;

/**
 * rest api response
 *
 * Created by chenjk on 2017/8/7.
 */
public class RestApiResponse {

    public static final RestApiResponse BAD_REQUEST_RESPONSE = new RestApiResponse(ApiResponseCode.INVALID_PARAM_CODE, "请求参数不正确，请传入group（应用名称）,dataid（配置名称）,profile（环境）");

    public static final RestApiResponse FILE_NOT_FOUND = new RestApiResponse(ApiResponseCode.FILE_NOT_EXIST_ON_QCONFIG_CODE, "文件在qconfig上不存在");

    private int code;

    private String message;

    public RestApiResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
