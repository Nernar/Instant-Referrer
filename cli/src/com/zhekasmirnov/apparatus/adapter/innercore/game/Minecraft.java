package com.zhekasmirnov.apparatus.adapter.innercore.game;

import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.runtime.LevelInfo;

public class Minecraft {
    private static GameState lastWorldState = GameState.NON_WORLD;
    private static GameState state = GameState.NON_WORLD;
    private static boolean isLeaveGamePosted = false;

    public enum GameState {
        NON_WORLD,
        HOST_WORLD,
        REMOTE_WORLD;

        public static GameState[] valuesCustom() {
            GameState[] valuesCustom = values();
            int length = valuesCustom.length;
            GameState[] gameStateArr = new GameState[length];
            System.arraycopy(valuesCustom, 0, gameStateArr, 0, length);
            return gameStateArr;
        }
    }

    public static void leaveGame() {
        if (state != GameState.NON_WORLD) {
            lastWorldState = state;
            state = GameState.NON_WORLD;
        }
        if (!NativeCallback.isLevelDisplayed()) {
            isLeaveGamePosted = true;
        }
    }

    public static void onLevelDisplayed() {
        if (isLeaveGamePosted) {
            isLeaveGamePosted = false;
        }
    }

    public static void onLevelSelected() {
        isLeaveGamePosted = false;
        GameState gameState = GameState.HOST_WORLD;
        state = gameState;
        lastWorldState = gameState;
    }

    public static void onConnectToHost(String host, int port) {
        isLeaveGamePosted = false;
        GameState gameState = GameState.REMOTE_WORLD;
        state = gameState;
        lastWorldState = gameState;
    }

    public static void onGameStopped(boolean isServer) {
        isLeaveGamePosted = false;
        if (isServer && state == GameState.HOST_WORLD) {
            state = GameState.NON_WORLD;
            LevelInfo.onLeft();
        }
        if (!isServer && state == GameState.REMOTE_WORLD) {
            state = GameState.NON_WORLD;
            LevelInfo.onLeft();
        }
    }

    public static GameState getGameState() {
        return state;
    }

    public static GameState getLastWorldState() {
        return lastWorldState;
    }
}
