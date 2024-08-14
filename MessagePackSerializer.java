import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
            IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
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
            Object value = deserializeValue(field.getType(), unpacker, getFieldType(field));
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
        } else if (value instanceof List) {
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

    private static Object deserializeValue(Class<?> type, MessageUnpacker unpacker, Class<?> itemType)
            throws IOException,
            IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
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
            List<Object> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add(deserializeValue(itemType, unpacker, itemType));
            }
            return list;
        } else if (Map.class.isAssignableFrom(type)) {
            int size = unpacker.unpackMapHeader();
            Map<Object, Object> map = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                Object key = deserializeValue(String.class, unpacker, String.class); // 假设Key为String类型
                Object value = deserializeValue(itemType, unpacker, itemType);
                map.put(key, value);
            }
            return map;
        } else {
            return deserializeObject(type, unpacker);
        }
    }

    private static Class<?> getFieldType(Field field) {
        if (List.class.isAssignableFrom(field.getType())) {
            return (Class<?>) ((java.lang.reflect.ParameterizedType) field.getGenericType())
                    .getActualTypeArguments()[0];
        }
        return field.getType();
    }
}
