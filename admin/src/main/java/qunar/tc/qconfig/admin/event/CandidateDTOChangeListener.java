package qunar.tc.qconfig.admin.event;

import com.google.common.eventbus.Subscribe;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-5-26
 * Time: 下午2:30
 */
public interface CandidateDTOChangeListener {

    @Subscribe
    void candidateDTOChanged(CandidateDTONotifyBean notifyBean);
}
