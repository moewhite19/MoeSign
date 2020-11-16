package cn.whiteg.moesign.commands;

import cn.whiteg.mmocore.DataCon;
import cn.whiteg.mmocore.MMOCore;
import cn.whiteg.moesign.CommandInterface;
import cn.whiteg.moesign.MoeSign;
import cn.whiteg.moesign.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class test extends CommandInterface {

    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {
        if (!sender.hasPermission("whiteg.test")){
            sender.sendMessage("§b权限不足");
            return true;
        }
        DataCon dc = MMOCore.getPlayerData(sender);
        if (dc == null) return false;
        dc.set(MoeSign.plugin.getName() + ".date",null);
        if (args.length > 0){
            Bukkit.dispatchCommand(sender,"sign " + StringUtils.join(args," "));
        } else {
            Bukkit.dispatchCommand(sender,"sign");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }
}
