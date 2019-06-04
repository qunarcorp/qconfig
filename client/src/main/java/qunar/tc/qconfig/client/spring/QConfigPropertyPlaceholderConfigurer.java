package qunar.tc.qconfig.client.spring;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.MapConfig;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * User: zhaohuiyu
 * Date: 5/7/14
 * Time: 4:59 PM
 */
class QConfigPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(QConfigPropertyPlaceholderConfigurer.class);

    private long timeout;

    private final String[] files;

    private boolean ignoreResourceNotFound;

    private boolean trimValue;

    public QConfigPropertyPlaceholderConfigurer(String... files) {
        Preconditions.checkArgument(files != null && files.length > 0, "files必须配置");
        this.files = files;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        MapConfig[] configs = new MapConfig[files.length];

        ListenableFuture<?>[] futures = new ListenableFuture[files.length];
        for (int i = 0; i < files.length; ++i) {
            String file = files[i];
            Map.Entry<Util.File, Feature> entry = Util.parse(file, trimValue);

            configs[i] = MapConfig.get(entry.getKey().group, entry.getKey().file, entry.getValue());

            futures[i] = configs[i].initFuture();
        }

        ListenableFuture<?> future = ignoreResourceNotFound ? Futures.successfulAsList(futures) : Futures.allAsList(futures);
        try {
            List result = (List) future.get(timeout, TimeUnit.MILLISECONDS);
            Properties[] arr = new Properties[configs.length];
            for (int i = 0; i < configs.length; ++i) {
                if (result.get(i) == null) continue;

                MapConfig map = configs[i];
                arr[i] = map.asProperties();
            }
            setPropertiesArray(arr);
        } catch (InterruptedException e) {
            Thread.interrupted();
        } catch (ExecutionException e) {
            logger.error("retrieve config from qconfig error", e.getCause());
            if (e.getCause() instanceof FileNotFoundException) {
                throw new FileNotFoundException(e.getCause().getMessage());
            }
        } catch (TimeoutException e) {
            logger.error("retrieve config from qconfig timeout, timeout: {}", timeout, e);
        }
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
        this.ignoreResourceNotFound = ignoreResourceNotFound;
    }

    public void setTrimValue(boolean trimValue) {
        this.trimValue = trimValue;
    }
}
