package cn.whiteg.moesign.commands;

import cn.whiteg.mmocore.DataCon;
import cn.whiteg.mmocore.MMOCore;
import cn.whiteg.moeEco.VaultHandler;
import cn.whiteg.moesign.CommandInterface;
import cn.whiteg.moesign.MoeSign;
import cn.whiteg.moesign.Setting;
import cn.whiteg.moesign.utils.StringUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class sign extends CommandInterface {

    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final static DecimalFormat decimalFormat = new DecimalFormat(); //输出格式
    private final static String zeroDate = dateFormat.format(new Date(0));

    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {
        String str;
        if (args.length > 0){
            str = StringUtils.join(args," ");

            //去除回车
            str = str.replace('\n',' ');
            //限制长度
            if (str.length() > 32){
                str = str.substring(0,32);
            }
        } else {
            str = "";
        }
        DataCon dc = MMOCore.getPlayerData(sender);
        if (dc == null) return false;
        String nowDate = getNowDate();
        String userDate;
        ConfigurationSection data = dc.getConfig().getConfigurationSection(MoeSign.plugin.getName());
        if (data == null) data = dc.createSection(MoeSign.plugin.getName());
        userDate = data.getString("date",zeroDate);
        Setting settin = MoeSign.plugin.setting;
        if (nowDate.equals(userDate)){
            sender.sendMessage(settin.prefix + "阁下今天已经签到过啦");
        } else {
            Economy economy = MoeSign.plugin.getEconomy();
            Random random; //待开发种子生成

            if (str.isEmpty()){ //未使用种子
                random = new Random();
            } else {
                random = new Random(settin.seed.hashCode() ^ nowDate.hashCode() ^ str.hashCode());
            }

            int money;
            int min = settin.minMoney;
            money = random.nextInt(settin.maxMoney + Math.abs(min)) + min;
            data.set("date",nowDate);

            //引用MoeEco
            if (economy instanceof VaultHandler){
                VaultHandler moeEco = (VaultHandler) economy;
                EconomyResponse response;


                boolean win = money > 0;

                if (win){
                    response = moeEco.depositPlayer(dc,money);
                } else {
                    response = moeEco.withdrawPlayer(dc,-money);
                    //如果玩家账户余额不够扣怎么办呢...
                    if (response.type != EconomyResponse.ResponseType.SUCCESS){
                        //扣除全部吧!
                        response = moeEco.withdrawPlayer(dc,moeEco.getBalance(dc));
                    }
                }
                sender.sendMessage(settin.prefix + str + (str.isEmpty() ? "" : ",") + "签到成功." + (win ? "获得" : "损失") + decimalFormat.format(response.amount) + "鲸币 !");
            } else {
                sender.sendMessage("未适配经济插件" + economy.getName());
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }

    public String getNowDate() {
        return dateFormat.format(new Date());
    }
}
