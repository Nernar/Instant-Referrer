package com.zhekasmirnov.innercore.api.mod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.annotations.JSFunction;

public class TagRegistry {
    private static final HashMap<String, TagGroup> groups = new HashMap<>();

    public interface TagFactory {
        void addTags(Object obj, Collection<String> collection);
    }

    public interface TagPredicate {
        boolean check(Object obj, Collection<String> collection);
    }

    public static class TagGroup {
        public final String name;
        private final HashMap<String, HashSet<String>> primaryTagMap = new HashMap<>();
        private final List<TagFactory> tagFactories = new ArrayList<>();
        private boolean isCommonObjectsCollectionDirty = false;
        private final HashMap<Object, HashSet<String>> commonObjectCollection = new HashMap<>();

        public TagGroup(String name) {
            this.name = name;
        }

        public void addTagFactory(TagFactory factory) {
            if (factory != null) {
                this.tagFactories.add(factory);
                this.isCommonObjectsCollectionDirty = true;
            }
        }

        public void addTagsFor(Object obj, String... tags) {
            String key;
            HashMap<Object, HashSet<String>> hashMap = this.commonObjectCollection;
            synchronized (hashMap) {
                if (obj == null) {
                    key = null;
                } else {
                    key = obj.toString();
                }
                HashSet<String> tagSet = this.primaryTagMap.get(key);
                if (tagSet == null) {
                    tagSet = new HashSet<>();
                    this.primaryTagMap.put(key, tagSet);
                }
                for (String tag : tags) {
                    tagSet.add(tag);
                }
                this.isCommonObjectsCollectionDirty = true;
            }
        }

        public void removeTagsFor(Object obj, String... tags) {
            String key;
            HashMap<Object, HashSet<String>> hashMap = this.commonObjectCollection;
            synchronized (hashMap) {
                if (obj == null) {
                    key = null;
                } else {
                    key = obj.toString();
                }
                HashSet<String> tagSet = this.primaryTagMap.get(key);
                if (tagSet != null) {
                    for (String tag : tags) {
                        tagSet.remove(tag);
                    }
                }
                this.isCommonObjectsCollectionDirty = true;
            }
        }

        public void addCommonObject(Object obj, String... tags) {
            addTagsFor(obj, tags);
            synchronized (this.commonObjectCollection) {
                this.commonObjectCollection.put(obj, getTags(obj));
            }
        }

        public void removeCommonObject(Object obj) {
            synchronized (this.commonObjectCollection) {
                this.commonObjectCollection.remove(obj);
            }
        }

        public void addTags(Object obj, Collection<String> tags) {
            ScriptableObjectWrapper tagsArr;
            HashSet<String> primaryTags = this.primaryTagMap.get(obj != null ? obj.toString() : null);
            if (primaryTags != null) {
                tags.addAll(primaryTags);
            }
            if (obj instanceof Scriptable) {
                ScriptableObjectWrapper wrapper = new ScriptableObjectWrapper((Scriptable) obj);
                if (wrapper.has("_tags") && (tagsArr = wrapper.getScriptableWrapper("_tags")) != null) {
                    for (Object tag : tagsArr.asArray()) {
                        if (tag != null) {
                            tags.add(tag.toString());
                        }
                    }
                }
            }
            for (TagFactory factory : this.tagFactories) {
                factory.addTags(obj, tags);
            }
        }

        public HashSet<String> getTags(Object obj) {
            HashSet<String> tags = new HashSet<>();
            addTags(obj, tags);
            return tags;
        }

        public List<Object> getAllWhere(TagPredicate predicate) {
            List<Object> result = new ArrayList<>();
            synchronized (this.commonObjectCollection) {
                boolean isDirty = this.isCommonObjectsCollectionDirty;
                this.isCommonObjectsCollectionDirty = false;
                for (Map.Entry<Object, HashSet<String>> entry : this.commonObjectCollection.entrySet()) {
                    if (isDirty) {
                        entry.setValue(getTags(entry.getKey()));
                    }
                    if (predicate.check(entry.getKey(), entry.getValue())) {
                        result.add(entry.getKey());
                    }
                }
                return result;
            }
        }

        public List<Object> getAllWithTags(final Collection<String> checkTags) {
            return getAllWhere(new TagPredicate() {
                @Override
                public boolean check(Object obj, Collection<String> tags) {
                    for (String tag : checkTags) {
                        if (!tags.contains(tag)) {
                            return false;
                        }
                    }
                    return true;
                }
            });
        }

        public List<Object> getAllWithTag(final String tag) {
            return getAllWhere(new TagPredicate() {
                @Override
                public boolean check(Object obj, Collection<String> tags) {
                    return tags.contains(tag);
                }
            });
        }
    }

    @JSFunction
    public static TagGroup getOrCreateGroup(String name) {
        if (groups.containsKey(name)) {
            return groups.get(name);
        }
        TagGroup group = new TagGroup(name);
        groups.put(name, group);
        return group;
    }
}
