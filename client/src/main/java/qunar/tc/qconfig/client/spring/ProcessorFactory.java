package qunar.tc.qconfig.client.spring;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.PropertiesChange;
import qunar.tc.qconfig.client.QTable;
import qunar.tc.qconfig.client.impl.*;
import qunar.tc.qconfig.common.util.FileChecker;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Properties;

class ProcessorFactory {
    public void create(Annotation annotation, Class<?> clazz, Type genericType, Map.Entry<Util.File, Feature> fileInfo,
                       Action action, List<Processor> processors, AnnotationManager manager) {
        if (processQConfig(annotation, clazz, genericType, fileInfo, action, processors, manager)) return;
        if (processQMapConfig(annotation, clazz, genericType, fileInfo, action, processors, manager)) return;
        processQTableConfig(annotation, clazz, genericType, fileInfo, action, processors, manager);
    }

    private void processQTableConfig(Annotation annotationObject, Class<?> clazz, Type genericType,
                                     Map.Entry<Util.File, Feature> fileInfo, Action action, List<Processor> processors, AnnotationManager manager) {
        if (!(annotationObject instanceof QTableConfig)) return;

        QTableConfig annotation = (QTableConfig) annotationObject;
        Util.File file = fileInfo.getKey();
        Feature feature = fileInfo.getValue();

        final QConfigLogLevel logLevel = annotation.logLevel();
        Class<? extends QConfigTableTranslator> translatorClass = annotation.translator();
        boolean translatorExist = !TrivialTableTranslator.class.equals(translatorClass);
        if (translatorExist) {
            processors.add(new QTableTranslatorProcessor(translatorClass, file.group, file.file, feature, action, logLevel, manager));
        } else {
            if (clazz.equals(QTable.class)) {
                processors.add(new QTableProcessor(file.group, file.file, feature, action, logLevel, manager));
                return;
            }

            Class listBeanClass = getListBeanClass(genericType);
            if (listBeanClass != null) {
                processors.add(new ListProcessor(file.group, file.file, feature, listBeanClass, action, logLevel, manager));
                return;
            }

            Class mapBeanClass = getMapBeanClass(genericType);
            if (mapBeanClass != null) {
                processors.add(new MapProcessor(file.group, file.file, feature, mapBeanClass, action, logLevel, manager));
            }
        }
    }

    private Class getMapBeanClass(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }

        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type rawType = parameterizedType.getRawType();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (rawType.equals(Map.class) && actualTypeArguments != null && actualTypeArguments.length == 2 && actualTypeArguments[0].equals(String.class)) {
            return getTypeClass(actualTypeArguments[1]);
        }

        return null;
    }

    private Class getListBeanClass(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }

        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type rawType = parameterizedType.getRawType();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (rawType.equals(List.class) && actualTypeArguments != null && actualTypeArguments.length == 1) {
            return getTypeClass(actualTypeArguments[0]);
        }

        return null;
    }

    private Class getTypeClass(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            return getTypeClass(((ParameterizedType) type).getRawType());
        } else {
            return null;
        }
    }

    private boolean processQConfig(Annotation annotationObject, Class<?> clazz, Type genericType,
                                   Map.Entry<Util.File, Feature> fileInfo, Action action, List<Processor> processors, AnnotationManager manager) {
        if (!(annotationObject instanceof QConfig)) return false;

        QConfig annotation = (QConfig) annotationObject;
        Util.File file = fileInfo.getKey();
        Feature feature = fileInfo.getValue();
        final QConfigLogLevel logLevel = annotation.logLevel();

        if (clazz.equals(Properties.class)) {
            processors.add(new PropertiesProcessor(file.group, file.file, feature, action, logLevel, manager));
            return true;
        }
        if (clazz.equals(Map.class)) {
            processors.add(new PureMapProcessor(file.group, file.file, feature, action, logLevel, manager));
            return true;
        }
        if (clazz.equals(String.class)) {
            processors.add(new StringProcessor(file.group, file.file, feature, action, logLevel, manager));
            return true;
        }
        if (clazz.equals(QTable.class)) {
            processors.add(new QTableProcessor(file.group, file.file, feature, action, logLevel, manager));
            return true;
        }
        if (FileChecker.isJsonFile(file.file)) {
            processors.add(new JsonProcessor(genericType, file.group, file.file, feature, action, logLevel, manager));
            return true;
        }
        if (clazz.equals(PropertiesChange.class)) {
            processors.add(new PropertiesChangeProcessor(file.group, file.file, feature, action, logLevel, manager));
            return true;
        }
        return true;
    }

    private boolean processQMapConfig(Annotation annotationObject, Class<?> clazz, Type genericType,
                                      Map.Entry<Util.File, Feature> fileInfo, Action action, List<Processor> processors, AnnotationManager manager) {
        if (!(annotationObject instanceof QMapConfig)) return false;

        QMapConfig annotation = (QMapConfig) annotationObject;
        checkQMapConfig(annotation, action);

        Util.File file = fileInfo.getKey();
        Feature feature = fileInfo.getValue();

        final String key = annotation.key();
        final QConfigLogLevel logLevel = annotation.logLevel();
        Class<? extends QConfigMapTranslator> translatorClass = annotation.translator();
        boolean keyExist = !Strings.isNullOrEmpty(key);
        boolean translatorExist = !TrivialMapTranslator.class.equals(translatorClass);

        if (keyExist && translatorExist)
            throw new RuntimeException("key and translator can not exist both");

        if (!keyExist && !translatorExist) {
            if (clazz.equals(Properties.class)) {
                processors.add(new PropertiesProcessor(file.group, file.file, feature, action, logLevel, manager));
                return true;
            }
            if (isSimpleMapField(clazz, genericType)) {
                processors.add(new PureMapProcessor(file.group, file.file, feature, action, logLevel, manager));
                return true;
            }
            if (clazz.equals(Map.class)) {
                processors.add(new TypedMapProcessor(genericType, file.group, file.file, feature, action, logLevel, manager));
                return true;
            } else {
                processors.add(new PojoProcessor(file.group, file.file, feature, clazz, action, logLevel, manager));
                return true;
            }
        } else if (translatorExist) {
            final QConfigMapTranslator translator = Translators.doGetUserMapTranslator(translatorClass);
            processors.add(new TranslatorProcessor(translator, file.group, file.file, feature, action, logLevel, manager));
        } else {
            processors.add(new ValueTranslatorProcessor(genericType, key, annotation.defaultValue(), file.group, file.file, feature, action, logLevel, manager));
        }
        return true;
    }

    private boolean isSimpleMapField(Class<?> clazz, Type type) {
        if (!clazz.equals(Map.class)) {
            return false;
        }

        if (type instanceof Class) {
            return true;
        }

        Type keyType = ((ParameterizedType) type).getActualTypeArguments()[0];
        Type valueType = ((ParameterizedType) type).getActualTypeArguments()[1];
        return isStringOrObjectType(keyType) && isStringOrObjectType(valueType);
    }

    private boolean isStringOrObjectType(Type type) {
        return type.equals(String.class) || type.equals(Object.class);
    }

    private void checkQMapConfig(QMapConfig annotation, Action action) {
        boolean keyExist = !Strings.isNullOrEmpty(annotation.key());
        boolean defaultValueExist = !Strings.isNullOrEmpty(annotation.defaultValue());
        boolean translatorExist = !TrivialMapTranslator.class.equals(annotation.translator());
        if (translatorExist) {
            Preconditions.checkState(!keyExist && !defaultValueExist, "qconfig annotation conflict, translator can not exist with key or default value, class %s, %s %s", action.getClazz().getName(), action.getType(), action.getName());
        } else if (defaultValueExist) {
            Preconditions.checkState(keyExist, "qconfig annotation illegal, default value must exist with key, class %s, %s %s", action.getClazz().getName(), action.getType(), action.getName());
        }

    }
}
