package util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 类型转换的工具类，注意，不能转换内部有其他类引用的对象
 * 
 * @author liuliyong
 *
 */
public class TypeConversionUtil {
	
	
	public static Object mapToJavaObject(Map<String, Object> ori, Class<?> clazz)
			throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		@SuppressWarnings("deprecation")
		Object obj = clazz.newInstance();
		if (ori != null && ori.size() != 0) {
			for (Entry<String, Object> entry : ori.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				String methodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
				Field field = getClassField(clazz, methodName);
				if (field == null) {
					continue;
				}
				Class<?> fieldType = field.getType();
				value = convertValType(value, fieldType);   
                try{  
                    clazz.getMethod(methodName, field.getType()).invoke(obj, value);   
                }catch(NoSuchMethodException e){  
                    e.printStackTrace();  
                } catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
			}
		}
		return obj;
	}

	/**
	 * 得到clazz类中名为fieldName的域对象
	 * 
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	protected static final Field getClassField(Class<?> clazz, String fieldName) {
		// 代表已经是最底层的类
		if (Object.class.getName().equals(clazz.getName())) {
			return null;
		}
		Field[] declaredFields = clazz.getDeclaredFields();
		for (Field f : declaredFields) {
			if (f.getName().equals(fieldName)) {
				return f;
			}
		}
		Class<?> superClass = clazz.getSuperclass();
		if (superClass != null) {
			// 递归检查域是否从超类继承
			return getClassField(superClass, fieldName);
		}
		return null;
	}

	protected static final Object convertValType(Object value, Class<?> fieldTypeClass) {
		Object retVal = null;
		if (Long.class.getName().equals(fieldTypeClass.getName())
				|| long.class.getName().equals(fieldTypeClass.getName())) {
			retVal = Long.parseLong(value.toString());
		} else if (Integer.class.getName().equals(fieldTypeClass.getName())
				|| int.class.getName().equals(fieldTypeClass.getName())) {
			retVal = Integer.parseInt(value.toString());
		} else if (Float.class.getName().equals(fieldTypeClass.getName())
				|| float.class.getName().equals(fieldTypeClass.getName())) {
			retVal = Float.parseFloat(value.toString());
		} else if (Double.class.getName().equals(fieldTypeClass.getName())
				|| double.class.getName().equals(fieldTypeClass.getName())) {
			retVal = Double.parseDouble(value.toString());
		} else {
			retVal = value;
		}
		return retVal;
	}
}
