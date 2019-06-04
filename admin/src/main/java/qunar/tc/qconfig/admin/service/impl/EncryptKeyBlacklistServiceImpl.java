package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.model.EncryptKeyBlacklist;
import qunar.tc.qconfig.admin.service.EncryptKeyBlacklistService;


/**
 * @author keli.wang
 */
@Service
public class EncryptKeyBlacklistServiceImpl implements EncryptKeyBlacklistService {

    protected static final Logger logger = LoggerFactory.getLogger(EncryptKeyBlacklistServiceImpl.class);

    private volatile EncryptKeyBlacklist blacklist = EncryptKeyBlacklist.emptyBlacklist();
//    }

    private boolean inContainsBlacklist(final String key) {
        return blacklist.getContainsBlacklist().stream().anyMatch(key::contains);
    }

    private boolean inStartWithBlacklist(final String key) {
        return blacklist.getStartWithBlacklist().stream().anyMatch(key::startsWith);
    }

    private boolean inEndWithBlacklist(final String key) {
        return blacklist.getEndWithBlacklist().stream().anyMatch(key::endsWith);
    }

    @Override
    public boolean inBlacklist(final String key) {
        Preconditions.checkNotNull(key,
                                   "inBlacklist function doesn't accept null argument");

        return inContainsBlacklist(key) ||
                inStartWithBlacklist(key) ||
                inEndWithBlacklist(key);
    }
}
