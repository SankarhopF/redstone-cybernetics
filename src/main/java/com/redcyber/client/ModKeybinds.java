package com.redcyber.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/** Клавиши мода. Меню тумблеров открывается удержанием клавиши. */
public final class ModKeybinds {
    /** Удержание этой клавиши открывает меню отключения возможностей кибернетики. */
    public static final KeyBinding ABILITIES = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.redcyber.abilities",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.redcyber"));

    private ModKeybinds() {
    }

    /** Регистрация происходит в статическом инициализаторе; метод задаёт порядок. */
    public static void init() {
    }
}
