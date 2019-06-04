package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.servercommon.bean.VersionData;

/**
 * User: zhaohuiyu
 * Date: 5/14/14
 * Time: 7:51 PM
 *
 * group + dataId + profile + author + createTs 决定一个草稿
 */
public class Draft {

    /**
     * 草稿作者
     */
    private String author;

    /**
     * 创建时间
     */
    private long createTs;

    private VersionData<Config> config;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getCreateTs() {
        return createTs;
    }

    public void setCreateTs(long createTs) {
        this.createTs = createTs;
    }

    public VersionData<Config> getConfig() {
        return config;
    }

    public void setConfig(VersionData<Config> config) {
        this.config = config;
    }
}
