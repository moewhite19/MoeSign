package cn.whiteg.moesign.config;

import java.util.Random;

public class FixedValue extends ValueProvider {
    private final double value;

    public FixedValue(double value) {
        this.value = value;
    }

    @Override
    public double getValue(Random random) {
        return value;
    }
}
