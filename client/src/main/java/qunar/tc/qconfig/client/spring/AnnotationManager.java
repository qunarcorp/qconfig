package qunar.tc.qconfig.client.spring;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import qunar.tc.qconfig.client.Feature;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

class AnnotationManager {
    private static final Logger LOG = LoggerFactory.getLogger(AnnotationManager.class);

    private static final Set<Class<? extends Annotation>> loadAnnotations = ImmutableSet.of(
            QConfig.class,
            QMapConfig.class,
            QTableConfig.class,
            DisableQConfig.class);

    private List<WeakReference<Object>> beans;

    private BeanProcessor beanProcessor;

    private FieldProcessor fieldProcessor;

    private MethodProcessor methodProcessor;

    private boolean trimValue;

    private volatile boolean inited = false;

    public AnnotationManager(boolean trimValue) {
        this.trimValue = trimValue;
    }

    private final Object INIT_LOCK = new Object();

    private final Object EXECUTED_LOCK = new Object();

    void init(Class<?> clazz) {
        if (inited) return;
        synchronized (INIT_LOCK) {
            if (inited) return;
            this.beanProcessor = new BeanProcessor(clazz, this);
            this.fieldProcessor = new FieldProcessor(clazz, this);
            this.methodProcessor = new MethodProcessor(clazz, this);
            inited = true;
        }

    }

    void processBean(Object bean) {
        boolean hasBean = beanProcessor.process(bean);
        boolean hasField = fieldProcessor.process(bean);
        boolean hasMethod = methodProcessor.process(bean);

        if (hasBean || hasField || hasMethod) {
            synchronized (EXECUTED_LOCK) {
                if (beans == null) {
                    beans = new ArrayList<>();
                }
                beans.add(new WeakReference<>(bean));
            }
        }
    }

    public void process(Action action, Object value, QConfigLogLevel logLevel) {
        synchronized (EXECUTED_LOCK) {
            if (beans == null) return;
            Iterator<WeakReference<Object>> iterator = beans.iterator();
            while (iterator.hasNext()) {
                WeakReference<Object> next = iterator.next();
                Object bean = next.get();
                if (bean == null) {
                    iterator.remove();
                    continue;
                }

                action.act(bean, value, logLevel);
            }
        }
    }

    private String getFile(Annotation annotation) {
        if (annotation instanceof QMapConfig) {
            return ((QMapConfig) annotation).value();
        }

        if (annotation instanceof QTableConfig) {
            return ((QTableConfig) annotation).value();
        }

        if (annotation instanceof QConfig) {
            return ((QConfig) annotation).value();
        }

        return null;
    }

    public Map.Entry<Util.File, Feature> getFileInfo(Annotation annotation) {
        String file = getFile(annotation);
        return getFileInfo(file);
    }

    public Map.Entry<Util.File, Feature> getFileInfo(String file) {
        if (Strings.isNullOrEmpty(file)) {
            throw new RuntimeException("file name can not be empty");
        }

        return Util.parse(file, trimValue);
    }

    public Annotation extractAnnotation(Field field) {
        Set<Annotation> annotations = getAnnotations(field);
        return extractOneAnnotation(annotations);
    }

    public Set<Annotation> getAnnotations(Field field) {
        Set<Annotation> annotations = Sets.newHashSet();
        for (Class<? extends Annotation> annotationClass : loadAnnotations) {
            Annotation annotation = AnnotationUtils.getAnnotation(field, annotationClass);
            if (annotation != null) {
                annotations.add(annotation);
            }
        }
        return annotations;
    }

    private static Annotation extractOneAnnotation(Set<Annotation> annotations) {
        if (annotations.isEmpty()) {
            return null;
        } else if (annotations.size() == 1) {
            return annotations.iterator().next();
        } else {
            LOG.error("conflict annotations, {}", annotations);
            throw new RuntimeException("conflict annotations, " + annotations);
        }
    }

    public Annotation extractAnnotation(Method method) {
        Set<Annotation> annotations = Sets.newHashSet();
        for (Class<? extends Annotation> annotationClass : loadAnnotations) {
            Annotation annotation = AnnotationUtils.findAnnotation(method, annotationClass);
            if (annotation != null) {
                annotations.add(annotation);
            }
        }
        return extractOneAnnotation(annotations);
    }
}
