package qunar.tc.qconfig.admin.model.rest.upload;

import qunar.tc.qconfig.admin.model.rest.RestApiRequest;

import java.util.List;

/**
 * Created by chenjk on 2018/1/18.
 */
public class UploadRequest extends RestApiRequest {

    private UploadFileEntity config;

    public UploadRequest() {
        super();
    }

    public UploadFileEntity getConfig() {
        return config;
    }

    public void setConfig(UploadFileEntity config) {
        this.config = config;
    }
}
