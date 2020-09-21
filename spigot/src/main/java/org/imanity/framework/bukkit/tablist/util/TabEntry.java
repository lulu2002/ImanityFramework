package org.imanity.framework.bukkit.tablist.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.imanity.framework.bukkit.tablist.ImanityTablist;
import org.imanity.framework.bukkit.util.Skin;

import java.util.UUID;

@Getter @Setter @AllArgsConstructor
public class TabEntry {

    private String id;
    private UUID uuid;
    private String text;
    private ImanityTablist tab;
    private Skin texture;
    private TabColumn column;
    private int slot;
    private int rawSlot;
    private int latency;

}