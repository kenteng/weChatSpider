package com.ifeng.weChatSpider.Mongo;

import org.bson.Document;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

/**
 * Created by zhanglr on 2016/2/24.
 */

/**
 * 实体序列化成Bson
 */
public abstract class EntyCodec implements IDecode,IEncode {
    /**
     * 将Bson转换成实体类
     * @param loader
     */
    @Override
    public abstract void decode(DataLoader loader);

    /**
     * 将实体转换成Bson
     * @param <T>
     * @return
     */
    @Override
    public <T> Document encode(){
        return encode(this);
    }

    private <T> Document encode(T t){
        Document en = new Document();
        try {
            Field[] fields = t.getClass().getDeclaredFields();
            AccessibleObject.setAccessible(fields, true);
            if (fields != null){
                for (Field field : fields) {

                    if (EntyCodec.class.isAssignableFrom(field.getType())
                            && EntyCodec.class.isAssignableFrom(field.getType())){
                        Document res = encode(field.get(t));
                        en.put(field.getName(), res);
                    }
                    else {
                        en.put(field.getName(), field.get(t));
                    }


                }
            }
        } catch (Exception e) {
        }

        return en;
    }
}
