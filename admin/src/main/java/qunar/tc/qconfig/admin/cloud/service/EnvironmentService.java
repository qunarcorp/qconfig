package qunar.tc.qconfig.admin.cloud.service;

import java.util.List;
import java.util.Map;

public interface EnvironmentService {

    List<String> getSystemDefaultEnvs();

    Map<String, Integer> getEnvDisplayOrders();
}
