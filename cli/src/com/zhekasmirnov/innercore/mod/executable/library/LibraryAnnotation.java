package com.zhekasmirnov.innercore.mod.executable.library;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class LibraryAnnotation {
    @SuppressWarnings("unused")
    private static final String NAME_ID = "$_annotation";
    private final String name;
    private final Class<?>[] parameterTypes;

    public LibraryAnnotation(String name, Class<?>[] clsArr) {
        this.name = name;
        this.parameterTypes = clsArr;
    }

    public LibraryAnnotation(String name) {
        this(name, new Class[0]);
    }

    public String getName() {
        return this.name;
    }

    public static class AnnotationInstance {
        private final Object[] params;
        private final LibraryAnnotation parent;

        private AnnotationInstance(LibraryAnnotation parent, Object[] params) {
            this.parent = parent;
            this.params = params;
        }

        AnnotationInstance(LibraryAnnotation libraryAnnotation, Object[] objArr, AnnotationInstance annotationInstance) {
            this(libraryAnnotation, objArr);
        }

        public Object[] getParams() {
            return this.params;
        }

        @SuppressWarnings("unchecked")
        public <T> T getParameter(int id, Class<? extends T> type) {
            return (T) this.params[id];
        }
    }

    public static class AnnotationSet {
        private final HashSet<AnnotationInstance> instances;
        private final Object target;

        public AnnotationSet(Object target) {
            this.instances = new HashSet<>();
            this.target = target;
        }

        public AnnotationSet(Object target, ArrayList<AnnotationInstance> arr) {
            this(target);
            this.instances.addAll(arr);
        }

        public Object getTarget() {
            return this.target;
        }

        public ArrayList<AnnotationInstance> findAll(String name) {
            ArrayList<AnnotationInstance> found = new ArrayList<>();
            Iterator<AnnotationInstance> it = this.instances.iterator();
            while (it.hasNext()) {
                AnnotationInstance instance = it.next();
                if (name.equals(instance.parent.getName())) {
                    found.add(instance);
                }
            }
            return found;
        }

        public AnnotationInstance find(String name) {
            ArrayList<AnnotationInstance> found = findAll(name);
            if (found.size() > 0) {
                return found.get(0);
            }
            return null;
        }
    }

    public void injectMethod(final Scriptable scope) {
        scope.put(this.name, scope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable parent, Object[] parameters) {
                LibraryAnnotation.this.checkParameters(parameters);
                String str = "$_annotation" + LibraryAnnotation.this.name;
                while (true) {
                    String key = str;
                    if (scope.has(key, scope)) {
                        str = "$" + key;
                    } else {
                        ICLog.d("LIBRARY", "annotation injected " + key);
                        scope.put(key, scope, new AnnotationInstance(LibraryAnnotation.this, parameters, null));
                        return null;
                    }
                }
            }
        });
    }

    private static String objectToTypeName(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.getClass().getSimpleName();
    }

    private void reportInvalidParameters(Object[] parameters) {
        StringBuilder message = new StringBuilder();
        message.append(this.name);
        message.append(" got invalid parameters: required (");
        for (Class<?> type : this.parameterTypes) {
            message.append(type);
            message.append(", ");
        }
        message.append(") got (");
        for (Object param : parameters) {
            message.append(objectToTypeName(param));
            message.append(", ");
        }
        message.append(")");
        throw new IllegalArgumentException(message.toString());
    }

    public void checkParameters(Object[] parameters) {
        if (parameters.length != this.parameterTypes.length) {
            reportInvalidParameters(parameters);
        }
        for (int i = 0; i < parameters.length; i++) {
            if (!this.parameterTypes[i].isInstance(parameters[i])) {
                reportInvalidParameters(parameters);
            }
        }
    }

    public static ArrayList<AnnotationSet> getAllAnnotations(Scriptable scope) {
        Object obj;
        ArrayList<AnnotationSet> allAnnotations = new ArrayList<>();
        Object[] ids = scope.getIds();
        ArrayList<AnnotationInstance> annotations = new ArrayList<>();
        for (Object id : ids) {
            if (id instanceof String) {
                String key = (String) id;
                if (key.contains("$_annotation")) {
                    annotations.add((AnnotationInstance) scope.get(key, scope));
                }
            }
            if (id instanceof String) {
                obj = scope.get((String) id, scope);
            } else {
                obj = scope.get(((Integer) id).intValue(), scope);
            }
            Object obj2 = obj;
            allAnnotations.add(new AnnotationSet(obj2, annotations));
            annotations.clear();
        }
        return allAnnotations;
    }
}
