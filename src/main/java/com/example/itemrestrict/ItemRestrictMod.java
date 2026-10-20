package com.example.itemrestrict;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ItemRestrictMod.MOD_ID)
public final class ItemRestrictMod {
    public static final String MOD_ID = "itemrestrict";

    public ItemRestrictMod() {
        NeoForge.EVENT_BUS.register(ItemRestrictEvents.class);
        NeoForge.EVENT_BUS.addListener(ItemRestrictCommands::register);
    }
}
