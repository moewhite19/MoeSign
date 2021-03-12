package cn.whiteg.moesign.commands;

import cn.whiteg.mmocore.common.HasCommandInterface;
import cn.whiteg.moesign.Setting;
import cn.whiteg.moesign.utils.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Random;

import static cn.whiteg.moesign.MoeSign.plugin;

public class show extends HasCommandInterface {
    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {
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
        String nowDate = sign.getNowDate();
        Setting setting = plugin.setting;
        Random random;
        if (code.isEmpty()){ //未使用财富密码，直接生成随机数
            random = new Random();
        } else {
            //根据设置的种子，时间代码，和财富密码生成随机数
            random = new Random(setting.seed.hashCode() ^ nowDate.hashCode() ^ code.hashCode());
        }
        int money = (int) setting.money.getValue(random);
        sender.sendMessage(setting.prefix + code + (code.isEmpty() ? "" : ",") + "财富值为: " + "§f" + money);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("whiteg.test");
    }
}
