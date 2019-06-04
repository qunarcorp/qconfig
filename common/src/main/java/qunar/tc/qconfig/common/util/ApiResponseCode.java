package qunar.tc.qconfig.common.util;

/**
 * 开放给client调用API响应码
 *
 * @author zhenyu.nie created on 2015 2015/4/20 18:39
 */
public class ApiResponseCode {

    // 服务器异常
    public static final int BAD_SERVICE_CODE = -1;

    // 请求成功
    public static final int OK_CODE = 0;

    // 无效的token
    public static final int INVALID_TOKEN_CODE = 1;

    // 无效的参数
    public static final int INVALID_PARAM_CODE = 2;

    // 解析返回结果错误
    public static final int PARSE_RESPONSE_ERROR_CODE = 3;

    // 文件已被修改
    public static final int HAS_BEEN_MODIFIED_CODE = 4;

    // 无效的机器环境
    public static final int INVALID_MACHINE_ENVIRONMENT_CODE = 5;

    // 机器ip校验失败
    public static final int ILLEGAL_IP_CODE = 6;

    // 上传超过文件大小限制
    public static final int OVER_UPLOAD_SIZE_CODE = 7;

    // 没有修改文件的权限
    public static final int NO_MODIFY_FILE_PERMISSION_CODE = 8;

    // 文件在qconfig上不存在
    public static final int FILE_NOT_EXIST_ON_QCONFIG_CODE = 9;

    // 文件处于不能上传的状态
    public static final int NOT_IN_MODIFY_STATUS = 10;

    // 文件处于取消引用的状态
    public static final int CANCEL_REF_STATUS = 11;

    // buildGroup不存在
    public static final int BUILD_GROUP_NOT_EXIST_CODE = 12;

    //公共文件已经存在
    public static final int INHERIT_FILE_EXIST = 13;

    //rest 类型文件不存在
    public static final int REST_FILE_NOT_EXIST = 14;

    //插件不存在
    public static final int PLUGIN_NOT_EXIST = 15;

    //没有读取权限
    public static final int NO_READ_PERMISSION = 16;

    //buildGroup已经存在
    public static final int BUILD_GROUP_NOT_EXISTED_CODE = 17;

    //没有创建buildGroup权限
    public static final int BUILD_GROUP_CANNOT_CREATE_PERMISSION_CODE = 18;

}
