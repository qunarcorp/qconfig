package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.common.bean.CandidateSnapshot;

/**
 * @author keli.wang
 */
public interface RebuildPropertiesEntryService {
    interface ProgressListener {
        void onProgress(final CandidateSnapshot snapshot);
    }

    void rebuildPropertiesEntry(final ProgressListener listener);
}
