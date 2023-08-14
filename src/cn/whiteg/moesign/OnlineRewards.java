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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class OnlineRewards implements Listener, Runnable {
    private final MoeSign plugin;
    private final int interval;
    final ValueProvider money;
    private BukkitTask task;
    private Map<UUID, Status> statusMap = Collections.synchronizedMap(new HashMap<>());
    private static Random random = new Random();
    AfkTimer afkTimer;
    long later = System.currentTimeMillis();


    public OnlineRewards(MoeSign plugin,int interval,ValueProvider money) {
        this.plugin = plugin;
        this.interval = interval * 1000 * 60;
        this.money = money;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,this,20L,20L);
        plugin.mainCommand.registerCommand(new CommandInter());
        if (Bukkit.getPluginManager().isPluginEnabled("MoeAfk")){
            afkTimer = MoeAfk.plugin.afkTimer;
        }
        plugin.regListener(this);
        final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (!players.isEmpty()){
            for (Player player : players) {
                statusMap.put(player.getUniqueId(),new Status(player));
            }
        }
    }

    @Override
    public void run() {
        final long now = System.currentTimeMillis();
        final long time = now - later;
        statusMap.forEach((uuid,status) -> {
            status.task(now,time);
        });
        later = now; //记录上次的时间
    }

    public void stop() {
        if (task == null){
            return;
        }
        task.cancel();
        plugin.unregListener(this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        statusMap.put(player.getUniqueId(),new Status(player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        statusMap.remove(player.getUniqueId());
    }

    class CommandInter extends CommandInterface {
        @Override
        public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
            if (sender instanceof Player player){
                if (args.length > 0){
                    return statusMap.get(player.getUniqueId()).onCommand(args[0]);
                } else if (player.hasPermission("whiteg.test")){
                    final Status status = statusMap.get(player.getUniqueId());
                    player.sendMessage("剩余: " + CommonUtils.tanMintoh(status.nextTime - System.currentTimeMillis()));
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
        private final Player player;
        String code = null;
        double value;
        long nextTime;

        public Status(Player player) {
            this.player = player;
            nextTime = System.currentTimeMillis() + interval;
        }

        public void task(long now,long time) {
            //当玩家在挂机时，将nextTime往后退
            if (afkTimer.getAfkStaus(player).isAfkin()){
                nextTime += time;
                return;
            }
            if (now >= nextTime){
                //生成代码和货币数量
                value = money.getValue(random);
                code = StringUtils.generateString(4,random);
                //发送通知
                final TextComponent text = Component.text(plugin.setting.prefix + " §b阁下已达到需求获得在线奖励，").append(Component.text("§b§l[点我领取]")
                        .clickEvent(ClickEvent.runCommand("/moesign tr " + code)));
                player.sendMessage(text);
                nextTime = now + interval;
            }
        }

        public boolean onCommand(String arg) {
            if (arg.equals(code)){
                code = null;
                final Economy economy = plugin.getEconomy();
                if (economy instanceof VaultHandler moeEco){
                    final EconomyResponse response = moeEco.depositPlayer(player,value);
                    if (response.type == EconomyResponse.ResponseType.SUCCESS){
                        player.sendMessage(plugin.setting.prefix + " §b领取成功! 获得§f" + moeEco.getDecimalFormat().format(response.amount));
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


    }
}
