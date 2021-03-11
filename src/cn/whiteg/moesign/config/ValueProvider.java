package cn.whiteg.moesign.config;

import cn.whiteg.moesign.MoeSign;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class ValueProvider {
    public static final FixedValue ZERO = new FixedValue(0);
    public static Map<String, Constructor<? extends ValueProvider>> types = new HashMap<>();

    static {
        registerType("random",RandomValue.class);
        registerType("lots",LotsValue.class);
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
                return ZERO;
            }
        } else if (o instanceof Number){
            return new FixedValue(((Number) o).doubleValue());
        } else if (o instanceof String){
            return new FixedValue(Double.parseDouble((String) o));
        }
        return ZERO;
    }

    public static void registerType(String type,Class<? extends ValueProvider> clazz) {
        try{
            Constructor<? extends ValueProvider> con = clazz.getConstructor(ConfigurationSection.class);
            types.put(type,con);
        }catch (Throwable e){
            MoeSign.plugin.logger.warning("无法注册type类型" + clazz.getSimpleName());
        }
    }

    public abstract double getValue(Random random);
}
