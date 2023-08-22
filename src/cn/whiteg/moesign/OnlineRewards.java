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
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OnlineRewards implements Runnable {
    static long TIME_ONE_DAY = 86400000L; //一天的时间
    private int SAV_VER = 1; //文件储存版本号
    private final MoeSign plugin;
    File saveFile;
    private final int interval;
    final ValueProvider moneyProvider;
    private BukkitTask task;
    private final Map<UUID, OnlineTimeStatus> statusMap = Collections.synchronizedMap(new HashMap<>());
    private static final Random random = new Random();
    AfkTimer afkTimer;
    long later = System.currentTimeMillis();
    private int date;


    public OnlineRewards(MoeSign plugin,int interval,ValueProvider money) {
        this.plugin = plugin;
        this.interval = interval * 1000 * 60;
        this.moneyProvider = money;
        date = (int) ((System.currentTimeMillis() + TimeZone.getDefault().getRawOffset()) / TIME_ONE_DAY);
        saveFile = new File(plugin.getDataFolder(),"online_reward.sav");
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,this,20L,20L);
        plugin.mainCommand.registerCommand(new CommandInter());
        if (Bukkit.getPluginManager().isPluginEnabled("MoeAfk")){
            afkTimer = MoeAfk.plugin.afkTimer;
        }
        load();
    }

    //当日期变化时返回true
    public boolean hasDateChange(long now) {
        int nDate = (int) ((now + TimeZone.getDefault().getRawOffset()) / TIME_ONE_DAY);
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
            final OnlineTimeStatus status = statusMap.computeIfAbsent(player.getUniqueId(),uuid -> new OnlineTimeStatus(player));
            status.task(player,now,time);
        }
        later = now; //记录上次的时间
    }

    public void stop() {
        if (task == null){
            return;
        }
        task.cancel();
        save();
    }

    public void load() {
        if (saveFile.exists()){
            byte[] head = plugin.getName().getBytes(StandardCharsets.UTF_8);
            try{
                try (DataInputStream dataIn = new DataInputStream(new FileInputStream(saveFile))){
                    final byte[] rHead = dataIn.readNBytes(head.length);
                    //文件头&版本号&日期都符合才加载
                    if (Arrays.equals(head,rHead) && dataIn.readInt() == SAV_VER && dataIn.readInt() == date){
                        while (dataIn.available() > 0) {
                            String readName = new String(dataIn.readNBytes(dataIn.read()),StandardCharsets.UTF_8);
//                            plugin.getLogger().info("加载名字: " + readName);
                            UUID uuid = new UUID(dataIn.readLong(),dataIn.readLong());
                            OnlineTimeStatus status = new OnlineTimeStatus(uuid,readName);
                            status.mTime = dataIn.readLong();
                            status.dTime = dataIn.readLong();
                            statusMap.put(uuid,status);
                        }
                    }
                }
            }catch (IOException e){
                plugin.getLogger().warning("无法加载文件: " + saveFile);
                e.printStackTrace();
            }
        }
    }

    public void save() {
        if (statusMap.isEmpty()) return; //空的怎么储存?
        try{
            if (!saveFile.exists() && !saveFile.createNewFile()){
                plugin.getLogger().warning("无法创建文件: " + saveFile);
                return;
            }
            byte[] head = plugin.getName().getBytes(StandardCharsets.UTF_8);
            try (DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(saveFile))){
                dataOut.write(head);
                dataOut.writeInt(SAV_VER);
                dataOut.writeInt(date);
                for (Map.Entry<UUID, OnlineTimeStatus> entry : statusMap.entrySet()) {
                    OnlineTimeStatus status = entry.getValue();
                    final byte[] nameArray = status.name.getBytes(StandardCharsets.UTF_8);
                    if (nameArray.length > 255){
                        plugin.getLogger().warning("name bytes length > 512: " + status.name);
                        continue;
                    }
                    dataOut.write(nameArray.length & 0xff);
                    dataOut.write(nameArray);
                    dataOut.writeLong(status.uuid.getMostSignificantBits());
                    dataOut.writeLong(status.uuid.getLeastSignificantBits());
                    dataOut.writeLong(status.mTime);
                    dataOut.writeLong(status.dTime);
                    //todo 暂时只储存在线时长，不管未领取的奖励
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    class CommandInter extends CommandInterface {
        @Override
        public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
            if (sender instanceof Player player){
                if (args.length > 0){
                    return statusMap.get(player.getUniqueId()).onCommand(player,args[0]);
                }
            }
            if (sender.hasPermission("whiteg.test")){
                statusMap.forEach((uuid,status) -> {
                    sender.sendMessage(status.getName() + "§b累积时间: §f" + CommonUtils.tanMintoh(status.mTime) + "§b ,总累积时间: §f" + CommonUtils.tanMintoh(status.dTime));
                    sender.sendMessage("§b当前领取需要时长为: §f" + CommonUtils.tanMintoh(interval));
                });
                return true;
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

    class OnlineTimeStatus {
        private final UUID uuid;
        private final String name;
        String code = null;
        double value;
        long mTime; //领奖累积时间
        long dTime; //总累积时间

        public OnlineTimeStatus(Player player) {
            this.uuid = player.getUniqueId();
            this.name = player.getName();
        }

        public OnlineTimeStatus(UUID uuid,String name) {
            this.uuid = uuid;
            this.name = name;
        }

        public void task(Player player,long now,long time) {
            //当玩家在挂机时，将nextTime往后退
            final AfkTimer.AfkStaus afkStaus = afkTimer.getAfkStaus(player);
            if (afkStaus == null || afkStaus.isAfkin()) return;
            mTime += time;
            dTime += time;
            if (mTime >= interval){
                //重置奖励时间
//                mTime -= interval;
                mTime = mTime % interval;
                //生成代码和货币数量
                value = moneyProvider.getValue(random);
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
