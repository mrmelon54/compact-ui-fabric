package net.onpointcoding.compactui.duck;

import net.minecraft.client.gui.screen.pack.PackListWidget;

public interface ResourcePackScreenDuckProvider {
    PackListWidget getSelectedPackListWidget();

    PackListWidget getAvailablePackList();
}
