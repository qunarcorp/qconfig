package qunar.tc.qconfig.client;

import com.google.common.base.Optional;
import qunar.tc.qconfig.client.impl.Snapshot;
import qunar.tc.qconfig.client.impl.VersionProfile;

/**
 * 使用ConfigUploader.getInstance()获取Uploader
 * 为客户端提供了一部分管理配置文件的功能。
 * 只能操作本应用下的配置，也就是classpath:/META-INF/app.properties内app.ip配置的应用<br />
 *
 * 文件发布流程为:<br /><br />
 * 编辑-提交-审核通过-发布<br />
 * &nbsp; &nbsp; &nbsp;  &nbsp;|&nbsp; &nbsp; &nbsp; &nbsp;&nbsp; &nbsp; |<br />
 * &nbsp; &nbsp; &nbsp; 拒绝&nbsp; &nbsp; &nbsp; 取消<br />
 * @author zhenyu.nie created on 2015 2015/4/20 14:09
 */
public interface Uploader {

    /**
     * 根据qconfig在机器上缓存的版本信息作为version调用uploadAtVersion，
     * 版本信息为系统最近一次使用dataId文件的远程版本；如果不存在，则相当于新建文件
     * upload方法应该单独使用，默认采用一键发布
     *
     * @param dataId 上传或更新的配置文件名称
     * @param data   上传或更新的配置文件内容
     * @return 响应信息
     */
    UploadResult upload(String dataId, String data) throws Exception;

    /**
     * 获取名为dataId的文件的当前快照信息，获取的规则与应用使用qconfig拉取的规则相同，如果不存在则返回Optional.absent();
     * getCurrent方法应该与uploadAtVersion方法配合使用
     *
     * @param dataId 希望获取的配置文件名称
     * @return 文件内容和版本信息
     */
    Optional<Snapshot<String>> getCurrent(String dataId) throws Exception;


    /**
     * 上传文件到qconfig，dataId为文件名
     * 如果根据qconfig文件拉取规则，系统当前可以获取到的文件版本为version，那么上传成功，否则会失败
     * 如果要新建文件，传入version应为VersionProfile.ABSENT；新文件将创建在本机器所在环境（dev，beta，prod）对应的buildGroup中
     * uploadAtVersion方法应该与getCurrent方法配合使用，默认采用一键发布
     *
     * @param versionProfile 上传或更新的版本号和环境
     * @param dataId  上传或更新的配置文件名称
     * @param data    上传或更新的配置文件内容
     * @return 响应信息
     */
    UploadResult uploadAtVersion(VersionProfile versionProfile, String dataId, String data) throws Exception;

    /**
     * 上传文件到qconfig，dataId为文件名
     * 如果根据qconfig文件拉取规则，系统当前可以获取到的文件版本为version，那么上传成功，否则会失败
     * 如果要新建文件，传入version应为VersionProfile.ABSENT；新文件将创建在本机器所在环境（dev，beta，prod）对应的buildGroup中
     * uploadAtVersion方法应该与getCurrent方法配合使用，默认采用一键发布
     *
     * @param versionProfile 上传或更新的版本号和环境
     * @param dataId  上传或更新的配置文件名称
     * @param data    上传或更新的配置文件内容
     * @param isPublic 是否同时设置为公共文件
     * @return 响应信息
     */
    UploadResult uploadAtVersion(VersionProfile versionProfile, String dataId, String data, boolean isPublic) throws Exception ;

    /**
     * 提交文件，对应portal上提交文件；如果需要新建配置，那么snapshot里面传入VersionProfile.ABSENT;
     * 否则通过loadCandidateSnapShotData(String)获取配置最新的快照
     *
     * @param versionProfile 版本号和环境
     * @param dataId 配置名称
     * @param operator 操作人
     * @param description 配置描述
     * @return 响应信息
     * @see #loadCandidateSnapShotData(String)
     *
     */
    UploadResult apply(VersionProfile versionProfile, String dataId, String data, String operator, String description) throws Exception;

    /**
     * 审核通过已经提交的文件，对应portal审核通过
     *
     * @param versionProfile 版本号和环境
     * @param dataId 配置名称
     * @param operator 操作人
     * @return 响应信息
     * @see #loadCandidateSnapShotData(String)
     */
    UploadResult approve(VersionProfile versionProfile, String dataId, String operator) throws Exception ;

    /**
     * 拒绝已经提交的文件，对应protal拒绝通过
     * @param versionProfile 版本号和环境
     * @param dataId 配置名称
     * @param operator 操作人
     * @return 响应信息
     * @see #loadCandidateSnapShotData(String)
     */
    UploadResult reject(VersionProfile versionProfile, String dataId, String operator) throws Exception ;


    /**
     * 取消已经审核通过的文件，对应protal上取消发布
     * @param versionProfile 版本号和环境
     * @param dataId 配置名称
     * @param operator 操作人
     * @return 响应信息
     * @see #loadCandidateSnapShotData(String)
     */
    UploadResult cancel(VersionProfile versionProfile, String dataId, String operator) throws Exception ;

    /**
     * 发布配置，对应protal上发布配置
     * @param versionProfile 版本号和环境
     * @param dataId 配置名称
     * @param operator 操作人
     * @param isPublic 是否是公共文件
     * @return 响应信息
     * @see #loadCandidateSnapShotData(String)
     */
    UploadResult publish(VersionProfile versionProfile, String dataId, String operator, boolean isPublic) throws Exception ;

    /**
     * 获取本应用最新未发布版本信息
     * @param dataId
     *  配置名称
     *
     * @return
     */
    Optional<Snapshot<String>> loadCandidateSnapShotData(String dataId) throws Exception ;

}
