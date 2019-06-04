package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.Feature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class MethodProcessor {
    private final List<Processor> processors;

    MethodProcessor(Class<?> clazz, AnnotationManager manager) {
        ProcessorFactory factory = new ProcessorFactory();
        processors = new ArrayList<>();

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            Annotation annotation = manager.extractAnnotation(method);
            if (annotation == null || annotation instanceof DisableQConfig) {
                continue;
            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1) {
                throw new RuntimeException("method receives qconfig change must be on parameter, method: " + method);
            }

            Map.Entry<Util.File, Feature> fileInfo = manager.getFileInfo(annotation);
            Action action = new MethodInvoker(fileInfo.getKey(), method);

            factory.create(annotation, parameterTypes[0], method.getGenericParameterTypes()[0], fileInfo, action, processors, manager);
        }
    }

    public boolean process(Object bean) {
        if (processors.isEmpty()) return false;
        for (Processor processor : processors) {
            processor.process(bean);
        }
        return true;
    }
}
