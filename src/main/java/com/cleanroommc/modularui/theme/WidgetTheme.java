package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

@SideOnly(Side.CLIENT)
public class WidgetTheme {

    @Nullable
    private final IDrawable background;
    @Nullable
    private final IDrawable hoverBackground;
    private final int color;
    private final int textColor;
    private final boolean textShadow;

    public WidgetTheme(@Nullable IDrawable background, @Nullable IDrawable hoverBackground, int color, int textColor, boolean textShadow) {
        this.background = background;
        this.hoverBackground = hoverBackground;
        this.color = color;
        this.textColor = textColor;
        this.textShadow = textShadow;
    }

    public WidgetTheme(WidgetTheme parent, JsonObject fallback, JsonObject json) {
        this.background = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parent.getBackground(), "background", "bg");
        this.hoverBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parent.getHoverBackground(), "hoverBackground", "hbg");
        this.color = JsonHelper.getColorWithFallback(json, fallback, parent.getColor(), "color");
        this.textColor = JsonHelper.getColorWithFallback(json, fallback, parent.getTextColor(), "textColor");
        this.textShadow = JsonHelper.getBoolWithFallback(json, fallback, parent.getTextShadow(), "textShadow");
    }

    public @Nullable IDrawable getBackground() {
        return background;
    }

    public @Nullable IDrawable getHoverBackground() {
        return hoverBackground;
    }

    public int getColor() {
        return color;
    }

    public int getTextColor() {
        return textColor;
    }

    public boolean getTextShadow() {
        return textShadow;
    }
}
