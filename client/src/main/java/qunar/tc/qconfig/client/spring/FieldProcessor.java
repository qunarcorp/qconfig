package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.Feature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class FieldProcessor {
    private final List<Processor> processors;

    FieldProcessor(Class<?> clazz, AnnotationManager manager) {
        processors = new ArrayList<>();
        ProcessorFactory factory = new ProcessorFactory();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Annotation annotation = manager.extractAnnotation(field);
            if (annotation == null || annotation instanceof DisableQConfig) {
                continue;
            }

            Class<?> fieldType = field.getType();
            Type fieldGenericType = field.getGenericType();
            Map.Entry<Util.File, Feature> fileInfo = manager.getFileInfo(annotation);
            Action action = new FieldSetter(fileInfo.getKey(), field);
            factory.create(annotation, fieldType, fieldGenericType, fileInfo, action, processors, manager);
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
