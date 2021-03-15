package cn.whiteg.moesign.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class LotsValue extends ValueProvider {
    final ValueProvider[] list;

    public LotsValue(ConfigurationSection cs) {
        List<?> l = cs.getList("list");
        if (l == null){
            list = null;
        } else {
            list = new ValueProvider[l.size()];
            for (int i = 0; i < l.size(); i++) {
                list[i] = ValueProvider.prase(l.get(i));
            }
        }
    }

    public LotsValue(Collection<?> collection) {
        if (collection == null){
            list = null;
        } else {
            list = new ValueProvider[collection.size()];
            int i = 0;
            for (Object obj : collection) {
                list[i] = ValueProvider.prase(obj);
                i++;
            }
        }

    }

    @Override
    public double getValue(Random random) {
        if (list == null) return 0F;
        return list[random.nextInt(list.length)].getValue(random);
    }
}
