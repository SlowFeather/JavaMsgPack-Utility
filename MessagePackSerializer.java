import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.lang.reflect.Array;

public class MessagePackSerializer {

    public static byte[] serialize(Object obj) throws IOException, IllegalAccessException {
        try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            serializeObject(obj, packer);
            return packer.toByteArray();
        }
    }

    public static <T> T deserialize(byte[] data, Class<T> clazz) throws IOException, IllegalAccessException,
            InstantiationException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data)) {
            return clazz.cast(deserializeObject(clazz, unpacker));
        }
    }

    private static void serializeObject(Object obj, MessageBufferPacker packer)
            throws IOException, IllegalAccessException {
        if (obj == null) {
            packer.packNil();
            return;
        }

        Class<?> clazz = obj.getClass();
        List<Field> allFields = new ArrayList<>();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            allFields.addAll(Arrays.asList(fields));
            clazz = clazz.getSuperclass(); // 处理父类字段
        }

        packer.packArrayHeader(allFields.size());

        for (Field field : allFields) {
            field.setAccessible(true);
            Object value = field.get(obj);
            serializeValue(value, packer);
        }
    }

    private static Object deserializeObject(Class<?> clazz, MessageUnpacker unpacker) throws IOException,
            IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException,
            ClassNotFoundException {
        if (unpacker.tryUnpackNil()) {
            return null;
        }

        Object obj = clazz.getDeclaredConstructor().newInstance();

        int fieldCount = unpacker.unpackArrayHeader();

        List<Field> allFields = new ArrayList<>();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            allFields.addAll(Arrays.asList(fields));
            clazz = clazz.getSuperclass();
        }

        for (int i = 0; i < fieldCount; i++) {
            Field field = allFields.get(i);
            field.setAccessible(true);
            Object value = deserializeValue(field.getType(), unpacker, getFieldGenericType(field));
            field.set(obj, value);
        }

        return obj;
    }

    private static void serializeValue(Object value, MessageBufferPacker packer)
            throws IOException, IllegalAccessException {
        if (value == null) {
            packer.packNil();
        } else if (value instanceof String) {
            packer.packString((String) value);
        } else if (value instanceof Integer) {
            packer.packInt((Integer) value);
        } else if (value instanceof Long) {
            packer.packLong((Long) value);
        } else if (value instanceof Float) {
            packer.packFloat((Float) value);
        } else if (value instanceof Double) {
            packer.packDouble((Double) value);
        } else if (value instanceof Boolean) {
            packer.packBoolean((Boolean) value);
        } 
        else if (value.getClass().isArray()) {
            // 处理数组序列化
            int length = Array.getLength(value);
            packer.packArrayHeader(length);
            for (int i = 0; i < length; i++) {
                serializeValue(Array.get(value, i), packer);
            }
        }
        else if (value instanceof List) {
            List<?> list = (List<?>) value;
            packer.packArrayHeader(list.size());
            for (Object item : list) {
                serializeValue(item, packer);
            }
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            packer.packMapHeader(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                serializeValue(entry.getKey(), packer);
                serializeValue(entry.getValue(), packer);
            }
        } else {
            serializeObject(value, packer);
        }
    }

    private static Object deserializeValue(Class<?> type, MessageUnpacker unpacker, Type genericType)
            throws IOException, IllegalAccessException, InstantiationException, InvocationTargetException,
            NoSuchMethodException, ClassNotFoundException {
        if (unpacker.tryUnpackNil()) {
            return null;
        } else if (type == String.class) {
            return unpacker.unpackString();
        } else if (type == Integer.class || type == int.class) {
            return unpacker.unpackInt();
        } else if (type == Long.class || type == long.class) {
            return unpacker.unpackLong();
        } else if (type == Float.class || type == float.class) {
            return unpacker.unpackFloat();
        } else if (type == Double.class || type == double.class) {
            return unpacker.unpackDouble();
        } else if (type == Boolean.class || type == boolean.class) {
            return unpacker.unpackBoolean();
        }
        else if (type.isArray()) { 
            // 处理数组类型 (例如 int[], String[], User[])
            int length = unpacker.unpackArrayHeader();
            Class<?> componentType = type.getComponentType(); // 获取数组元素类型
            Object array = Array.newInstance(componentType, length); // 创建数组实例
            for (int i = 0; i < length; i++) {
                // 递归反序列化数组元素
                Object val = deserializeValue(componentType, unpacker, componentType);
                Array.set(array, i, val);
            }
            return array;
        }
        else if (List.class.isAssignableFrom(type)) {
            int size = unpacker.unpackArrayHeader();
            List<Object> list = new ArrayList<Object>(size);
            for (int i = 0; i < size; i++) {
                Type listItemType = (genericType instanceof ParameterizedType)
                        ? ((ParameterizedType) genericType).getActualTypeArguments()[0]
                        : Object.class;

                Class<?> listItemClass;
                if (listItemType instanceof Class<?>) {
                    listItemClass = (Class<?>) listItemType;
                } else if (listItemType instanceof ParameterizedType) {
                    listItemClass = (Class<?>) ((ParameterizedType) listItemType).getRawType();
                } else {
                    listItemClass = Object.class;
                }

                list.add(deserializeValue(listItemClass, unpacker, listItemType));
            }
            return list;
        } else if (Map.class.isAssignableFrom(type)) {
            int size = unpacker.unpackMapHeader();
            Map<Object, Object> map = new HashMap<Object, Object>(size);
            Type keyType = (genericType instanceof ParameterizedType)
                    ? ((ParameterizedType) genericType).getActualTypeArguments()[0]
                    : String.class;
            Type valueType = (genericType instanceof ParameterizedType)
                    ? ((ParameterizedType) genericType).getActualTypeArguments()[1]
                    : Object.class;
            for (int i = 0; i < size; i++) {
                Object key = deserializeValue((Class<?>) keyType, unpacker, keyType);
                Object value = deserializeValue((Class<?>) valueType, unpacker, valueType);
                map.put(key, value);
            }
            return map;
        } else {
            return deserializeObject(type, unpacker);
        }
    }

    private static Object deserializeValueOld(Class<?> type, MessageUnpacker unpacker, Type genericType)
            throws IOException, IllegalAccessException, InstantiationException, InvocationTargetException,
            NoSuchMethodException, ClassNotFoundException {
        if (unpacker.tryUnpackNil()) {
            return null;
        } else if (type == String.class) {
            return unpacker.unpackString();
        } else if (type == Integer.class || type == int.class) {
            return unpacker.unpackInt();
        } else if (type == Long.class || type == long.class) {
            return unpacker.unpackLong();
        } else if (type == Float.class || type == float.class) {
            return unpacker.unpackFloat();
        } else if (type == Double.class || type == double.class) {
            return unpacker.unpackDouble();
        } else if (type == Boolean.class || type == boolean.class) {
            return unpacker.unpackBoolean();
        } else if (List.class.isAssignableFrom(type)) {
            int size = unpacker.unpackArrayHeader();
            List<Object> list = new ArrayList<Object>(size);
            for (int i = 0; i < size; i++) {
                Type listItemType = (genericType instanceof ParameterizedType)
                        ? ((ParameterizedType) genericType).getActualTypeArguments()[0]
                        : Object.class;
                list.add(deserializeValue((Class<?>) listItemType, unpacker, listItemType));
            }
            return list;
        } else if (Map.class.isAssignableFrom(type)) {
            int size = unpacker.unpackMapHeader();
            Map<Object, Object> map = new HashMap<Object, Object>(size);
            Type keyType = (genericType instanceof ParameterizedType)
                    ? ((ParameterizedType) genericType).getActualTypeArguments()[0]
                    : String.class;
            Type valueType = (genericType instanceof ParameterizedType)
                    ? ((ParameterizedType) genericType).getActualTypeArguments()[1]
                    : Object.class;
            for (int i = 0; i < size; i++) {
                Object key = deserializeValue((Class<?>) keyType, unpacker, keyType);
                Object value = deserializeValue((Class<?>) valueType, unpacker, valueType);
                map.put(key, value);
            }
            return map;
        } else {
            return deserializeObject(type, unpacker);
        }
    }

    private static Type getFieldGenericType(Field field) {
        return field.getGenericType();
    }
}
