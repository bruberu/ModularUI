package com.cleanroommc.modularui.utils.math.functions.string;

import com.cleanroommc.modularui.api.IValue;
import com.cleanroommc.modularui.utils.math.functions.SNFunction;

public class StringEndsWith extends SNFunction {

    public StringEndsWith(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 2;
    }

    @Override
    public double doubleValue() {
        return this.getArg(0).stringValue().endsWith(this.getArg(1).stringValue()) ? 1 : 0;
    }
}
