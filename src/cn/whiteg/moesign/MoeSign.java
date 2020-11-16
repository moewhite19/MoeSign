package cn.whiteg.moesign;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;


public class MoeSign extends PluginBase {
    public static MoeSign plugin;
    public Logger logger;
    public CommandManage mainCommand;
    public Setting setting;
    private Economy economy;

    public MoeSign() {
        plugin = this;
        logger = getLogger();
    }

    public void onLoad() {
        saveDefaultConfig();
    }

    public void onEnable() {
        logger.info("开始加载插件");
        setting = new Setting(plugin);
        if (setting.DEBUG) logger.info("§a调试模式已开启");
        PluginCommand pc = getCommand(getName().toLowerCase());
        if (pc != null){
            mainCommand = new CommandManage(this);
            pc.setExecutor(mainCommand);
            pc.setTabCompleter(mainCommand);
        } else {
            logger.info("没用注册指令(忘记添加指令到plugin.yml啦?)");
        }
        logger.info("全部加载完成");
        Bukkit.getScheduler().runTask(this,() -> {
            if (Bukkit.getPluginManager().getPlugin("Vault") != null){
                RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
                if (economyProvider != null){
                    this.economy = economyProvider.getProvider();
                }
            }
        });
    }

    public void onDisable() {
        //注销所有监听器
        unregListener();
        logger.info("插件已关闭");
    }

    public void onReload() {
        logger.info("--开始重载--");
        setting.reload();
        logger.info("--重载完成--");
    }

    public Economy getEconomy() {
        return economy;
    }
}
