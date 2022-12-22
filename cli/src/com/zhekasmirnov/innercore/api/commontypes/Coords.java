package com.zhekasmirnov.innercore.api.commontypes;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.ScriptableObject;

public class Coords extends ScriptableObject {
    @Override
    public String getClassName() {
        return "Coords";
    }

    public Coords(int x, int y, int z, int side, boolean relativeCoordsAreSame) {
        int relX = x;
        int relY = y;
        int relZ = z;
        if (!relativeCoordsAreSame) {
            switch (side) {
                case 0:
                    relY--;
                    break;
                case 1:
                    relY++;
                    break;
                case 2:
                    relZ--;
                    break;
                case 3:
                    relZ++;
                    break;
                case 4:
                    relX--;
                    break;
                case 5:
                    relX++;
                    break;
            }
        }
        put("x", this, Integer.valueOf(x));
        put("y", this, Integer.valueOf(y));
        put("z", this, Integer.valueOf(z));
        put("side", this, Integer.valueOf(side));
        ScriptableObject rel = ScriptableObjectHelper.createEmpty();
        rel.put("x", rel, Integer.valueOf(relX));
        rel.put("y", rel, Integer.valueOf(relY));
        rel.put("z", rel, Integer.valueOf(relZ));
        put("relative", this, rel);
    }

    public Coords(int x, int y, int z, int side) {
        this(x, y, z, side, false);
    }

    public Coords(double x, double y, double z) {
        put("x", this, Double.valueOf(x));
        put("y", this, Double.valueOf(y));
        put("z", this, Double.valueOf(z));
    }

    public Coords(int x, int y, int z) {
        put("x", this, Integer.valueOf(x));
        put("y", this, Integer.valueOf(y));
        put("z", this, Integer.valueOf(z));
    }

    public Coords setSide(int side) {
        put("side", this, Integer.valueOf(side));
        return this;
    }
}
