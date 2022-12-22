package com.zhekasmirnov.innercore.api.runtime.saver.world;

import com.zhekasmirnov.innercore.api.runtime.saver.serializer.ScriptableSerializer;
import java.io.IOException;
import org.mozilla.javascript.Scriptable;

public abstract class ScriptableSaverScope implements WorldDataScopeRegistry.SaverScope {
    public abstract void read(Object obj);

    public abstract Object save();

    @Override
    public void readJson(Object json) throws Exception {
        Object scriptable = ScriptableSerializer.scriptableFromJson(json);
        if (scriptable instanceof Scriptable) {
            read((Scriptable) scriptable);
            return;
        }
        throw new IOException("scriptable saver scope readJson() de-serialized into non scriptable");
    }

    @Override
    public Object saveAsJson() throws Exception {
        return ScriptableSerializer.scriptableToJson(save(), C$$Lambda$ScriptableSaverScope$SKsTsK8MFXdC42bmQt3ZeMrD8mA.INSTANCE);
    }
}
