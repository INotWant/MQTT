package mqtt.tool;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author iwant
 * @date 19-5-15 16:04
 * @desc conf 帮助类
 */
public final class ConfUtil {

    private static FileInputStream in;
    private static Properties properties = new Properties();

    /**
     * 使用前 open 配置文件
     */
    public static void open(String fileName) {
        try {
            in = new FileInputStream(fileName);
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * properties 接口
     */
    public static Properties getProperties() {
        return properties;
    }

    /**
     * 使用反射对 配置类 中相关静态变量进行修改。
     * 要求：相关静态变量是 public，并且有同名的方法用作修改该变量的值。
     * 注：支持的变量类型 [int short int long float double boolean String]
     *
     * @param clazz 相应的配置文件类
     */
    @SuppressWarnings("unchecked")
    public static void setting(Class clazz) {
        Enumeration<?> enumeration = properties.propertyNames();
        try {
            while (enumeration.hasMoreElements()) {
                String strKey = (String) enumeration.nextElement();
                String strValue = properties.getProperty(strKey);

                Field field = clazz.getDeclaredField(strKey);
                Class<?> type = field.getType();
                Method method = clazz.getDeclaredMethod(strKey, type);
                if ("byte".equals(type.getName()))
                    method.invoke(null, Byte.parseByte(strValue));
                else if ("short".equals(type.getName()))
                    method.invoke(null, Short.parseShort(strValue));
                else if ("int".equals(type.getName()))
                    method.invoke(null, Integer.parseInt(strValue));
                else if ("long".equals(type.getName()))
                    method.invoke(null, Long.parseLong(strValue));
                else if ("float".equals(type.getName()))
                    method.invoke(null, Float.parseFloat(strValue));
                else if ("double".equals(type.getName()))
                    method.invoke(null, Double.parseDouble(strValue));
                else if ("boolean".equals(type.getName()))
                    method.invoke(null, Boolean.parseBoolean(strValue));
                else if ("java.lang.String".equals(type.getName()))
                    method.invoke(null, strValue);
                else
                    throw new IllegalStateException("不支持的类型");
            }
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException |
                InvocationTargetException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用后关闭
     */
    public static void close() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
