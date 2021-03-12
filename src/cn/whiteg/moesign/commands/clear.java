package cn.whiteg.moesign.commands;

import cn.whiteg.mmocore.DataCon;
import cn.whiteg.mmocore.MMOCore;
import cn.whiteg.mmocore.common.HasCommandInterface;
import cn.whiteg.moesign.MoeSign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class clear extends HasCommandInterface {

    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {
        DataCon dc = MMOCore.getPlayerData(sender);
        if (dc == null) return false;
        dc.set(MoeSign.plugin.getName() + ".date",null);
        sender.sendMessage("已删除签到标记");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender,Command command,String s,String[] strings) {
        return null;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("whiteg.test");
    }
}
