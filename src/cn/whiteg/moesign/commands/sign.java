package cn.whiteg.moesign.commands;

import cn.whiteg.mmocore.DataCon;
import cn.whiteg.mmocore.MMOCore;
import cn.whiteg.moeEco.VaultHandler;
import cn.whiteg.moesign.CommandInterface;
import cn.whiteg.moesign.Setting;
import cn.whiteg.moesign.utils.StringUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static cn.whiteg.moesign.MoeSign.plugin;

public class sign extends CommandInterface {

    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    //    private final static DecimalFormat decimalFormat = new DecimalFormat("####,####,####,####.##"); //数字输出格式
    private final static String zeroDate = dateFormat.format(new Date(0));

    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {
        String code; //财富密码
        if (args.length > 0){
            code = StringUtils.join(args," ");
            //去除回车
            code = code.replace('\n',' ');
            //限制长度
            if (code.length() > 256){
                code = code.substring(0,256);
            }
        } else {
            code = "";
        }
        DataCon dc = MMOCore.getPlayerData(sender);
        if (dc == null) return false;
        String nowDate = getNowDate();
        String userDate;
        ConfigurationSection data = dc.getConfig().getConfigurationSection(plugin.getName());
        if (data == null) data = dc.createSection(plugin.getName());
        userDate = data.getString("date",zeroDate);
        Setting setting = plugin.setting;
        if (nowDate.equals(userDate)){
            sender.sendMessage(setting.prefix + "阁下今天已经签到过啦");
        } else {
            Economy economy = plugin.getEconomy();
            Random random;

            if (code.isEmpty()){ //未使用财富密码，直接生成随机数
                random = new Random();
            } else {
                //根据设置的种子，时间代码，和财富密码生成随机数
                random = new Random(setting.seed.hashCode() ^ nowDate.hashCode() ^ code.hashCode());
            }

            int money = (int) setting.money.getValue(random);
            data.set("date",nowDate);

            boolean win = money > 0;
            EconomyResponse response;

            //引用MoeEco
            if (economy instanceof VaultHandler){
                VaultHandler moeEco = (VaultHandler) economy;
                if (win){
                    response = moeEco.depositPlayer(dc,money);
                } else {
                    response = moeEco.withdrawPlayer(dc,Math.abs(money));
                    //如果玩家账户余额不够扣怎么办呢...
                    if (response.type != EconomyResponse.ResponseType.SUCCESS){
                        //扣除全部吧!
                        response = moeEco.withdrawPlayer(dc,moeEco.getBalance(dc));
                    }
                }
                sender.sendMessage(setting.prefix + code + (code.isEmpty() ? "" : ",") + "签到成功." + (win ? "获得" : "丢失") + "§f" + moeEco.getDecimalFormat().format(response.amount) + "!");
            } else {
                //其他Vault经济插件
                if (win){
                    response = economy.depositPlayer(dc.getName(),money);
                } else {
                    response = economy.withdrawPlayer(dc.getName(),Math.abs(money));
                    //如果玩家账户余额不够扣怎么办呢...
                    if (response.type != EconomyResponse.ResponseType.SUCCESS){
                        //扣除全部吧!
                        response = economy.withdrawPlayer(dc.getName(),economy.getBalance(dc.getName()));
                    }
                }
                sender.sendMessage(setting.prefix + code + (code.isEmpty() ? "" : ",") + "签到成功." + (win ? "获得" : "丢失") + "§f" + (response.amount) + "!");
                sender.sendMessage("未适配经济插件" + economy.getName());
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }

    public static String getNowDate() {
        return dateFormat.format(new Date());
    }
}
