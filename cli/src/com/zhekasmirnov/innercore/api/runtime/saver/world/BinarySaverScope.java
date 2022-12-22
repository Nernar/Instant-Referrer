package com.zhekasmirnov.innercore.api.runtime.saver.world;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import org.json.JSONObject;

public abstract class BinarySaverScope implements WorldDataScopeRegistry.SaverScope {
    public abstract void read(InputStream inputStream);

    public abstract void save(OutputStream outputStream);

    @Override
    public void readJson(Object json) throws Exception {
        String data = ((JSONObject) json).optString("data");
        if (data == null) {
            throw new IOException("missing binary data for BinarySaverScope");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
        try {
            read(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (inputStream != null) {
                    if (th != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable th3) {
                        }
                    }
                    inputStream.close();
                }
                throw th2;
            }
        }
    }

    @Override
    public Object saveAsJson() throws Exception {
        JSONObject json = new JSONObject();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            save(outputStream);
            json.put("data", Base64.getEncoder().encodeToString(outputStream.toByteArray()));
            if (outputStream != null) {
                outputStream.close();
            }
            return json;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (outputStream != null && th != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable th3) {
                    }
                    outputStream.close();
                }
                throw th2;
            }
        }
    }
}
