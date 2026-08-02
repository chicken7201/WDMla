package com.gtnewhorizons.wdmla.plugin.wawla;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;

import com.gtnewhorizons.wdmla.api.IWDMlaClientRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;

/** Registers WAWLA's Morph player disguise support when Morph is loaded. */
@WDMlaPlugin(uid = "wawla_morph", dependencies = "Morph")
public class MorphWawlaPlugin implements IWDMlaPlugin {

    /** Registers the client header override for other players. */
    @Override
    public void registerClient(IWDMlaClientRegistration registration) {
        registration.registerEntityComponent(MorphWawlaProvider.INSTANCE, EntityOtherPlayerMP.class);
    }

    /** Registers Morph state synchronization for players. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        registration.registerEntityDataProvider(MorphWawlaProvider.INSTANCE, EntityPlayer.class);
    }
}
