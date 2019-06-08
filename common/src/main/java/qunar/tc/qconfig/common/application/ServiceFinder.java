package qunar.tc.qconfig.common.application;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author miao.yang susing@gmail.com
 * @date 2014-12-10
 */
public class ServiceFinder<T> {

    private static final ConcurrentHashMap<Class<?>, ServiceFinder<?>> instanceMap = new ConcurrentHashMap<>();
    private static final Map<Class, String> errorMessages = new HashMap<>();

    static {
        errorMessages.put(ServerManagement.class, "请检查是否引用了qconfig-common，并且包版本一致");
    }

    private final Supplier<T> supplier;

    private ServiceFinder(final Class<T> clz) {
        supplier = Suppliers.memoize(new Supplier<T>() {
            @Override
            public T get() {
                ServiceLoader<T> loader = ServiceLoader.load(clz);
                return Iterables.getFirst(loader, null);
            }
        });
    }


    public static <T> T getService(Class<T> clz) {
        T instance = getServiceWithoutCheck(clz);
        Preconditions.checkNotNull(instance, errorMessages.get(clz));
        return instance;
    }

    public static <T> T getServiceWithoutCheck(Class<T> clz) {
        Preconditions.checkNotNull(clz);
        Preconditions.checkArgument(clz.isInterface(), "clz is not a interface");
        ServiceFinder<T> serviceFinder = (ServiceFinder<T>) instanceMap.get(clz);
        if (serviceFinder == null) {
            instanceMap.putIfAbsent(clz, new ServiceFinder<>(clz));
            serviceFinder = (ServiceFinder<T>) instanceMap.get(clz);
        }
        return serviceFinder.supplier.get();
    }

}