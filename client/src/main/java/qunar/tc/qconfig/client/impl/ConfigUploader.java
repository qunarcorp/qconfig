package qunar.tc.qconfig.client.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.UploadResult;
import qunar.tc.qconfig.client.Uploader;
import qunar.tc.qconfig.common.application.ServerManager;
import qunar.tc.qconfig.common.bean.StatusType;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

/**
 * @author zhenyu.nie created on 2015 2015/4/20 14:16
 */
public class ConfigUploader implements Uploader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigUploader.class);

    private static ConfigUploader INSTANCE = new ConfigUploader();

    public static Uploader getInstance() {
        return INSTANCE;
    }

    private static final String groupName = ((ConfigEngine) ConfigEngine.getInstance()).getGroupName();

    public static final String DEFAULT_OPERATOR = ServerManager.getInstance().getAppServerConfig().getName();

    private static final String EMPTY_DESCRIPTION = "";

    private final QConfigAdminClient client;

    private final QConfigServerClient serverClient;

    private ConfigUploader() {
        client = QConfigAdminClient.getInstance();
        serverClient = QConfigServerClientFactory.create();
    }

    @Override
    public UploadResult upload(String dataId, String data) throws Exception {
        Preconditions.checkNotNull(dataId);
        Preconditions.checkNotNull(data);
        Meta meta = new Meta(groupName, dataId);
        VersionProfile currentVersion = FileStore.readVersion(FileStore.getVersionFile(meta));
        return uploadAtVersion(currentVersion, dataId, data, false, DEFAULT_OPERATOR, true, EMPTY_DESCRIPTION);
    }

    @Override
    public UploadResult uploadAtVersion(VersionProfile version, String dataId, String data) throws Exception {
        return uploadAtVersion(version, dataId, data, false, DEFAULT_OPERATOR, true, EMPTY_DESCRIPTION);
    }

    @Override
    public UploadResult uploadAtVersion(VersionProfile version, String dataId, String data, boolean isPublic) throws Exception {
        Preconditions.checkNotNull(version);
        Preconditions.checkNotNull(dataId);
        Preconditions.checkNotNull(data);
        Meta meta = new Meta(groupName, dataId);
        return uploadCas(version, meta, data, isPublic, DEFAULT_OPERATOR, true, EMPTY_DESCRIPTION);
    }

    @Override
    public UploadResult apply(VersionProfile versionProfile, String dataId, String data, String operator, String description) throws Exception {
        return uploadAtVersion(versionProfile, dataId, data, false, operator, false, description);
    }

    @Override
    public UploadResult approve(VersionProfile versionProfile, String dataId, String operator) throws Exception {
        return doChangeStatus(versionProfile, dataId, operator, StatusType.PENDING, StatusType.PASSED, false);
    }

    @Override
    public UploadResult reject(VersionProfile versionProfile, String dataId, String operator) throws Exception {
        return doChangeStatus(versionProfile, dataId, operator, StatusType.PENDING, StatusType.REJECT, false);
    }

    @Override
    public UploadResult cancel(VersionProfile versionProfile, String dataId, String operator) throws Exception {
        return doChangeStatus(versionProfile, dataId, operator, StatusType.PASSED, StatusType.CANCEL, false);
    }

    @Override
    public UploadResult publish(VersionProfile versionProfile, String dataId, String operator, boolean isPublic) throws Exception {
        return doChangeStatus(versionProfile, dataId, operator, StatusType.PASSED, StatusType.PUBLISH, isPublic);
    }

    private UploadResult uploadAtVersion(VersionProfile version, String dataId, String data, boolean isPublic, String operator, boolean directPublish, String description) throws Exception {
        Preconditions.checkNotNull(version);
        Preconditions.checkNotNull(dataId);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(operator);
        Meta meta = new Meta(groupName, dataId);
        return uploadCas(version, meta, data, isPublic, operator, directPublish, description);
    }

    private UploadResult changeStatus(String dataId, long version, String profile, String operator, StatusType statusType, boolean isPublic) throws Exception {
        logger.info("request " + statusType.text() + " qconfig file, file=[{}], version = {}, fileprofile = {}", new Meta(groupName, dataId), version, profile);
        UploadResult result = client.changeStatus(groupName, dataId, profile, version, operator, statusType, isPublic).get();
        logger.info("complete request " + statusType.text() + " qconfig file, file=[{}], version = {}, fileprofile = {}", new Meta(groupName, dataId), version, profile);
        return result;
    }

    private UploadResult doChangeStatus(VersionProfile versionProfile, String dataId, String operator, StatusType expectedStatus, StatusType targetStatus, boolean isPublic) throws Exception {
        Optional<Snapshot<String>> currentSnapshot = loadCandidateSnapShotData(dataId);
        if (currentSnapshot.isPresent()) {
            Snapshot<String> snapshot = currentSnapshot.get();
            if (snapshot.getStatusType() == expectedStatus) {
                return changeStatus(dataId, versionProfile.getVersion(), versionProfile.getProfile(), operator, targetStatus, isPublic);
            } else {
                return UploadResult.NOT_IN_MODIFY_STATUS;
            }
        } else {
            return UploadResult.FILE_NOT_EXIST_ON_QCONFIG_CODE;
        }
    }

    @Override
    public Optional<Snapshot<String>> loadCandidateSnapShotData(String dataId) throws Exception {
        logger.info("request latest snapshot qconfig file, file=[{}]", new Meta(groupName, dataId));
        ListenableFuture<Snapshot<String>> snapshot = client.loadCandidateSnapShotData(groupName, dataId);
        Snapshot<String> result = snapshot.get();
        logger.info("complete request latest snapshot qconfig file, file=[{}]", new Meta(groupName, dataId));
        return result == null ? Optional.<Snapshot<String>>absent() : Optional.of(result);
    }

    private UploadResult uploadCas(VersionProfile version, Meta meta, String data, boolean isPublic, String operator, boolean directPublish, String description) throws Exception {
        logger.info("request upload qconfig file, file=[{}], {}", meta, version);
        UploadResult result = client.upload(meta, version, data, isPublic, operator, directPublish, description).get();
        logger.info("upload qconfig file complete, file=[{}], {}, {}", meta, version, result);
        return result;
    }

    @Override
    public Optional<Snapshot<String>> getCurrent(String dataId) throws Exception {
        Meta meta = new Meta(groupName, dataId);
        logger.info("request get current qconfig file, group=[{}], dataId=[{}]", groupName, dataId);
        ListenableFuture<Snapshot<String>> snapshot = serverClient.forceReload(meta, VersionProfile.ABSENT.getVersion(), Feature.DEFAULT);
        try {
            Optional<Snapshot<String>> current = Optional.of(snapshot.get());
            logger.info("get current qconfig file successOf, group=[{}], dataId=[{}], {}", groupName, dataId, current.get().getVersion());
            return current;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                logger.info("get current qconfig file failOf, no file, group=[{}], dataId=[{}]", groupName, dataId);
                return Optional.absent();
            }
            throw e;
        }
    }
}
