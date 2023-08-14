package cn.whiteg.moesign;

import cn.whiteg.moesign.config.ValueProvider;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class Setting {
    private final static int CONFIGVER = 2;
    private static FileConfiguration storage;
    private final MoeSign plugin;
    public boolean DEBUG;
    public String prefix; //前缀
    public ValueProvider money;
    public String seed;
    public OnlineRewards onlineRewards;

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
        try{
            money = ValueProvider.prase(config.get("money"));
        }catch (Exception e){
            money = ValueProvider.getZero();
        }
        seed = config.getString("seed");

        ConfigurationSection cs;
        if (onlineRewards != null){
            onlineRewards.stop();
            onlineRewards = null;
        }
        cs = config.getConfigurationSection("OnlineRewards");
        if (cs != null && cs.getBoolean("enable")){
            onlineRewards = new OnlineRewards(plugin,cs.getInt("interval"),ValueProvider.prase(cs.get("money")));
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
