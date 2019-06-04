package qunar.tc.qconfig.admin.service;

import java.util.List;

/**
 * Created by pingyang.yang on 2018/10/30
 */
public interface BastionAddressService {

    List<String> getBastionAddress(String group);
}
