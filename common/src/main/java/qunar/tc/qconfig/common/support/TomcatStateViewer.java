package qunar.tc.qconfig.common.support;

import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2018 2018/3/26 12:31
 * 通过isStopped方法判断tomcat当前是否已经停止，未使用tomcat则认为一直在运行，返回false
 */
public class TomcatStateViewer {

    private static final Set<String> TOMCAT_CLASSLOADER_NAMES = ImmutableSet.of(
            "org.apache.catalina.loader.WebappClassLoader",
            "org.apache.catalina.loader.ParallelWebappClassLoader");

    private static final String TOMCAT_CLASSLOADER_BASE = "org.apache.catalina.loader.WebappClassLoaderBase";

    private static final AvailableJudgement ALWAYS_AVAILABLE = new AvailableJudgement() {
        @Override
        public boolean available() throws Exception {
            return true;
        }
    };

    private static final TomcatStateViewer viewer = new TomcatStateViewer();

    public static TomcatStateViewer getInstance() {
        return viewer;
    }

    private AvailableJudgement judgement;

    private TomcatStateViewer() {
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            Class<?> stateHoldClass = getStateHoldClass(classLoader);
            judgement = getAvailableJudgement(classLoader, stateHoldClass);
            judgement.available();
        } catch (Exception e) {
            judgement = ALWAYS_AVAILABLE;
        }
    }

    public boolean isStopped() {
        try {
            return !judgement.available();
        } catch (Exception e) {
            return true;
        }
    }

    private Class<?> getStateHoldClass(ClassLoader classLoader) {
        if (classLoader == null) {
            return null;
        }

        Class<? extends ClassLoader> classloaderClass = classLoader.getClass();
        // 也许可以让WebappClassLoaderBase的子类都通过，不过也增加了不确定性，目前看来tomcat7,8,9到现在为止都只有set里的两个ClassLoader
        if (!TOMCAT_CLASSLOADER_NAMES.contains(classloaderClass.getCanonicalName())) {
            return null;
        }

        Class parentLoaderClass = classloaderClass.getSuperclass();
        if (TOMCAT_CLASSLOADER_BASE.equals(parentLoaderClass.getCanonicalName())) {
            return parentLoaderClass;
        }

        return classloaderClass;
    }

    private AvailableJudgement getAvailableJudgement(ClassLoader classLoader, Class<?> clazz) throws NoSuchFieldException, ClassNotFoundException {
        if (clazz == null) {
            return ALWAYS_AVAILABLE;
        }

        AvailableJudgement startedFiledJudgement = getStartedFieldJudgement(classLoader, clazz);
        if (startedFiledJudgement != null) {
            return startedFiledJudgement;
        }

        AvailableJudgement stateFieldJudgement = getStateFieldJudgement(classLoader, clazz);
        if (stateFieldJudgement != null) {
            return stateFieldJudgement;
        }

        return ALWAYS_AVAILABLE;
    }

    private AvailableJudgement getStateFieldJudgement(final ClassLoader classLoader, Class<?> clazz) {
        final Field stateField;
        try {
            stateField = clazz.getDeclaredField("state");
        } catch (NoSuchFieldException e) {
            return null;
        }

        try {
            Class<?> lifecycleStateClass = Class.forName("org.apache.catalina.LifecycleState");
            if (stateField.getType().equals(lifecycleStateClass)) {
                stateField.setAccessible(true);
                final Field availableField = lifecycleStateClass.getDeclaredField("available");
                availableField.setAccessible(true);
                return new AvailableJudgement() {
                    @Override
                    public boolean available() throws Exception {
                        Object lifecycleState = stateField.get(classLoader);
                        if (lifecycleState == null) {
                            // 这里不应该为null，如果以后tomcat出现有null的实现，那不确定是否available，不确定的时候就当作available
                            return true;
                        }
                        Object state = availableField.get(lifecycleState);
                        if (state == null) {
                            // 原因同上，不确定的时候当作available
                            return true;
                        }
                        return (Boolean) state;
                    }
                };
            } else {
                return ALWAYS_AVAILABLE;
            }
        } catch (Exception e) {
            return ALWAYS_AVAILABLE;
        }
    }

    private AvailableJudgement getStartedFieldJudgement(final ClassLoader classLoader, Class<?> clazz) {
        try {
            final Field startedField = clazz.getDeclaredField("started");
            if (startedField.getType().equals(boolean.class)) {
                startedField.setAccessible(true);
                return new AvailableJudgement() {
                    @Override
                    public boolean available() throws Exception {
                        return (Boolean) startedField.get(classLoader);
                    }
                };
            } else {
                return ALWAYS_AVAILABLE;
            }
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private interface AvailableJudgement {
        boolean available() throws Exception;
    }
}

