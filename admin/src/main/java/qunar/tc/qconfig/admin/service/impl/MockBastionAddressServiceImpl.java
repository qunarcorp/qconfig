package qunar.tc.qconfig.admin.service.impl;


import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Component;
import qunar.tc.qconfig.admin.service.BastionAddressService;

import java.util.List;

/**
 * Created by pingyang.yang on 2018/10/30
 */
@Component
public class MockBastionAddressServiceImpl implements BastionAddressService {

    /**
     * 获取堡垒机地址ip
     */
    @Override
    public List<String> getBastionAddress(String group) {
        return ImmutableList.of();
    }

}
