package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import java.util.HashMap;
import org.mozilla.javascript.ScriptableObject;

public class ModDebugInfo {
    private HashMap<String, ExecutableStatus> statusMap = new HashMap<>();

    public static class ExecutableStatus {
        private Throwable compileError;
        private Executable executable;
        private boolean isCompiled;

        public ExecutableStatus(Executable executable) {
            this.isCompiled = false;
            this.isCompiled = true;
            this.executable = executable;
        }

        public ExecutableStatus(Throwable compileError) {
            this.isCompiled = false;
            this.isCompiled = false;
            this.compileError = compileError;
        }

        public Throwable getError() {
            if (this.isCompiled) {
                return this.executable.getLastRunException();
            }
            return this.compileError;
        }

        public String getStatus() {
            if (!this.isCompiled) {
                return "compile error: " + this.compileError;
            }
            if (this.executable.getLastRunException() != null) {
                return "run error: " + this.executable.getLastRunException();
            }
            StringBuilder sb = new StringBuilder();
            sb.append("ok");
            sb.append(this.executable.isLoadedFromDex ? " [bytecode]" : "");
            return sb.toString();
        }

        public String getReport() {
            String str;
            Throwable err = getError();
            StringBuilder sb = new StringBuilder();
            sb.append(getStatus());
            if (err != null) {
                str = "\n" + ICLog.getStackTrace(err);
            } else {
                str = "";
            }
            sb.append(str);
            return sb.toString();
        }

        public ScriptableObject getFont() {
            return ScriptableObjectHelper.createEmpty();
        }
    }

    public HashMap<String, ExecutableStatus> getStatusMap() {
        return this.statusMap;
    }

    public ScriptableObject getFormattedStatusMap() {
        ScriptableObject map = ScriptableObjectHelper.createEmpty();
        for (String name : this.statusMap.keySet()) {
            ExecutableStatus status = this.statusMap.get(name);
            ScriptableObject data = ScriptableObjectHelper.createEmpty();
            data.put("font", data, status.getFont());
            data.put("status", data, status.getStatus());
            data.put("report", data, status.getReport());
            map.put(name, map, data);
        }
        return map;
    }

    public void putStatus(String name, ExecutableStatus status) {
        this.statusMap.put(name, status);
    }

    public void putStatus(String name, Executable status) {
        this.statusMap.put(name, new ExecutableStatus(status));
    }

    public void putStatus(String name, Throwable status) {
        this.statusMap.put(name, new ExecutableStatus(status));
    }
}
