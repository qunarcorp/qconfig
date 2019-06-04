package qunar.tc.qconfig.admin.event.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.CharSource;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.EncryptKeyDao;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.event.*;
import qunar.tc.qconfig.admin.model.DiffCount;
import qunar.tc.qconfig.admin.model.DiffResult;
import qunar.tc.qconfig.admin.model.EncryptKey;
import qunar.tc.qconfig.admin.model.PushItemWithHostName;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.EncryptKeyService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.support.DiffUtil;
import qunar.tc.qconfig.admin.support.Differ;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.spring.QMapConfig;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.FileChecker;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.common.util.QConfigAttributesLoader;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static qunar.tc.qconfig.admin.event.MailUtil.*;

/**
 * @author zhenyu.nie created on 2014 2014/5/26 21:42
 */
@Service
public class ConfigMailListener extends MailSender<CandidateDTONotifyBean> implements CandidateDTOChangeListener, CandidateDTOPushListener {

    @Resource
    private UserContextService userContext;

    @Resource
    private ConfigService configService;

    @Resource
    private EncryptKeyDao encryptKeyDao;

    @Resource
    private EncryptKeyService encryptKeyService;

    @Resource
    private Differ differ;

    private static Map<String, Boolean> pushEventMailSwitches = Maps.newHashMap();

    private static final String PUSH_EVENT_MAIL_GLOBAL_KEY = "global.open";

    @QMapConfig(value = "config.properties", key = "notify.mail.ignore.apps")
    private Set<String> ignoreMailApps;

    static {
        MapConfig conf = MapConfig.get("push_mail_switch.properties", Feature.create().setFailOnNotExists(false).build());
        conf.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                Map<String, Boolean> switches = Maps.newHashMapWithExpectedSize(conf.size());
                for (Map.Entry<String, String> entry : conf.entrySet()) {
                    switches.put(entry.getKey(), Boolean.valueOf(entry.getValue()));
                }
                pushEventMailSwitches = switches;
            }
        });
    }

    @Override
    public void candidateDTOChanged(CandidateDTOPushNotifyBean notifyBean) {
        StringBuilder remarksBuilder = new StringBuilder();
        Iterator<PushItemWithHostName> iterator = notifyBean.destinations.iterator();
        while (iterator.hasNext()) {
            PushItemWithHostName host = iterator.next();
            remarksBuilder.append(host.getIp()).append(":").append(host.getPort());
            if (iterator.hasNext()) {
                remarksBuilder.append(",");
            }
        }

        CandidateDTONotifyBean bean = notifyBean.candidateDTONotifyBean;
        CandidateDTONotifyBean copyBean = new CandidateDTONotifyBean(bean.event, bean.operator,
                bean.ip, bean.candidateDTO.copy(), remarksBuilder.toString(), bean.application);

        sendMail(copyBean);
    }

    @Override
    public void candidateDTOChanged(CandidateDTONotifyBean notifyBean) {
        sendMail(notifyBean.copy());
    }

    @Override
    protected boolean needMail(CandidateDTONotifyBean notifyBean) {
        if (ProfileUtil.affectProd(notifyBean.candidateDTO.getProfile())) {
            if (notifyBean.event == ConfigOperationEvent.PUSH) {
                // 开关判断
                return pushEventCanMail(notifyBean.candidateDTO.getGroup());
            }
            if (notifyBean.event == ConfigOperationEvent.PUBLISH && notifyBean.candidateDTO.isSendMail()) {
                return ignoreMailApps == null || !ignoreMailApps.contains(notifyBean.candidateDTO.getGroup());
            }
            if (notifyBean.event == ConfigOperationEvent.NEW) {
                return ignoreMailApps == null || !ignoreMailApps.contains(notifyBean.candidateDTO.getGroup());
            }
            if (notifyBean.event == ConfigOperationEvent.UPDATE && notifyBean.candidateDTO.isSendMail()) {
                return ignoreMailApps == null || !ignoreMailApps.contains(notifyBean.candidateDTO.getGroup());
            }
        }
        return false;
    }

    @Override
    protected Set<String> generateNames(CandidateDTONotifyBean notifyBean) {
        return userContext.getRelativeMailAddresses(notifyBean.candidateDTO.getGroup(), notifyBean.operator);
    }

    @Override
    protected String generateContent(CandidateDTONotifyBean notifyBean) {
        CandidateDTO candidateDTO = notifyBean.candidateDTO;
        StringBuilder sb = new StringBuilder();

        long version = candidateDTO.getEditVersion();
        // 推送，编辑版本号并不会上升
        if (notifyBean.event != ConfigOperationEvent.PUSH) {
            version += 1;
        }

        String link;
        if (notifyBean.event == ConfigOperationEvent.NEW ||
                notifyBean.event == ConfigOperationEvent.UPDATE && notifyBean.candidateDTO.isSendMail()) {
            link = String.format(APPROVE_LINK_TEMPLATE, candidateDTO.getGroup(), candidateDTO.getProfile(),
                    candidateDTO.getDataId(), version);
        } else {
            link = String.format(CANDIDATE_LINK_TEMPLATE, candidateDTO.getGroup(), candidateDTO.getProfile(),
                    candidateDTO.getDataId(), version);
        }
        sb.append(link).append(LINE);
        sb.append("应用：").append(candidateDTO.getGroup()).append(LINE);
        sb.append("操作类型：").append(notifyBean.event.text()).append(LINE);
        sb.append("环境：").append(Environment.fromProfile(candidateDTO.getProfile()).text()).append(LINE);
        sb.append(QConfigAttributesLoader.getInstance().getBuildGroup()).append("：").
                append(ProfileUtil.getBuildGroup(candidateDTO.getProfile())).append(LINE);
        sb.append("文件名：").append(candidateDTO.getDataId()).append(LINE);
        sb.append("版本号：").append(version).append(LINE);
        sb.append("备注：").append(notifyBean.remarks).append(LINE);
        sb.append("操作人：").append(notifyBean.operator);

        if (notifyBean.event == ConfigOperationEvent.PUBLISH
                || notifyBean.event == ConfigOperationEvent.NEW
                || notifyBean.event == ConfigOperationEvent.UPDATE
                || notifyBean.event == ConfigOperationEvent.PUSH) {
            String currentData = candidateDTO.getData();
            if (notifyBean.event == ConfigOperationEvent.PUSH) {
                currentData = getCurrentData(notifyBean);
            }

            if (FileChecker.isJsonFile(candidateDTO.getDataId())) {
                CandidateSnapshot lastPublish = configService.findLastPublish(new ConfigMeta(candidateDTO.getGroup(),
                        candidateDTO.getDataId(), candidateDTO.getProfile()), version);
                String diffUrl = String.format(DIFF_LINK_TEMPLATE, candidateDTO.getGroup(), candidateDTO.getDataId(),
                        candidateDTO.getProfile(), lastPublish == null ? 0 : lastPublish.getEditVersion(), version);
                sb.append(LINE).append(diffUrl);
            } else {
                DiffResult<String> result = generatePrettyDiffHtml(notifyBean.candidateDTO,
                        configService.templateDataLongToStr(candidateDTO.getGroup(), candidateDTO.getDataId(), getOldData(notifyBean, version)),
                        configService.templateDataLongToStr(candidateDTO.getGroup(), candidateDTO.getDataId(), currentData));
                DiffCount diffCount = result.getDiffCount();
                boolean isTemplateFile = FileChecker.isTemplateFile(candidateDTO.getDataId());
                sb.append(LINE)
                        .append("文件变更：")
                        .append(DiffUtil.diffText(candidateDTO.getDataId(), diffCount, isTemplateFile))
                        .append(LINE)
                        .append(result.getResult());
            }
        }
        return sb.toString();
    }

    private String getOldData(CandidateDTONotifyBean notifyBean, long currentVersion) {
        CandidateDTO candidateDTO = notifyBean.candidateDTO;
        ConfigMeta meta = new ConfigMeta(candidateDTO.getGroup(), candidateDTO.getDataId(), candidateDTO.getProfile());
        CandidateSnapshot snapshot = configService.findLastPublish(meta, currentVersion);
        return snapshot == null ? "" : configService.templateDataLongToStr(candidateDTO.getGroup(), candidateDTO.getDataId(), snapshot.getData());

    }

    private String getCurrentData(CandidateDTONotifyBean notifyBean) {
        CandidateDTO candidateDTO = notifyBean.candidateDTO;
        ConfigMeta meta = new ConfigMeta(candidateDTO.getGroup(), candidateDTO.getDataId(), candidateDTO.getProfile());
        CandidateSnapshot snapshot = configService.currentEditSnapshot(meta);
        return snapshot == null ? "" : configService.templateDataLongToStr(candidateDTO.getGroup(), candidateDTO.getDataId(), snapshot.getData());
    }

    /**
     * 加密旧config文件中需要加密的key
     *
     * @param older           旧config的内容
     * @param encryptKeys     需要加密的key
     * @param toEncryptValues 输出被加密了的值
     * @return 加密后的config的内容
     * @throws IOException
     */
    private String encryptOlder(final String older,
                                final List<EncryptKey> encryptKeys,
                                final Map<String, String> toEncryptValues)
            throws IOException {
        final StringBuilder encryptedOlder = new StringBuilder(older.length() + 200);
        for (String oldLine : CharSource.wrap(older).readLines()) {
            if (!needEncrypt(oldLine, encryptKeys)) {
                encryptedOlder.append(oldLine);
            } else {
                int indexOfEqualSign = getLeftKeyIndex(oldLine);
                final String leftOfEqualSign = oldLine.substring(0, indexOfEqualSign);
                final String key = getKeyFromLeftOfEqualSign(leftOfEqualSign);
                if (!isComment(oldLine)) {
                    toEncryptValues.put(key, oldLine.substring(indexOfEqualSign + 1).trim());
                }
                encryptedOlder.append(leftOfEqualSign).append("=").append("加密数据");
            }
            encryptedOlder.append("\n");
        }

        return encryptedOlder.toString();
    }

    /**
     * 加密新config文件中需要加密的key
     *
     * @param newer           新config文件的内容
     * @param encryptKeys     需要加密的key
     * @param toEncryptValues 旧config文件中被加密了的值
     * @return 加密后的config的内容
     * @throws IOException
     */
    private String encryptNewer(final String newer,
                                final List<EncryptKey> encryptKeys,
                                final Map<String, String> toEncryptValues)
            throws IOException {
        final StringBuilder encryptedNewer = new StringBuilder(newer.length() + 200);
        for (String newLine : CharSource.wrap(newer).readLines()) {
            if (!needEncrypt(newLine, encryptKeys)) {
                encryptedNewer.append(newLine);
            } else {
                final int indexOfEqualSign = getLeftKeyIndex(newLine);
                final String leftOfEqualSign = newLine.substring(0, indexOfEqualSign);
                final String key = getKeyFromLeftOfEqualSign(leftOfEqualSign);
                final String oldValue = Strings.nullToEmpty(toEncryptValues.get(key));

                if (isComment(newLine) || newLine.substring(indexOfEqualSign + 1).trim().equals(oldValue)) {
                    encryptedNewer.append(leftOfEqualSign).append("=").append("加密数据");
                } else {
                    encryptedNewer.append(leftOfEqualSign).append("=").append("加密数据 (有修改)");
                }
            }

            encryptedNewer.append("\n");
        }

        return encryptedNewer.toString();
    }

    DiffResult<String> generatePrettyDiffHtml(CandidateDTO candidateDTO, String older, String newer) {
        boolean isTemplateFile = FileChecker.isTemplateFile(candidateDTO.getDataId());
        try {
            if (!isTemplateFile && candidateDTO.getDataId().endsWith(".properties")) {
                final List<EncryptKey> encryptKeys = encryptKeyDao.select(candidateDTO.getGroup(),
                        candidateDTO.getDataId());
                final Map<String, String> toEncryptValues = Maps.newHashMap();

                older = encryptOlder(older, encryptKeys, toEncryptValues);
                newer = encryptNewer(newer, encryptKeys, toEncryptValues);

            }
            final DiffResult<String> diffResult = differ.diffToHtmlWithEncrypt(older, newer, candidateDTO.getGroup(), candidateDTO.getDataId());
            final String formatContent = PRETTY_PREFIX + diffResult.getResult() + PRETTY_SUFFIX;
            return new DiffResult<String>(diffResult.getDiffCount(), formatContent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isComment(String line) {
        return line.trim().startsWith("#");
    }

    private String getKeyFromLeftOfEqualSign(String leftToEqualSign) {
        String key = leftToEqualSign.trim();
        if (key.startsWith("#")) {
            key = key.substring(1).trim();
        }
        return key;
    }

    private boolean needEncrypt(String line, List<EncryptKey> encryptKeys) {
        int index = getLeftKeyIndex(line);
        if (index <= 0) {
            return false;
        }

        String leftToEqualSign = line.substring(0, index);
        String key = getKeyFromLeftOfEqualSign(leftToEqualSign);
        return !key.isEmpty()
                && !key.startsWith("#")
                && encryptKeyService.isEncryptedKey(encryptKeys, key);
    }

    private int getLeftKeyIndex(String line) {
        int indexOfEqualSign = line.indexOf('=');
        int indexOfColonSign = line.indexOf(':');
        int index;
        if (indexOfColonSign >= 0 && indexOfEqualSign >= 0) {
            index = indexOfColonSign < indexOfEqualSign ? indexOfColonSign : indexOfEqualSign;
        } else if (indexOfColonSign < 0) {
            index = indexOfEqualSign;
        } else {
            index = indexOfColonSign;
        }
        return index;
    }

    private static final String PRETTY_PREFIX = "<div style=\"border: 1px solid #dedede;" + "-moz-border-radius: 10px;"
            + "-webkit-border-radius: 10px;" + "border-radius:10px;" + "margin:10px;padding:30px;"
            + "background-color:#EEEEEE;\">";

    private static final String PRETTY_SUFFIX = "</div>";

    @Override
    protected String generateTitle(CandidateDTONotifyBean notifyBean) {
        CandidateDTO candidateDTO = notifyBean.candidateDTO;
        return String.format(TITLE_TEMPLATE,
                candidateDTO.getGroup(),
                Environment.fromProfile(candidateDTO.getProfile()).text(),
                ProfileUtil.getBuildGroup(candidateDTO.getProfile()),
                candidateDTO.getDataId(),
                notifyBean.event.text(),
                notifyBean.operator);
    }

    private boolean pushEventCanMail(String appId) {
        if (pushEventMailSwitches.containsKey(appId)) {
            return pushEventMailSwitches.get(appId);
        }

        Boolean open = pushEventMailSwitches.get(PUSH_EVENT_MAIL_GLOBAL_KEY);
        return open == null ? false : open;
    }

}
