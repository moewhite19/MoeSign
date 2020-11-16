package cn.whiteg.moesign;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class Setting {
    private final static int CONFIGVER = 1;
    private static FileConfiguration storage;
    private final MoeSign plugin;
    public boolean DEBUG;
    public String prefix; //前缀
    public int maxMoney; //最大游戏币
    public int minMoney; //最小游戏币
    public String seed;

    public Setting(MoeSign plugin) {
        this.plugin = plugin;
        reload();
    }


    public void reload() {
        File file = new File(MoeSign.plugin.getDataFolder(),"config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        //自动更新配置文件
        if (config.getInt("ver") < CONFIGVER){
            plugin.saveResource("config.yml",true);
            config.set("ver",CONFIGVER);
            final FileConfiguration newcon = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = newcon.getKeys(true);
            for (String k : keys) {
                if (config.isSet(k)) continue;
                config.set(k,newcon.get(k));
                plugin.logger.info("新增配置节点: " + k);
            }
            try{
                config.save(file);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        DEBUG = config.getBoolean("debug");
        prefix = config.getString("prefix");
        maxMoney = config.getInt("maxMoney");
        minMoney = config.getInt("minMoney");
        seed = config.getString("seed");

        //如果最大小于最小,互相交换值
        if (maxMoney < minMoney){
            int max = minMoney;
            minMoney = maxMoney;
            maxMoney = max;
        }
        file = new File(file.getParentFile(),"storage.yml");
        if (file.exists()){
            storage = YamlConfiguration.loadConfiguration(file);
        } else {
            storage = new YamlConfiguration();
        }
    }

    public void saveStorage() {
        File file = new File(plugin.getDataFolder(),"storage.yml");
        try{
            storage.save(file);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public FileConfiguration getStorage() {
        return storage;
    }
}
