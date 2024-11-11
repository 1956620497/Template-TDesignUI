package c.e.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Consumer;

//手动实现BeanUtils.copyProperties();
//使用反射机制
public interface BaseData {

    //设置将该方法可以以lambda的方式进行属性设置
    default <V> V asViewObject(Class<V> clazz, Consumer<V> consumer){
        V v = this.asViewObject(clazz);
        consumer.accept(v);
        return v;
    }

    //这里需要外面传进来一个需要转换的类型
    default <V> V asViewObject(Class<V> clazz){
        //有了提供的类型，要把类转换成需要的类型
        try{
            //取出需要转换的类所有的字段
            Field[] declaredFields = clazz.getDeclaredFields();
            //获取该类的构造器,一般实体类都是无参构造的,直接默认获取该无参构造
            Constructor<V> constructor = clazz.getConstructor();
            //通过构造器将指定类型的vo对象构造出来
            V v = constructor.newInstance();
            //当前对象的字段一个一个读取出来，将当前对象同名的对应字段一个一个赋值进去
            for (Field declaredField : declaredFields)
                convert(declaredField,v);
            //转换完成直接返回
            return v;
        }catch (ReflectiveOperationException e){
            throw new RuntimeException(e.getMessage());
        }

    }

    /**
     * 转换对应的字段和相应的vo
     * @param field   对应的字段
     * @param vo   转换进去的vo
     */
    private void convert(Field field,Object vo){
        try{
            //获取当前类的对应字段      获取给进来的字段名字，给进来什么字段名就获取当前字段下的对应的名字
            Field source = this.getClass().getDeclaredField(field.getName());
            //让他们两个字段能够访问
            field.setAccessible(true);
            source.setAccessible(true);
            //将vo对象中对应的属性赋值给field对应的属性里面
            field.set(vo,source.get(this));

        }catch (IllegalAccessException | NoSuchFieldException ignored){

        }
    }

}
