package cn.whiteg.moesign;

import cn.whiteg.mmocore.common.CommandInterface;
import cn.whiteg.mmocore.util.CommonUtils;
import cn.whiteg.moeAfk.AfkTimer;
import cn.whiteg.moeAfk.MoeAfk;
import cn.whiteg.moeEco.VaultHandler;
import cn.whiteg.moesign.config.ValueProvider;
import cn.whiteg.moesign.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class OnlineRewards implements Runnable {
    private final MoeSign plugin;
    private final int interval;
    final ValueProvider money;
    private BukkitTask task;
    private final Map<UUID, Status> statusMap = Collections.synchronizedMap(new HashMap<>());
    private static final Random random = new Random();
    AfkTimer afkTimer;
    long later = System.currentTimeMillis();
    static long oneDay = 86400000L; //一天的时间
    private int date;


    public OnlineRewards(MoeSign plugin,int interval,ValueProvider money) {
        this.plugin = plugin;
        this.interval = interval * 1000 * 60;
        this.money = money;
        date = (int) ((System.currentTimeMillis() + TimeZone.getDefault().getRawOffset()) / oneDay);
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,this,20L,20L);
        plugin.mainCommand.registerCommand(new CommandInter());
        if (Bukkit.getPluginManager().isPluginEnabled("MoeAfk")){
            afkTimer = MoeAfk.plugin.afkTimer;
        }
    }

    //当日期变化时返回true
    public boolean hasDateChange(long now) {
        int nDate = (int) ((now + TimeZone.getDefault().getRawOffset()) / oneDay);
        if (nDate != date){
            date = nDate;
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        final long now = System.currentTimeMillis();
        final long time = now - later;

        if (hasDateChange(now)){
            for (UUID uuid : statusMap.keySet().toArray(new UUID[0])) {
                //跨日期时删除不在线的玩家
                if (Bukkit.getPlayer(uuid) == null){
                    statusMap.remove(uuid);
                }
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            final Status status = statusMap.computeIfAbsent(player.getUniqueId(),uuid -> new Status(player));
            status.task(player,now,time);
        }
        later = now; //记录上次的时间
    }

    public void stop() {
        if (task == null){
            return;
        }
        task.cancel();
    }

    class CommandInter extends CommandInterface {
        @Override
        public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
            if (sender instanceof Player player){
                if (args.length > 0){
                    return statusMap.get(player.getUniqueId()).onCommand(player,args[0]);
                } else if (player.hasPermission("whiteg.test")){
                    statusMap.forEach((uuid,status) -> {
                        player.sendMessage(status.getName() + "§b累积时间: §f" + CommonUtils.tanMintoh(status.mTime) + "§b ,总累积时间: §f" + CommonUtils.tanMintoh(status.dTime));
                    });
                }
            }
            return false;
        }

        @Override
        public String getName() {
            return "tr";
        }

        @Override
        public String getDescription() {
            return "在线奖励";
        }
    }

    class Status {
        private final UUID uuid;
        private final String name;
        String code = null;
        double value;
        long mTime; //领奖累积时间
        long dTime; //总累积时间

        public Status(Player player) {
            this.uuid = player.getUniqueId();
            name = player.getName();
        }

        public void task(Player player,long now,long time) {
            //当玩家在挂机时，将nextTime往后退
            if (afkTimer.getAfkStaus(player).isAfkin()){
                return;
            }
            mTime += time;
            dTime += time;
            if (time >= interval){
                //重置奖励时间
                mTime -= interval;
                //生成代码和货币数量
                value = money.getValue(random);
                if (value < 0) value = 0; //暂时不支持负数
                code = StringUtils.generateString(4,random);
                //发送通知
                final TextComponent text = Component.text(plugin.setting.prefix + " §b阁下已达到需求获得在线奖励，").append(Component.text("§b§l[点我领取]")
                        .clickEvent(ClickEvent.runCommand("/moesign tr " + code)));
                player.sendMessage(text);
            }
        }

        public boolean onCommand(Player player,String arg) {
            if (arg.equals(code)){
                code = null;
                final Economy economy = plugin.getEconomy();
                if (economy instanceof VaultHandler moeEco){
                    final EconomyResponse response = moeEco.depositPlayer(player,value);
                    if (response.type == EconomyResponse.ResponseType.SUCCESS){
                        player.sendMessage(plugin.setting.prefix + " §b领取成功! 获得§f" + moeEco.getDecimalFormat().format(response.amount));
                        Bukkit.getConsoleSender().sendMessage(plugin.setting.prefix +/* code + (code.isEmpty() ? "" : ",") +*/ "§f" + player.getName() + "§b获得§f" + moeEco.getDecimalFormat().format(response.amount) + "!");
                    } else {
                        player.sendMessage(plugin.setting.prefix + " §c领取失败! 原因§f" + response.errorMessage);
                    }
                } else {
                    //todo 先不考虑其他经济插件， 不想重复造轮子
                    player.sendMessage("§c未支持的经济插件§f: " + economy.getName());
                }
                return true;
            } else {
                player.sendMessage(plugin.setting.prefix + " §b阁下已经领取过啦，稍后再领吧!");
            }
            return false;
        }

        public String getName() {
            return name;
        }
    }
}
