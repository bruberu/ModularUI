package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.SingleChildWidget;
import org.jetbrains.annotations.NotNull;

public class ButtonWidget<W extends ButtonWidget<W>> extends SingleChildWidget<W> implements Interactable {

    private IGuiAction.MousePressed mousePressed;
    private IGuiAction.MouseReleased mouseReleased;
    private IGuiAction.MousePressed mouseTapped;
    private IGuiAction.MouseScroll mouseScroll;
    private IGuiAction.KeyPressed keyPressed;
    private IGuiAction.KeyReleased keyReleased;
    private IGuiAction.KeyPressed keyTapped;

    @Override
    public WidgetTheme getWidgetTheme(ITheme theme) {
        return theme.getButtonTheme();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (this.mousePressed != null && this.mousePressed.press(mouseButton)) {
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        return this.mouseReleased != null && this.mouseReleased.release(mouseButton);
    }

    @NotNull
    @Override
    public Result onMouseTapped(int mouseButton) {
        if (this.mouseTapped != null && this.mouseTapped.press(mouseButton)) {
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public @NotNull Result onKeyPressed(char typedChar, int keyCode) {
        if (this.keyPressed != null && this.keyPressed.press(typedChar, keyCode)) {
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onKeyRelease(char typedChar, int keyCode) {
        return this.keyReleased != null && this.keyReleased.release(typedChar, keyCode);
    }

    @NotNull
    @Override
    public Result onKeyTapped(char typedChar, int keyCode) {
        if (this.keyTapped != null && this.keyTapped.press(typedChar, keyCode)) {
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        return this.mouseScroll != null && this.mouseScroll.scroll(scrollDirection, amount);
    }

    public W onMousePressed(IGuiAction.MousePressed mousePressed) {
        this.mousePressed = mousePressed;
        return getThis();
    }

    public W onMouseReleased(IGuiAction.MouseReleased mouseReleased) {
        this.mouseReleased = mouseReleased;
        return getThis();
    }

    public W onMouseTapped(IGuiAction.MousePressed mouseTapped) {
        this.mouseTapped = mouseTapped;
        return getThis();
    }

    public W onMouseScrolled(IGuiAction.MouseScroll mouseScroll) {
        this.mouseScroll = mouseScroll;
        return getThis();
    }

    public W onKeyPressed(IGuiAction.KeyPressed keyPressed) {
        this.keyPressed = keyPressed;
        return getThis();
    }

    public W onKeyReleased(IGuiAction.KeyReleased keyReleased) {
        this.keyReleased = keyReleased;
        return getThis();
    }

    public W onKeyTapped(IGuiAction.KeyPressed keyTapped) {
        this.keyTapped = keyTapped;
        return getThis();
    }
}
