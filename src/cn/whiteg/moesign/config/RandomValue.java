package cn.whiteg.moesign.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Random;

public class RandomValue extends ValueProvider {
    final ValueProvider max;
    final ValueProvider min;

    public RandomValue(ConfigurationSection cs) {
        max = ValueProvider.prase(cs.get("max"));
        min = ValueProvider.prase(cs.get("min"));
    }

    @Override
    public double getValue(Random random) {
        int ma = (int) max.getValue(random);
        int mi = (int) min.getValue(random);
        if (ma > mi) return random.nextInt(ma + Math.abs(mi)) + mi;
        else return random.nextInt(mi + Math.abs(ma)) + ma;
    }
}
