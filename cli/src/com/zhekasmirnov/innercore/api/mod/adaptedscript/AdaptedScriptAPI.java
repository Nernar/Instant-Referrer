package com.zhekasmirnov.innercore.api.mod.adaptedscript;

import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.Version;
import com.zhekasmirnov.innercore.api.annotations.APIIgnore;
import com.zhekasmirnov.innercore.api.annotations.APIStaticModule;
import com.zhekasmirnov.innercore.api.annotations.DeprecatedAPIMethod;
import com.zhekasmirnov.innercore.api.annotations.Indev;
import com.zhekasmirnov.innercore.api.annotations.Placeholder;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.log.DialogHelper;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.util.DebugAPI;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.api.nbt.NbtDataType;
import com.zhekasmirnov.innercore.api.runtime.LevelInfo;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;
import com.zhekasmirnov.innercore.api.runtime.TickManager;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import com.zhekasmirnov.innercore.api.runtime.other.PrintStacking;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaver;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaverRegistry;
import com.zhekasmirnov.innercore.api.runtime.saver.serializer.ScriptableSerializer;
import com.zhekasmirnov.innercore.api.runtime.saver.world.ScriptableSaverScope;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataScopeRegistry;
import com.zhekasmirnov.innercore.api.unlimited.BlockRegistry;
import com.zhekasmirnov.innercore.api.unlimited.SpecialType;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.innercore.utils.UIUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.json.JSONException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.annotations.JSStaticFunction;

public class AdaptedScriptAPI extends API {

    @APIStaticModule
    public static class CustomEnchant {
    }

    @APIStaticModule
    public static class GenerationUtils {
    }

    @APIStaticModule
    public static class ICRender {
    }

    @APIStaticModule
    public static class ItemModel {
    }

    @APIStaticModule
    public static class Particles {
    }

    @Override
    public String getName() {
        return "AdaptedScript";
    }

    @Override
    public int getLevel() {
        return 1;
    }

    @Override
    public void onLoaded() {
    }

    @Override
    public void onModLoaded(Mod mod) {
    }

    @Override
    public void onCallback(String name, Object[] args) {
    }

    @Override
    public void setupCallbacks(Executable executable) {
    }

    @Override
    public void prepareExecutable(Executable executable) {
        super.prepareExecutable(executable);
    }

    @JSStaticFunction
    public static void preventDefault() {
        throw new UnsupportedOperationException();
    }

    @JSStaticFunction
    public static void log(String str) {
        ICLog.d("MOD", str);
    }

    @JSStaticFunction
    public static void print(String str) {
        ICLog.d("MOD-PRINT", str);
        PrintStacking.print(str);
    }

    @JSStaticFunction
    public static void logDeprecation(String functionName) {
        ICLog.d("WARNING", "using deprecated or unimplemented method " + functionName + "()");
    }

    @JSStaticFunction
    public static void setTile(int x, int y, int z, int id, int data) {
        throw new UnsupportedOperationException();
    }

    @JSStaticFunction
    public static int getTile(int x, int y, int z) {
        throw new UnsupportedOperationException();
    }

    @JSStaticFunction
    public static int getTileAndData(int x, int y, int z) {
        throw new UnsupportedOperationException();
    }

    @JSStaticFunction
    public static long getPlayerEnt() {
        throw new UnsupportedOperationException();
    }

    @JSStaticFunction
    public static void clientMessage(String message) {
        throw new UnsupportedOperationException();
    }

    @JSStaticFunction
    public static void tipMessage(String message) {
        throw new UnsupportedOperationException();
    }

    @JSStaticFunction
    public static void explode(double x, double y, double z, double power, boolean onFire) {
        throw new UnsupportedOperationException();
    }

    @APIStaticModule
    public static class Logger {
        @JSStaticFunction
        public static void Log(String message, String prefix) {
            ICLog.d((prefix == null || prefix.isEmpty()) ? "MOD" : "MOD", message);
        }

        @JSStaticFunction
        public static void LogError(Object error) {
            try {
                Throwable throwable = (Throwable) Context.jsToJava(error, Throwable.class);
                ICLog.e("ERROR", "STACK TRACE:", throwable);
            } catch (Throwable th) {
            }
        }

        @JSStaticFunction
        public static void Flush() {
            ICLog.flush();
        }

        @JSStaticFunction
        public static void debug(String tag, String message) {
            ICLog.d(tag, message);
        }

        @JSStaticFunction
        public static void info(String tag, String message) {
            ICLog.i(tag, message);
        }

        @JSStaticFunction
        public static void error(String tag, String message, Object error) {
            try {
                Throwable throwable = (Throwable) Context.jsToJava(error, Throwable.class);
                ICLog.e(tag, message, throwable);
            } catch (Throwable e) {
                ICLog.e("ERROR", "error occurred while logging mod error (" + error + ", " + error.getClass() + "):", e);
            }
        }
    }

    @APIStaticModule
    public static class Level {
        @JSStaticFunction
        public static void setBlockChangeCallbackEnabled(int id, boolean enabled) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setTile(int x, int y, int z, int id, int data) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getTile(int x, int y, int z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getData(int x, int y, int z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getTileAndData(int x, int y, int z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getBrightness(int x, int y, int z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static boolean isChunkLoaded(int x, int z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static boolean isChunkLoadedAt(int x, int y, int z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getChunkState(int x, int z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getChunkStateAt(int x, int y, int z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getBiome(int x, int z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setBiome(int x, int z, int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getBiomeMap(int x, int z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setBiomeMap(int x, int z, int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static String biomeIdToName(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static float getTemperature(int x, int y, int z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getGrassColor(int x, int z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setGrassColor(int x, int z, int color) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void destroyBlock(int x, int y, int z, boolean drop) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void addParticle(int id, double x, double y, double z, double vx, double vy, double vz, int data) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void addFarParticle(int id, double x, double y, double z, double vx, double vy, double vz, int data) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static long spawnMob(double x, double y, double z, int id, String skin) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void spawnExpOrbs(double x, double y, double z, int amount) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static long dropItem(double x, double y, double z, int placeholder, int id, int count, int data, Object extra) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static Object getTileEntity(int x, int y, int z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static long getTime() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setTime(int time) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getGameMode() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setGameMode(int mode) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getDifficulty() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setDifficulty(int val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getRainLevel() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setRainLevel(double val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getLightningLevel() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setLightningLevel(double val) {
            throw new UnsupportedOperationException();
        }

        @Placeholder
        @JSStaticFunction
        public static void playSound(double x, double y, double z, String name, double f1, double f2) {
            throw new UnsupportedOperationException();
        }

        @Placeholder
        @JSStaticFunction
        public static void playSoundEnt(Object ent, String name, double f1, double f2) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void explode(double x, double y, double z, double power, boolean fire) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setNightMode(boolean val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setRespawnCoords(int x, int y, int z) {
            throw new UnsupportedOperationException();
        }

        @DeprecatedAPIMethod
        @JSStaticFunction
        public static void setSpawn(int x, int y, int z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static long getSeed() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static String getWorldName() {
            return LevelInfo.getLevelName();
        }

        @JSStaticFunction
        public static String getWorldDir() {
            return LevelInfo.getLevelDir();
        }

        @JSStaticFunction
        public static ScriptableObject clip(double r36, double r38, double r40, double r42, double r44, double r46, int r48) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setSkyColor(double r, double g, double b) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void resetSkyColor() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setCloudColor(double r, double g, double b) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void resetCloudColor() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setSunsetColor(double r, double g, double b) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void resetSunsetColor() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setFogColor(double r, double g, double b) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void resetFogColor() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setFogDistance(double start, double end) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void resetFogDistance() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setUnderwaterFogColor(double r, double g, double b) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void resetUnderwaterFogColor() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setUnderwaterFogDistance(double start, double end) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void resetUnderwaterFogDistance() {
            throw new UnsupportedOperationException();
        }
    }

    @APIStaticModule
    public static class Entity {
        static long unwrapEntity(Object ent) {
            return ((Long) (ent instanceof Wrapper ? ((Wrapper) ent).unwrap() : Long.valueOf(((Number) ent).longValue()))).longValue();
        }

        @JSStaticFunction
        public static boolean isValid(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getDimension(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static ArrayList<Long> getAllArrayList() {
            AdaptedScriptAPI.logDeprecation("Entity.getAllArrayList()");
            return new ArrayList<>(NativeCallback.getAllEntities());
        }

        @JSStaticFunction
        public static NativeArray getAll() {
            AdaptedScriptAPI.logDeprecation("Entity.getAll()");
            return new NativeArray(NativeCallback.getAllEntities().toArray());
        }

        @JSStaticFunction
        public static float[] getPosition(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getX(Object entity) {
            return getPosition(entity)[0];
        }

        @JSStaticFunction
        public static double getY(Object entity) {
            return getPosition(entity)[1];
        }

        @JSStaticFunction
        public static double getZ(Object entity) {
            return getPosition(entity)[2];
        }

        @JSStaticFunction
        public static void setPosition(Object entity, double x, double y, double z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setPositionAxis(Object entity, int axis, double val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static float[] getVelocity(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getVelX(Object entity) {
            return getVelocity(entity)[0];
        }

        @JSStaticFunction
        public static double getVelY(Object entity) {
            return getVelocity(entity)[1];
        }

        @JSStaticFunction
        public static double getVelZ(Object entity) {
            return getVelocity(entity)[2];
        }

        @JSStaticFunction
        public static void setVelocity(Object entity, double x, double y, double z) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setVelocityAxis(Object entity, int axis, double val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static float[] getRotation(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getYaw(Object entity) {
            return getRotation(entity)[0];
        }

        @JSStaticFunction
        public static double getPitch(Object entity) {
            return getRotation(entity)[1];
        }

        @JSStaticFunction
        public static void setRot(Object entity, double x, double y) {
            setRotation(entity, x, y);
        }

        @JSStaticFunction
        public static void setRotation(Object entity, double x, double y) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setRotationAxis(Object entity, int axis, double val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getHealth(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setHealth(Object entity, int health) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getMaxHealth(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setMaxHealth(Object entity, int health) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getAnimalAge(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setAnimalAge(Object entity, int age) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getFireTicks(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setFireTicks(Object entity, int ticks, boolean force) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static boolean isImmobile(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setImmobile(Object entity, boolean val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static boolean isSneaking(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setSneaking(Object entity, boolean val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static String getNameTag(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setNameTag(Object entity, String tag) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getRenderType(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setRenderType(Object entity, int type) {
            throw new UnsupportedOperationException();
        }

        @Placeholder
        @JSStaticFunction
        public static String getSkin(Object entity) {
            AdaptedScriptAPI.logDeprecation("Entity.getSkin");
            return "missing_texture.png";
        }

        @JSStaticFunction
        public static void setSkin(Object entity, String skin) {
            throw new UnsupportedOperationException();
        }

        @Placeholder
        @JSStaticFunction
        public static String getMobSkin(Object entity) {
            AdaptedScriptAPI.logDeprecation("Entity.getMobSkin");
            return "missing_texture.png";
        }

        @JSStaticFunction
        public static void setMobSkin(Object entity, String skin) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static ItemInstance getDroppedItem(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static ItemInstance getProjectileItem(Object entity) {
            throw new UnsupportedOperationException();
        }

        @Indev
        @JSStaticFunction
        public static void setDroppedItem(Object entity, int id, int count, int data, Object extra) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static ItemInstance getCarriedItem(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static ItemInstance getOffhandItem(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setCarriedItem(Object entity, int id, int count, int data, Object extra) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setOffhandItem(Object entity, int id, int count, int data, Object extra) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static ItemInstance getArmor(Object entity, int slot) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setArmor(Object entity, int slot, int id, int count, int data, Object extra) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static ItemInstance getArmorSlot(Object entity, int slot) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setArmorSlot(Object entity, int slot, int id, int count, int data, Object extra) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static long getPlayerEnt() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void remove(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void addEffect(Object entity, int effect, int duration, int level, boolean b1, boolean b2, boolean effectAnimation) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static boolean hasEffect(Object entity, int effect) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getEffectLevel(Object entity, int effect) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getEffectDuration(Object entity, int effect) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void removeEffect(Object entity, int effect) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void removeAllEffects(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void rideAnimal(Object entity, Object rider) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static long getRider(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static long getRiding(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static long getTarget(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setTarget(Object entity, Object target) {
            throw new UnsupportedOperationException();
        }

        @DeprecatedAPIMethod
        @JSStaticFunction
        public static int getEntityTypeId(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getType(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static String getTypeName(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static Object getCompoundTag(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setCompoundTag(Object entity, Object _tag) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setCollisionSize(Object entity, double w, double h) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void dealDamage(Object entity, int damage, int cause, ScriptableObject additionalParams) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static Object getAttribute(Object entity, String attribute) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static Object getPathNavigation(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static NativeArray getEntitiesInsideBox(double x1, double y1, double z1, double x2, double y2, double z2, int type, boolean flag) {
            throw new UnsupportedOperationException();
        }
    }

    @APIStaticModule
    public static class Player {
        @JSStaticFunction
        public static long get() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static boolean isPlayer(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static ScriptableObject getPointed() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void addItemInventory(int id, int count, int data, boolean preventDropThatLeft, Object extra) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void addItemCreativeInv(int id, int count, int data, Object extra) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static ItemInstance getInventorySlot(int slot) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setInventorySlot(int slot, int id, int count, int data, Object extra) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static ItemInstance getArmorSlot(int slot) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setArmorSlot(int slot, int id, int count, int data, Object extra) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static ItemInstance getCarriedItem() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static ItemInstance getOffhandItem() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setCarriedItem(int id, int count, int data, Object extra) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setOffhandItem(int id, int count, int data, Object extra) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getSelectedSlotId() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setSelectedSlotId(int slot) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getHunger() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setHunger(double val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getSaturation() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setSaturation(double val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getExhaustion() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setExhaustion(double val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void addExperience(int val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void addExp(int val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getExperience() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getExp() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setExperience(double val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setExp(double val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getLevel() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setLevel(double val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getScore() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static boolean isFlying() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setFlying(boolean val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static boolean canFly() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setCanFly(boolean val) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getX() {
            return Entity.getX(Long.valueOf(get()));
        }

        @JSStaticFunction
        public static double getY() {
            return Entity.getY(Long.valueOf(get()));
        }

        @JSStaticFunction
        public static double getZ() {
            return Entity.getY(Long.valueOf(get()));
        }

        @JSStaticFunction
        public static float[] getPosition() {
            return Entity.getPosition(Long.valueOf(get()));
        }

        @JSStaticFunction
        public static int getDimension() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setFov(double fov) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void resetFov() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setCameraEntity(Object entity) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void resetCamera() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setAbility(String ability, Object value) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static float getFloatAbility(String ability) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static boolean getBooleanAbility(String ability) {
            throw new UnsupportedOperationException();
        }
    }

    @APIStaticModule
    public static class UI {

        public static class WindowGroup {
        }

        public static class Container {
            public Container() {
            }

            public Container(Object parent) {
            }
        }

        public static class Window {
            public Window(Object location) {
            }

            public Window(ScriptableObject content) {
            }

            public Window() {
            }
        }

        public static class StandartWindow {
            public StandartWindow(ScriptableObject content) {
            }

            public StandartWindow() {
            }

            protected boolean isLegacyFormat() {
                return true;
            }
        }

        public static class StandardWindow {
            public StandardWindow(ScriptableObject content) {
            }

            public StandardWindow() {
            }

            protected boolean isLegacyFormat() {
                return false;
            }
        }

        public static class AdaptiveWindow {
            public AdaptiveWindow(ScriptableObject content) {
            }

            public AdaptiveWindow() {
            }
        }

        public static class TabbedWindow {
            public TabbedWindow(Object location) {
            }

            public TabbedWindow(ScriptableObject content) {
            }

            public TabbedWindow() {
            }
        }

        public static class WindowLocation {
            public WindowLocation() {
            }

            public WindowLocation(ScriptableObject obj) {
            }
        }

        public static class Texture {
            public Texture(Object obj) {
            }
        }

        public static class Font {
            public Font(int color, float size, float shadow) {
            }

            public Font(ScriptableObject obj) {
            }
        }

        public static class ConfigVisualizer extends com.zhekasmirnov.innercore.api.mod.util.ConfigVisualizer {
            public ConfigVisualizer(com.zhekasmirnov.innercore.mod.build.Config config, String prefix) {
                super(config, prefix);
            }

            public ConfigVisualizer(com.zhekasmirnov.innercore.mod.build.Config config) {
                super(config);
            }
        }

        @APIStaticModule
        public static class FrameTextureSource {
            @JSStaticFunction
            public static Object get(String name) {
                throw new UnsupportedOperationException();
            }
        }

        @APIStaticModule
        public static class TextureSource {
            @JSStaticFunction
            public static Object get(String name) {
                throw new UnsupportedOperationException();
            }

            @JSStaticFunction
            public static Object getNullable(String name) {
                throw new UnsupportedOperationException();
            }

            @JSStaticFunction
            public static void put(String name, Object bmp) {
                throw new UnsupportedOperationException();
            }
        }

        @JSStaticFunction
        public static float getScreenRelativeHeight() {
            return (UIUtils.screenHeight * 1000.0f) / UIUtils.screenWidth;
        }

        @JSStaticFunction
        public static float getScreenHeight() {
            return getScreenRelativeHeight();
        }

        @JSStaticFunction
        public static Object getContext() {
            return UIUtils.getContext();
        }
    }

    @APIStaticModule
    public static class IDRegistry extends com.zhekasmirnov.innercore.api.unlimited.IDRegistry {
        @Deprecated
        @JSStaticFunction
        public static void __placeholder() {
            AdaptedScriptAPI.logDeprecation("IDRegistry.__placeholder");
        }

        @Deprecated
        @JSStaticFunction
        public static String getIdInfo(int id) {
            throw new UnsupportedOperationException();
        }
    }

    @APIStaticModule
    public static class Item {
        protected Item(int id, long ptr, String nameId, String nameToDisplay) {
        }

        @JSStaticFunction
        public static Object createFoodItem(int id, String nameId, String name, String iconName, int iconIndex, int food) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static String getName(int id, int data, Object extra) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getMaxDamage(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getMaxStackSize(int id, int data) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setRequiresIconOverride(int id, boolean enabled) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void overrideCurrentIcon(String name, int index) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void overrideCurrentName(String name) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void invokeItemUseOn(int id, int count, int data, Object extra, int x, int y, int z, int side, double vx, double vy, double vz, Object entity) {
            throw new UnsupportedOperationException();
        }

        public static void invokeItemUseNoTarget(int id, int count, int data, Object extra) {
            throw new UnsupportedOperationException();
        }
    }

    public static class Armor {
        private Armor(Object actor) {
        }

        @JSStaticFunction
        public static void registerCallbacks(int id, ScriptableObject obj) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void preventDamaging(int id) {
            throw new UnsupportedOperationException();
        }
    }

    public static class ItemExtraData {
        public ItemExtraData(long extra) {
        }

        public ItemExtraData(Object extra) {
        }

        public ItemExtraData() {
        }

        protected void finalize() throws Throwable {
            super.finalize();
        }
    }

    @APIStaticModule
    public static class NBT extends NbtDataType {

        public static class CompoundTag {
            public CompoundTag() {
            }

            public CompoundTag(Object tag) {
            }

            protected void finalize() throws Throwable {
                super.finalize();
            }
        }

        public static class ListTag {
            public ListTag() {
            }

            public ListTag(Object tag) {
            }

            protected void finalize() throws Throwable {
                super.finalize();
            }
        }
    }

    @APIStaticModule
    public static class Recipes {
        @Deprecated
        @JSStaticFunction
        public static void __placeholder() {
            AdaptedScriptAPI.logDeprecation("Recipes.__placeholder");
        }
    }

    @APIStaticModule
    public static class Block {
        private static int anonymousSpecialTypeIndex = 0;

        private static SpecialType parseSpecialType(Object type) {
            SpecialType specialType = SpecialType.DEFAULT;
            if (type instanceof SpecialType) {
                SpecialType specialType2 = (SpecialType) type;
                return specialType2;
            } else if (type instanceof String) {
                SpecialType specialType3 = SpecialType.getSpecialType((String) type);
                return specialType3;
            } else if (type instanceof ScriptableObject) {
                StringBuilder sb = new StringBuilder();
                sb.append("anonymous_type_");
                int i = anonymousSpecialTypeIndex;
                anonymousSpecialTypeIndex = i + 1;
                sb.append(i);
                SpecialType specialType4 = SpecialType.createSpecialType(sb.toString());
                specialType4.setupProperties((ScriptableObject) type);
                return specialType4;
            } else {
                return specialType;
            }
        }

        @JSStaticFunction
        public static void createBlock(int uid, String nameId, ScriptableObject variantsScriptable, Object type) {
            BlockRegistry.createBlock(uid, nameId, variantsScriptable, parseSpecialType(type));
        }

        @JSStaticFunction
        public static void createLiquidBlock(int id1, String nameId1, int id2, String nameId2, ScriptableObject variantsScriptable, Object type, int tickDelay, boolean isRenewable) {
            BlockRegistry.createLiquidBlockPair(id1, nameId1, id2, nameId2, variantsScriptable, parseSpecialType(type), tickDelay, isRenewable);
        }

        @JSStaticFunction
        public static String createSpecialType(String name, ScriptableObject props) {
            SpecialType type = SpecialType.createSpecialType(name);
            type.setupProperties(props);
            return type.name;
        }

        @JSStaticFunction
        public static void setShape(int id, double x1, double y1, double z1, double x2, double y2, double z2, Object _data) {
            if (_data instanceof Number) {
                int data = ((Number) _data).intValue();
                BlockRegistry.setShape(id, data, (float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2);
                return;
            }
            BlockRegistry.setShape(id, -1, (float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2);
        }

        @JSStaticFunction
        public static int getMaterial(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static boolean isSolid(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static boolean canContainLiquid(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static boolean canBeExtraBlock(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getDestroyTime(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getExplosionResistance(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getFriction(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static double getTranslucency(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getLightLevel(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getLightOpacity(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getRenderLayer(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getRenderType(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setDestroyTime(int id, double time) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setTempDestroyTime(int id, double time) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static int getMapColor(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setRedstoneTile(int id, Object data, boolean redstone) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setRedstoneEmitter(int id, Object data, boolean redstone) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setRedstoneConnector(int id, Object data, boolean redstone) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setRandomTickCallback(int id, Function callback) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setAnimateTickCallback(int id, Function callback) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setBlockChangeCallbackEnabled(int id, boolean enabled) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setEntityInsideCallbackEnabled(int id, boolean enabled) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setEntityStepOnCallbackEnabled(int id, boolean enabled) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void setNeighbourChangeCallbackEnabled(int id, boolean enabled) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static ScriptableObject getBlockAtlasTextureCoords(String name, int id) {
            throw new UnsupportedOperationException();
        }
    }

    public static class RenderMesh {
        public RenderMesh() {
        }

        public RenderMesh(String file, String type, Scriptable params) {
            this();
        }

        public RenderMesh(String file, String type) {
            this(file, type, null);
        }

        public RenderMesh(String file) {
            this(file, "obj");
        }
    }

    @APIStaticModule
    public static class BlockRenderer {

        public static class Model {
            public Model() {
            }

            public Model(Object mesh) {
            }

            public Model(float x1, float y1, float z1, float x2, float y2, float z2, ScriptableObject obj) {
            }

            public Model(ScriptableObject obj) {
                this(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, obj);
            }

            public Model(float x1, float y1, float z1, float x2, float y2, float z2, String texName, int texId) {
            }

            public Model(String texName, int texId) {
                this(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, texName, texId);
            }

            public Model(float x1, float y1, float z1, float x2, float y2, float z2, int id, int data) {
            }

            public Model(int id, int data) {
                this(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, id, data);
            }
        }

        @JSStaticFunction
        public static Object createModel() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static Object createTexturedBox(double x1, double y1, double z1, double x2, double y2, double z2, ScriptableObject tex) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static Object createTexturedBlock(ScriptableObject tex) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void addRenderCallback(int id, Function callback) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void forceRenderRebuild(int x, int y, int z, int mode) {
            throw new UnsupportedOperationException();
        }
    }

    @APIStaticModule
    public static class Renderer {
        @JSStaticFunction
        public static Object getItemModel(int id, int count, int data, double scale, double rX, double rY, double rZ, boolean randomize) {
            throw new UnsupportedOperationException();
        }
    }

    @APIStaticModule
    public static class StaticRenderer {
        @JSStaticFunction
        public static Object createStaticRenderer(int renderer, double x, double y, double z) {
            throw new UnsupportedOperationException();
        }
    }

    public static class ActorRenderer {
        public ActorRenderer() {
        }

        public ActorRenderer(String template) {
        }
    }

    public static class AttachableRender {
        public AttachableRender(long actorUid) {
        }

        @JSStaticFunction
        public static void attachRendererToItem(int id, Object renderer, String texture, String material) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void detachRendererFromItem(int id) {
            throw new UnsupportedOperationException();
        }
    }

    @APIStaticModule
    public static class Callback {
        @JSStaticFunction
        public static void addCallback(String name, Function func, int priority) {
            com.zhekasmirnov.innercore.api.runtime.Callback.addCallback(name, func, priority);
        }

        @JSStaticFunction
        public static void invokeCallback(String name, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9, Object o10) {
            com.zhekasmirnov.innercore.api.runtime.Callback.invokeCallback(name, o1, o2, o3, o4, o5, o6, o7, o8, o9, o10);
        }
    }

    @APIStaticModule
    public static class Updatable {
        @JSStaticFunction
        public static void addUpdatable(ScriptableObject obj) {
            com.zhekasmirnov.innercore.api.runtime.Updatable.getForServer().addUpdatable(obj);
        }

        @JSStaticFunction
        public static void addLocalUpdatable(ScriptableObject obj) {
            addAnimator(obj);
        }

        @JSStaticFunction
        public static void addAnimator(ScriptableObject obj) {
            com.zhekasmirnov.innercore.api.runtime.Updatable.getForClient().addUpdatable(obj);
        }

        @JSStaticFunction
        public static List<ScriptableObject> getAll() {
            return com.zhekasmirnov.innercore.api.runtime.Updatable.getForServer().getAllUpdatableObjects();
        }

        @JSStaticFunction
        public static int getSyncTime() {
            return TickManager.getTime();
        }
    }

    @APIStaticModule
    public static class Saver {

        @APIIgnore
        private interface IObjectSaver {
            Object read(ScriptableObject scriptableObject);

            ScriptableObject save(Object obj);
        }

        @APIIgnore
        private interface IScopeSaver {
            void read(Object obj);

            Object save();
        }

        @JSStaticFunction
        public static void registerScopeSaver(String name, Object scopeSaver) {
            final IScopeSaver saver = (IScopeSaver) Context.jsToJava(scopeSaver, IScopeSaver.class);
            WorldDataScopeRegistry.getInstance().addScope(name, new ScriptableSaverScope() {
                @Override
                public Object save() {
                    return saver.save();
                }

                @Override
                public void read(Object scope) {
                    if (scope == null) {
                        scope = ScriptableObjectHelper.createEmpty();
                    }
                    saver.read(scope);
                }
            });
        }

        @JSStaticFunction
        public static int registerObjectSaver(String name, Object scopeSaver) {
            final IObjectSaver saver = (IObjectSaver) Context.jsToJava(scopeSaver, IObjectSaver.class);
            return ObjectSaverRegistry.registerSaver(name, new ObjectSaver() {
                @Override
                public Object read(ScriptableObject input) {
                    return saver.read(input);
                }

                @Override
                public ScriptableObject save(Object input) {
                    return saver.save(input);
                }
            });
        }

        @JSStaticFunction
        public static String serializeToString(Object param) {
            return ScriptableSerializer.jsonToString(ScriptableSerializer.scriptableToJson(param, null));
        }

        @JSStaticFunction
        public static Object deserializeFromString(String param) {
            try {
                return ScriptableSerializer.scriptableFromJson(ScriptableSerializer.stringToJson(param));
            } catch (JSONException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        @JSStaticFunction
        public static void registerObject(Object obj, int saverId) {
            ObjectSaverRegistry.registerObject(obj, saverId);
        }

        @JSStaticFunction
        public static void setObjectIgnored(ScriptableObject obj, boolean ignore) {
            ObjectSaverRegistry.setObjectIgnored(obj, ignore);
        }

        @JSStaticFunction
        public static int getObjectSaverId(Object obj) {
            ObjectSaver saver = ObjectSaverRegistry.getSaverFor(obj);
            if (saver != null) {
                return saver.getSaverId();
            }
            return -1;
        }
    }

    public static class Config extends com.zhekasmirnov.innercore.mod.build.Config {
        public Config(File file) {
            super(file);
        }

        public Config(CharSequence path) {
            super(new File(path.toString()));
        }
    }

    @APIStaticModule
    public static class Resources {
        @JSStaticFunction
        public static String getItemTextureName(String name, int index) {
            return ResourcePackManager.getItemTextureName(name, index);
        }

        @JSStaticFunction
        public static String getBlockTextureName(String name, int index) {
            return ResourcePackManager.getBlockTextureName(name, index);
        }

        @JSStaticFunction
        public static InputStream getInputStream(String name) {
            try {
                return FileTools.getAssetInputStream(name);
            } catch (IOException e) {
                return null;
            }
        }

        @JSStaticFunction
        public static byte[] getBytes(String name) {
            try {
                return FileTools.getAssetBytes(name);
            } catch (IOException e) {
                return null;
            }
        }
    }

    @APIStaticModule
    public static class Translation {
        @JSStaticFunction
        public static void addTranslation(String name, ScriptableObject localization) {
            NameTranslation.addTranslation(name, localization);
        }

        @JSStaticFunction
        public static String translate(String str) {
            return NameTranslation.translate(str);
        }

        @JSStaticFunction
        public static String getLanguage() {
            return NameTranslation.getLanguage();
        }
    }

    @APIStaticModule
    public static class WorldRenderer {
        @JSStaticFunction
        public static Object getGlobalUniformSet() {
            throw new UnsupportedOperationException();
        }
    }

    public static class CustomBiome {
        public CustomBiome(String name) {
        }
    }

    @APIStaticModule
    public static class Dimensions {

        public static class MonoBiomeTerrainGenerator {
        }

        public static class NoiseConversion {
        }

        public static class NoiseGenerator {
        }

        public static class NoiseLayer {
        }

        public static class CustomDimension {
            public CustomDimension(String name, int preferredId) {
            }
        }

        public static class CustomGenerator {
            public CustomGenerator(int type) {
            }

            public CustomGenerator(String type) {
            }
        }

        public static class NoiseOctave {
            public NoiseOctave(int type) {
            }

            public NoiseOctave(String type) {
            }

            public NoiseOctave() {
            }
        }

        @JSStaticFunction
        public static void overrideGeneratorForVanillaDimension(int id, Object generator) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static Object getDimensionByName(String name) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static Object getDimensionById(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static boolean isLimboId(int id) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void transfer(Object entity, int dimension) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static ScriptableObject getAllRegisteredCustomBiomes() {
            throw new UnsupportedOperationException();
        }
    }

    @JSStaticFunction
    public static void runOnMainThread(Object _action) {
        Runnable action = (Runnable) Context.jsToJava(_action, Runnable.class);
        MainThreadQueue.serverThread.enqueue(action);
    }

    @JSStaticFunction
    public static void runOnClientThread(Object _action) {
        Runnable action = (Runnable) Context.jsToJava(_action, Runnable.class);
        MainThreadQueue.localThread.enqueue(action);
    }

    @APIStaticModule
    public static class MCSystem {
        @JSStaticFunction
        public static Object getNetwork() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void simulateBackPressed() {
            NativeCallback.onKeyEventDispatched(0, 1);
        }

        @JSStaticFunction
        public static Object getContext() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void runAsUi(Object runnable) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static void debugStr(String s) {
            DebugAPI.dialog(s);
        }

        @JSStaticFunction
        public static void debugBmp(Object bmp) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static String getMinecraftVersion() {
            return MinecraftVersions.getCurrent().getName();
        }

        @JSStaticFunction
        public static Object getInnerCoreVersion() {
            return Version.INNER_CORE_VERSION.name;
        }

        @JSStaticFunction
        public static void throwException(String msg) {
            throw new RuntimeException(msg);
        }

        @JSStaticFunction
        public static void debugAPILookUp() {
            API.debugLookUpClass(AdaptedScriptAPI.class);
        }

        @JSStaticFunction
        public static void runOnMainThread(Object _action) {
            Runnable action = (Runnable) Context.jsToJava(_action, Runnable.class);
            MainThreadQueue.serverThread.enqueue(action);
        }

        @JSStaticFunction
        public static void runOnClientThread(Object _action) {
            Runnable action = (Runnable) Context.jsToJava(_action, Runnable.class);
            MainThreadQueue.localThread.enqueue(action);
        }

        @JSStaticFunction
        public static void setLoadingTip(String tip) {
            LoadingUI.setTip(tip);
        }

        @JSStaticFunction
        public static void setNativeThreadPriority(int p) {
            AdaptedScriptAPI.logDeprecation("setNativeThreadPriority");
        }

        @JSStaticFunction
        public static void forceNativeCrash() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static boolean isDefaultPrevented() {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static boolean isMainThreadStopped() {
            return TickManager.isStopped();
        }

        @JSStaticFunction
        public static Object evalInScope(String script, Scriptable scope, String name) {
            return Compiler.assureContextForCurrentThread().evaluateString(scope, script, name, 0, null);
        }

        @JSStaticFunction
        public static String addRuntimePack(String typeStr, String name) {
            ModLoader.MinecraftPackType type = ModLoader.MinecraftPackType.fromString(typeStr);
            return ModLoader.instance.addRuntimePack(type, name).getAbsolutePath();
        }

        @JSStaticFunction
        public static void setCustomFatalErrorCallback(Object callback) {
            DialogHelper.setCustomFatalErrorCallback((DialogHelper.ICustomErrorCallback) Context.jsToJava(callback, DialogHelper.ICustomErrorCallback.class));
        }

        @JSStaticFunction
        public static void setCustomNonFatalErrorCallback(Object callback) {
            DialogHelper.setCustomNonFatalErrorCallback((DialogHelper.ICustomErrorCallback) Context.jsToJava(callback, DialogHelper.ICustomErrorCallback.class));
        }

        @JSStaticFunction
        public static void setCustomStartupErrorCallback(Object callback) {
            DialogHelper.setCustomStartupErrorCallback((DialogHelper.ICustomErrorCallback) Context.jsToJava(callback, DialogHelper.ICustomErrorCallback.class));
        }

        @JSStaticFunction
        public static String getCurrentThreadType() {
            throw new UnsupportedOperationException();
        }
    }

    @APIStaticModule
    public static class FileUtil {
        @JSStaticFunction
        public static String readFileText(String path) {
            try {
                return FileUtils.readFileText(new File(path));
            } catch (IOException exception) {
                ICLog.e("FileUtil", "error in reading file " + path, exception);
                return null;
            }
        }

        @JSStaticFunction
        public static void writeFileText(String path, String text) {
            try {
                FileUtils.writeFileText(new File(path), text);
            } catch (IOException exception) {
                ICLog.e("FileUtil", "error in writing file " + path, exception);
            }
        }
    }

    @APIStaticModule
    public static class Commands {
        @JSStaticFunction
        public static String exec(String command, Object player0, Object blockSource0) {
            throw new UnsupportedOperationException();
        }

        @JSStaticFunction
        public static String execAt(String command, int x, int y, int z, Object blockSource0) {
            throw new UnsupportedOperationException();
        }
    }

    @APIStaticModule
    public static class TagRegistry extends com.zhekasmirnov.innercore.api.mod.TagRegistry {
        private static List<String> toTagList(NativeArray arr) {
            List<String> result = new ArrayList<>();
            if (arr != null) {
                for (Object obj : arr.toArray()) {
                    if (obj != null) {
                        result.add(obj.toString());
                    }
                }
            }
            return result;
        }

        private static String[] toTagArray(NativeArray arr) {
            List<String> tags = toTagList(arr);
            String[] result = new String[tags.size()];
            tags.toArray(result);
            return result;
        }

        @JSStaticFunction
        public static void addTagFactory(String group, Object factory) {
            getOrCreateGroup(group).addTagFactory((TagRegistry.TagFactory) Context.jsToJava(factory, TagRegistry.TagFactory.class));
        }

        @JSStaticFunction
        public static void addCommonObject(String group, Object obj, NativeArray tags) {
            getOrCreateGroup(group).addCommonObject(obj, toTagArray(tags));
        }

        @JSStaticFunction
        public static void removeCommonObject(String group, Object obj) {
            getOrCreateGroup(group).removeCommonObject(obj);
        }

        @JSStaticFunction
        public static void addTagsFor(String group, Object obj, NativeArray tags, boolean noAdd) {
            if (noAdd) {
                getOrCreateGroup(group).addTagsFor(obj, toTagArray(tags));
            } else {
                getOrCreateGroup(group).addCommonObject(obj, toTagArray(tags));
            }
        }

        @JSStaticFunction
        public static void addTagFor(String group, Object obj, String tag, boolean noAdd) {
            if (noAdd) {
                getOrCreateGroup(group).addTagsFor(obj, tag);
            } else {
                getOrCreateGroup(group).addCommonObject(obj, tag);
            }
        }

        @JSStaticFunction
        public static void removeTagsFor(String group, Object obj, NativeArray tags) {
            getOrCreateGroup(group).removeTagsFor(obj, toTagArray(tags));
        }

        @JSStaticFunction
        public static NativeArray getTagsFor(String group, Object obj) {
            Collection<String> tags = getOrCreateGroup(group).getTags(obj);
            return ScriptableObjectHelper.createArray(tags.toArray());
        }

        @JSStaticFunction
        public static NativeArray getAllWith(String group, Object _predicate) {
            TagRegistry.TagPredicate predicate = (TagRegistry.TagPredicate) Context.jsToJava(_predicate, TagRegistry.TagPredicate.class);
            Collection<Object> objects = getOrCreateGroup(group).getAllWhere(predicate);
            return ScriptableObjectHelper.createArray(objects.toArray());
        }

        @JSStaticFunction
        public static NativeArray getAllWithTags(String group, NativeArray tags) {
            Collection<Object> objects = getOrCreateGroup(group).getAllWithTags(toTagList(tags));
            return ScriptableObjectHelper.createArray(objects.toArray());
        }

        @JSStaticFunction
        public static NativeArray getAllWithTag(String group, String tag) {
            Collection<Object> objects = getOrCreateGroup(group).getAllWithTag(tag);
            return ScriptableObjectHelper.createArray(objects.toArray());
        }
    }

    public static class NetworkConnectedClientList {
        public NetworkConnectedClientList(boolean addToGlobalRefreshList) {
        }

        public NetworkConnectedClientList() {
        }
    }

    public static class NetworkEntity {
        public NetworkEntity(Object type, Object target, String name) {
        }

        public NetworkEntity(Object type, Object target) {
        }

        public NetworkEntity(Object type) {
        }
    }

    public static class NetworkEntityType {
        public NetworkEntityType(String typeName) {
        }
    }

    public static class SyncedNetworkData {
        public SyncedNetworkData(String name) {
        }

        public SyncedNetworkData() {
        }
    }

    public static class ItemContainer {
        public ItemContainer() {
        }

        public ItemContainer(Object container) {
        }
    }

    public static class BlockSource {
        public BlockSource(int dimension, boolean b1, boolean b2) {
        }

        public BlockSource(int dimension) {
        }

        protected void finalize() throws Throwable {
            super.finalize();
        }
    }

    public static class BlockState extends com.zhekasmirnov.apparatus.adapter.innercore.game.block.BlockState {
        public BlockState(int id, int data) {
            super(id, data);
        }

        public BlockState(int id, Object scriptable) {
            super(id, (Scriptable) scriptable);
        }
    }

    public static class PlayerActor {
        public PlayerActor(long entity) {
        }
    }

    @JSStaticFunction
    public static Function requireMethodFromNativeAPI(String _className, final String methodName, final boolean denyConversion) {
        String str = _className;
        if (!str.startsWith("com.zhekasmirnov.innercore.")) {
            str = "com.zhekasmirnov.innercore." + str;
        }
        final String _className2 = str;
        try {
            Class<?> clazz = Class.forName(_className2);
            Method[] methods = clazz.getMethods();
            Method _method = null;
            for (Method m : methods) {
                if (m.getName().equals(methodName)) {
                    _method = m;
                }
            }
            if (_method == null) {
                throw new RuntimeException("method cannot be found class=" + _className2 + " method=" + methodName);
            }
            final Method method = _method;
            final Class<?>[] types = method.getParameterTypes();
            final Object[] javaParams = new Object[types.length];
            final boolean[] isNumberParam = new boolean[types.length];
            final int[] numberParamType = new int[types.length];
            boolean _onlyNumbers = true;
            for (int i = 0; i < types.length; i++) {
                isNumberParam[i] = Integer.TYPE.isAssignableFrom(types[i]) || Double.TYPE.isAssignableFrom(types[i]) || Float.TYPE.isAssignableFrom(types[i]);
                if (!isNumberParam[i]) {
                    _onlyNumbers = false;
                } else if (Integer.TYPE.isAssignableFrom(types[i])) {
                    numberParamType[i] = 0;
                } else if (Float.TYPE.isAssignableFrom(types[i])) {
                    numberParamType[i] = 1;
                } else if (Double.TYPE.isAssignableFrom(types[i])) {
                    numberParamType[i] = 1;
                }
            }
            final boolean onlyNumbers = _onlyNumbers;
            return new ScriptableFunctionImpl() {
                @Override
                public Object call(Context context, Scriptable parent, Scriptable current, Object[] params) {
                    Object[] objArr;
                    if (denyConversion) {
                        objArr = params;
                    } else {
                        int i2 = 0;
                        if (onlyNumbers) {
                            for (int i4 = 0; i4 < types.length; i4++) {
                                Number param2 = 0;
                                if (i4 < params.length) {
                                    Object _param = params[i4];
                                    param2 = _param instanceof Number ? (Number) _param : 0;
                                }
                                switch (numberParamType[i4]) {
                                    case 0:
                                        javaParams[i4] = Integer.valueOf(param2.intValue());
                                        break;
                                    case 1:
                                        javaParams[i4] = Float.valueOf(param2.floatValue());
                                        break;
                                    case 2:
                                        javaParams[i4] = Double.valueOf(param2.doubleValue());
                                        break;
                                }
                            }
                        } else {
                            while (true) {
                                int i3 = i2;
                                if (i3 >= types.length) {
                                    break;
                                }
                                Object param = i3 >= params.length ? null : params[i3];
                                if (param == null && isNumberParam[i3]) {
                                    param = Double.valueOf(0.0d);
                                }
                                javaParams[i3] = Context.jsToJava(param, types[i3]);
                                i2 = i3 + 1;
                            }
                        }
                        objArr = javaParams;
                    }
                    try {
                        return Context.javaToJS(method.invoke(null, objArr), parent);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e.toString());
                    } catch (InvocationTargetException e2) {
                        throw new RuntimeException(e2.toString());
                    } catch (Exception e3) {
                        ICLog.i("ERROR", "failed to call required java method class=" + _className2 + " method=" + methodName);
                        throw e3;
                    }
                }
            };
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.toString());
        }
    }
}
