package cn.whiteg.moesign;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PluginBase extends JavaPlugin {
    private final Map<String, Listener> listenerMap = new HashMap<>();

    //注册事件
    public void regListener(Listener listener) {
        regListener(listener.getClass().getName(),listener);

    }

    //注册事件
    public void regListener(String key,Listener listener) {
        listenerMap.put(key,listener);
        Bukkit.getPluginManager().registerEvents(listener,this);

    }

    //注销所有注册的事件
    public void unregListener() {
        for (Map.Entry<String, Listener> entry : listenerMap.entrySet()) {
            unregListener(entry.getValue());
        }
        listenerMap.clear();
    }

    //根据key注销事件
    public boolean unregListener(String Key) {
        Listener listenr = listenerMap.remove(Key);
        if (listenr == null){
            return false;
        }
        unregListener(listenr);
        return true;
    }

    public void unregListener(Listener listener) {
        //注销事件
        Class<?> listenerClass = listener.getClass();
        HandlerList.unregisterAll(listener);

        //调用类中的unreg()方法
        try{
            Method unreg = listenerClass.getDeclaredMethod("unreg");
            unreg.setAccessible(true);
            unreg.invoke(listener);
        }catch (IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }catch (NoSuchMethodException ignored){
        }
    }

    public Map<String, Listener> getListenerMap() {
        return listenerMap;
    }

}
