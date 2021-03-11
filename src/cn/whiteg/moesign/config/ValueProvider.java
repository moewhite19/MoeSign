package cn.whiteg.moesign.config;

import cn.whiteg.moesign.MoeSign;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class ValueProvider {
    public static Map<String, Constructor<? extends ValueProvider>> types = new HashMap<>();
    private static FixedValue ZERO;

    static {
        registerType(RandomValue.class); //范围随机数
        registerType(LotsValue.class); //抽签
    }

    public static ValueProvider fromOf(Object o) {
        if (o instanceof ConfigurationSection){
            ConfigurationSection cs = (ConfigurationSection) o;
            String type = cs.getString("type");
            if (type == null){
                throw new IllegalStateException("无效配置" + cs.getName());
            }
            Constructor<? extends ValueProvider> con = types.get(type);
            if (con == null) throw new IllegalStateException("未知type: " + type);
            try{
                return con.newInstance(cs);
            }catch (InstantiationException | IllegalAccessException | InvocationTargetException e){
                e.printStackTrace();
                return getZero();
            }
        } else if (o instanceof Number){
            return new FixedValue(((Number) o).doubleValue());
        } else if (o instanceof String){
            return new FixedValue(Double.parseDouble((String) o));
        }
        return getZero();
    }

    public static void registerType(Class<? extends ValueProvider> clazz) {
        try{
            Constructor<? extends ValueProvider> con = clazz.getConstructor(ConfigurationSection.class);
            String type = clazz.getSimpleName();
            int i = type.indexOf("Value");
            if (i > 0) type = type.substring(0,i);
            types.put(type.toLowerCase(),con);
        }catch (Throwable e){
            MoeSign.plugin.logger.warning("无法注册type类型" + clazz.getSimpleName());
        }
    }

    public static ValueProvider getZero() {
        if (ZERO == null) ZERO = new FixedValue(0);
        return ZERO;
    }

    public abstract double getValue(Random random);
}
