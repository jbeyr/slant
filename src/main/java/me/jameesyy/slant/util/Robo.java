package me.jameesyy.slant.util;

import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class Robo {

    public enum Mouse {
        LEFT, RIGHT
    }

    public static void setMouseButtonStateWithReflectoinsHelper(int mouseButton, boolean held) {
        MouseEvent m = new MouseEvent();

        ObfuscationReflectionHelper.setPrivateValue(MouseEvent.class, m, mouseButton, "button");
        ObfuscationReflectionHelper.setPrivateValue(MouseEvent.class, m, held, "buttonstate");
        MinecraftForge.EVENT_BUS.post(m);

        ByteBuffer buttons = ObfuscationReflectionHelper.getPrivateValue(Mouse.class, null, "buttons");
        buttons.put(mouseButton, (byte) (held ? 1 : 0));
        ObfuscationReflectionHelper.setPrivateValue(Mouse.class, null, buttons, "buttons");

    }

    public static void setMouseButtonState(int mouseButton, boolean held) {
        try {
            MouseEvent m = new MouseEvent();

            Field buttonField = MouseEvent.class.getDeclaredField("button");
            Field buttonStateField = MouseEvent.class.getDeclaredField("buttonstate");

            buttonField.setAccessible(true);
            buttonField.setInt(m, mouseButton);

            buttonStateField.setAccessible(true);
            buttonStateField.setBoolean(m, held);

            MinecraftForge.EVENT_BUS.post(m); // fire mouse button state update event

            // access and update the "buttons" ByteBuffer in Mouse
            Field buttonsField = Mouse.class.getDeclaredField("buttons");
            buttonsField.setAccessible(true);
            ByteBuffer buttons = (ByteBuffer) buttonsField.get(null); // null because it's a static field
            buttons.put(mouseButton, (byte) (held ? 1 : 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
