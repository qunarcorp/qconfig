package qunar.tc.qconfig.client;


import qunar.tc.qconfig.client.impl.AbstractConfiguration;

/**
 * @author miao.yang susing@gmail.com
 * @since 14-5-9.
 */
public interface DataLoader {

    <T> Configuration<T> load(String groupName, String fileName, Feature feature, Generator<T> generator);

    <T> Configuration<T> load(String fileName, Feature feature, Generator<T> generator);

    <T> Configuration<T> load(String fileName, Generator<T> generator);

    /**
     * Generator 用于解析配置和生成包装实例. 一个配置仅有一个Parser
     *
     * @param <T>
     */
    interface Generator<T> {
        AbstractConfiguration<T> create(Feature feature, String fileName);
    }
}
