package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.common.widget.textfield.TextFieldHandler;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public class TextFieldRenderer extends TextRenderer {

    private final TextFieldHandler handler;
    private int markedColor = 0x2F72A8;
    private boolean renderCursor = false;

    public TextFieldRenderer(TextFieldHandler handler) {
        this.handler = handler;
    }

    public void toggleCursor() {
        this.renderCursor = !this.renderCursor;
    }

    public void setCursor(boolean active) {
        this.renderCursor = active;
    }

    public void setMarkedColor(int markedColor) {
        this.markedColor = markedColor;
    }

    @Override
    protected void drawMeasuredLines(List<Pair<String, Float>> measuredLines) {
        if (!simulate) {
            Point2D.Float start;
            if (handler.hasTextMarked()) {
                start = getPosOf(measuredLines, handler.getStartCursor());
                // render Marked
                Point2D.Float end = getPosOf(measuredLines, handler.getEndCursor());

                if (start.y == end.y) {
                    drawMarked(start.y, start.x, end.x);
                } else {
                    int min = handler.getStartCursor().y;
                    int max = handler.getEndCursor().y;
                    Pair<String, Float> line = measuredLines.get(min);
                    float startX = getStartX(line.getValue());
                    drawMarked(start.y, start.x, startX + line.getValue());
                    start.y += FR.FONT_HEIGHT * scale;
                    if (max - min > 1) {
                        for (int i = min + 1; i < max - 1; i++) {
                            line = measuredLines.get(i);
                            startX = getStartX(line.getValue());
                            drawMarked(start.y, startX, startX + line.getValue());
                            start.y += FR.FONT_HEIGHT * scale;
                        }
                    }
                    line = measuredLines.get(max);
                    startX = getStartX(line.getValue());
                    drawMarked(start.y, startX, end.x);
                }
            }
            // draw cursor
            start = getPosOf(measuredLines, handler.getMainCursor());
            if (this.renderCursor) {
                drawCursor(start.x, start.y);
            }
        }
        super.drawMeasuredLines(measuredLines);
    }

    public Point getCursorPos(List<String> lines, int x, int y) {
        if (lines.isEmpty()) {
            return new Point();
        }
        List<Pair<String, Float>> measuredLines = measureLines(lines);
        y -= getStartY(measuredLines.size()) + this.y;
        int index = (int) (y / (FR.FONT_HEIGHT * scale));
        if (index < 0) return new Point();
        if (index >= measuredLines.size())
            return new Point(measuredLines.get(measuredLines.size() - 1).getKey().length(), measuredLines.size() - 1);
        Pair<String, Float> line = measuredLines.get(index);
        x -= getStartX(line.getValue()) + this.x;
        if (line.getValue() <= 0) return new Point(0, index);
        if (line.getValue() < x) return new Point(line.getKey().length(), index);
        float currentX = 0;
        for (int i = 0; i < line.getKey().length(); i++) {
            char c = line.getKey().charAt(i);
            currentX += FR.getCharWidth(c) * scale;
            if (currentX >= x) {
                return new Point(i, index);
            }
        }
        return new Point();
    }

    public Point2D.Float getPosOf(List<Pair<String, Float>> measuredLines, Point cursorPos) {
        if (measuredLines.isEmpty()) {
            return new Point2D.Float(getStartX(0), getStartY(1));
        }
        Pair<String, Float> line = measuredLines.get(cursorPos.y);
        String sub = line.getKey().substring(0, Math.min(line.getKey().length(), cursorPos.x));
        return new Point2D.Float(getStartX(line.getRight()) + FR.getStringWidth(sub) * scale, getStartY(measuredLines.size()));
    }

    @SideOnly(Side.CLIENT)
    public void drawMarked(float y0, float x0, float x1) {
        y0 -= 1;
        float y1 = y0 + FR.FONT_HEIGHT * scale;
        float red = Color.getRedF(markedColor);
        float green = Color.getGreenF(markedColor);
        float blue = Color.getBlueF(markedColor);
        float alpha = Color.getAlphaF(markedColor);
        if (alpha == 0)
            alpha = 1f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(red, green, blue, alpha);
        GlStateManager.disableTexture2D();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x0, y1, 0.0D).endVertex();
        bufferbuilder.pos(x1, y1, 0.0D).endVertex();
        bufferbuilder.pos(x1, y0, 0.0D).endVertex();
        bufferbuilder.pos(x0, y0, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    @SideOnly(Side.CLIENT)
    private void drawCursor(float x0, float y0) {
        x0 = (x0 - 0.8f) / scale;
        y0 = (y0 - 1) / scale;
        float x1 = x0 + 0.6f /* * (1 / getScale())*/;
        float y1 = y0 + 9;
        float red = Color.getRedF(color);
        float green = Color.getGreenF(color);
        float blue = Color.getBlueF(color);
        float alpha = Color.getAlphaF(color);
        if (alpha == 0)
            alpha = 1f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 0);
        GlStateManager.color(red, green, blue, alpha);
        GlStateManager.disableTexture2D();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x0, y1, 0.0D).endVertex();
        bufferbuilder.pos(x1, y1, 0.0D).endVertex();
        bufferbuilder.pos(x1, y0, 0.0D).endVertex();
        bufferbuilder.pos(x0, y0, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
    }
}
