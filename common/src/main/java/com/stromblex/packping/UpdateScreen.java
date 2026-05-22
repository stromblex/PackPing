package com.stromblex.packping;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class UpdateScreen extends Screen {
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 8;
    private static final int PADDING = 16;
    private static final int HEADER_HEIGHT = 40;
    private static final long FADE_DURATION_MS = 300;

    private final Screen parent;
    private final String version;
    private final String downloadUrl;
    private final String changelog;
    private final long openTime;
    private float alpha = 0f;
    private boolean closing = false;
    private long closeStartTime;

    public UpdateScreen(Screen parent, String version, String downloadUrl, String changelog) {
        super(Component.literal("PackPing Update"));
        this.parent = parent;
        this.version = version;
        this.downloadUrl = downloadUrl;
        this.changelog = changelog;
        this.openTime = System.currentTimeMillis();
    }

    private int getBoxWidth() {
        return Math.max(200, Math.min(400, this.width - 30));
    }

    private int getBoxHeight() {
        return Math.max(140, Math.min(200, this.height - 30));
    }

    @Override
    protected void init() {
        super.init();

        int boxWidth = getBoxWidth();
        int boxHeight = getBoxHeight();
        int boxX = (this.width - boxWidth) / 2;
        int boxY = (this.height - boxHeight) / 2;

        int buttonWidth = Math.min(100, (boxWidth - BUTTON_SPACING - PADDING * 2) / 2);
        int totalBtnWidth = buttonWidth * 2 + BUTTON_SPACING;
        int startX = boxX + (boxWidth - totalBtnWidth) / 2;
        int buttonY = boxY + boxHeight - PADDING - BUTTON_HEIGHT;

        this.addRenderableWidget(new FlatButton(
                startX, buttonY, buttonWidth, BUTTON_HEIGHT,
                Component.literal(PackPingConfig.getDownloadButtonText()),
                button -> {
                    Util.getPlatform().openUri(downloadUrl);
                    if (PackPingConfig.shouldCloseGameAfterDownload()) {
                        if (this.minecraft != null) this.minecraft.stop();
                    } else {
                        startClosing();
                    }
                },
                0xFF1C3A5E, 0xFF245080, 0xFF2A5F9E));

        this.addRenderableWidget(new FlatButton(
                startX + buttonWidth + BUTTON_SPACING, buttonY, buttonWidth, BUTTON_HEIGHT,
                Component.literal(PackPingConfig.getSkipButtonText()),
                button -> startClosing(),
                0xFF1C2333, 0xFF2A3345, 0xFF3D4F65));
    }

    private void startClosing() {
        if (!closing) {
            closing = true;
            closeStartTime = System.currentTimeMillis();
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        long now = System.currentTimeMillis();

        if (closing) {
            float progress = (float) (now - closeStartTime) / FADE_DURATION_MS;
            alpha = 1f - Math.min(1f, progress);
            if (progress >= 1f) {
                if (this.minecraft != null) this.minecraft.setScreen(parent);
                return;
            }
        } else {
            float progress = (float) (now - openTime) / FADE_DURATION_MS;
            alpha = Math.min(1f, progress);
        }

        int bgAlpha = (int) (alpha * 240);
        context.fill(0, 0, this.width, this.height, (bgAlpha << 24) | 0x010409);

        int boxWidth = getBoxWidth();
        int boxHeight = getBoxHeight();
        int boxX = (this.width - boxWidth) / 2;
        int boxY = (this.height - boxHeight) / 2;

        int panelAlpha = (int) (alpha * 255);
        int a = panelAlpha << 24;

        // Panel
        context.fill(boxX - 1, boxY - 1, boxX + boxWidth + 1, boxY + boxHeight + 1, a | 0x21262D);
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, a | 0x0D1117);

        // Header
        context.fill(boxX, boxY, boxX + boxWidth, boxY + HEADER_HEIGHT, a | 0x161B22);
        context.fill(boxX, boxY + HEADER_HEIGHT, boxX + boxWidth, boxY + HEADER_HEIGHT + 1, a | 0x21262D);

        int headerTextY = boxY + (HEADER_HEIGHT - 8) / 2;

        String title = PackPingConfig.getFullscreenTitle();
        if (this.font.width(title) > boxWidth - 100) {
            title = this.font.plainSubstrByWidth(title, boxWidth - 110) + "...";
        }
        context.drawString(this.font, title, boxX + PADDING, headerTextY, 0xFFF0F6FC, false);

        // Version badge
        String versionBadge = "v" + version;
        int badgeTextWidth = this.font.width(versionBadge);
        int badgeX = boxX + boxWidth - badgeTextWidth - PADDING - 6;
        if (badgeX > boxX + this.font.width(title) + PADDING + 10) {
            context.fill(badgeX - 4, headerTextY - 3, badgeX + badgeTextWidth + 4, headerTextY + 11, a | 0x238636);
            context.drawString(this.font, versionBadge, badgeX, headerTextY, 0xFFFFFFFF, false);
        }

        // Content area
        int contentY = boxY + HEADER_HEIGHT + 10;
        int contentMaxWidth = boxWidth - PADDING * 2;
        int buttonY = boxY + boxHeight - PADDING - BUTTON_HEIGHT;
        int contentEndY = buttonY - 12;

        // Version info
        if (contentY + 10 < contentEndY) {
            String currentText = "Current: v" + PackPingConfig.getLocalVersion();
            if (this.font.width(currentText) > contentMaxWidth)
                currentText = this.font.plainSubstrByWidth(currentText, contentMaxWidth - 10) + "...";
            context.drawString(this.font, currentText, boxX + PADDING, contentY, 0xFF8B949E, false);
            contentY += 12;
        }
        if (contentY + 10 < contentEndY) {
            String latestText = "Latest: v" + version;
            if (this.font.width(latestText) > contentMaxWidth)
                latestText = this.font.plainSubstrByWidth(latestText, contentMaxWidth - 10) + "...";
            context.drawString(this.font, latestText, boxX + PADDING, contentY, 0xFF58A6FF, false);
            contentY += 16;
        }

        // Separator
        if (contentY + 14 < contentEndY) {
            context.fill(boxX + PADDING, contentY, boxX + boxWidth - PADDING, contentY + 1, a | 0x21262D);
            contentY += 8;
        }

        // Changelog
        if (contentY + 10 < contentEndY) {
            context.drawString(this.font, "What's Changed:", boxX + PADDING, contentY, 0xFFF0F6FC, false);
            contentY += 12;

            String[] lines = changelog.split("\\\\n|\\n");
            for (String line : lines) {
                if (contentY + 10 > contentEndY) break;
                String displayLine = "\u2022 " + line.trim();
                if (this.font.width(displayLine) > contentMaxWidth) {
                    displayLine = this.font.plainSubstrByWidth(displayLine, contentMaxWidth - 10) + "...";
                }
                context.drawString(this.font, displayLine, boxX + PADDING, contentY, 0xFF8B949E, false);
                contentY += 11;
            }
        }

        // Bottom separator
        context.fill(boxX + PADDING, buttonY - 8, boxX + boxWidth - PADDING, buttonY - 7, a | 0x21262D);

        // Render buttons (via super) AFTER panel so they appear on top
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        startClosing();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private static class FlatButton extends Button {
        private final int bgColor;
        private final int hoverColor;
        private final int borderColor;

        public FlatButton(int x, int y, int width, int height, Component message, OnPress onPress,
                          int bgColor, int hoverColor, int borderColor) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.bgColor = bgColor;
            this.hoverColor = hoverColor;
            this.borderColor = borderColor;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            int bg = this.isHovered() ? hoverColor : bgColor;
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), borderColor);
            graphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, bg);
            int textColor = this.isHovered() ? 0xFFFFFFFF : 0xFFD0D7DE;
            graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                    getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, textColor);
        }
    }
}
