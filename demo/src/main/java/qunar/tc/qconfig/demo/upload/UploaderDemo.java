package qunar.tc.qconfig.demo.upload;

import org.springframework.stereotype.Component;
import qunar.tc.qconfig.client.UploadResult;
import qunar.tc.qconfig.client.Uploader;
import qunar.tc.qconfig.client.impl.ConfigUploader;
import qunar.tc.qconfig.client.impl.Snapshot;
import qunar.tc.qconfig.client.impl.VersionProfile;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Component
public class UploaderDemo {

    public static void main(String[] args) {
        new UploaderDemo().uploadRecomand();
    }

    public void uploadSimple() {
        Uploader uploader = ConfigUploader.getInstance();
        try {
            UploadResult result = uploader.upload("testDemo.properties", "aaa=bbb");
            System.out.println(result.getCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uploadRecomand() {
        Uploader uploader = ConfigUploader.getInstance();
        try {
            Optional<Snapshot<String>> current = uploader.getCurrent("testDemo.properties");
            UploadResult result;
            if (current.isPresent()) {
                result = uploader.uploadAtVersion(current.get().getVersion(), "testDemo.properties", "aaa=bbb");
            } else {
                result = uploader.uploadAtVersion(VersionProfile.ABSENT, "testDemo.properties", "aaa=bbb");
            }
            System.out.println(result.getCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
