package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.model.Draft;

/**
 * User: zhaohuiyu
 * Date: 5/14/14
 * Time: 6:19 PM
 */
public interface DraftService {

    /**
     * 保存草稿
     */
    void save(Draft draft);
}
