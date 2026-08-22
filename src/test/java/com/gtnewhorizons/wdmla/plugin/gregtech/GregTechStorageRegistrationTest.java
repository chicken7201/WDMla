package com.gtnewhorizons.wdmla.plugin.gregtech;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import com.gtnewhorizons.wdmla.api.provider.IWDMlaProvider;
import com.gtnewhorizons.wdmla.impl.PriorityStore;
import com.gtnewhorizons.wdmla.impl.lookup.HierarchyLookup;
import com.gtnewhorizons.wdmla.plugin.universal.FluidStorageProvider;

class GregTechStorageRegistrationTest {

    /** Verifies that the digital tank packet key is unique and has a matching client decoder. */
    @Test
    void digitalTankUidIsUniqueAndClientDecodable() {
        assertNotEquals(
                FluidStorageProvider.Extension.INSTANCE.getUid(),
                GregTechDigitalTankStorageProvider.DIGITAL_TANK_STORAGE);
        assertEquals(
                GregTechDigitalTankStorageProvider.DIGITAL_TANK_STORAGE,
                GregTechDigitalTankStorageProvider.ClientExtension.INSTANCE.getUid());
    }

    /** Verifies that specialized and fallback registrations pass the retained UID uniqueness check. */
    @Test
    void specializedAndFallbackProvidersPassHierarchyValidation() {
        HierarchyLookup<IWDMlaProvider> lookup = new HierarchyLookup<>(Object.class);
        lookup.register(Object.class, GregTechDigitalTankStorageProvider.ClientExtension.INSTANCE);
        lookup.register(Object.class, FluidStorageProvider.Extension.INSTANCE);
        PriorityStore<net.minecraft.util.ResourceLocation, IWDMlaProvider> priorities =
                new PriorityStore<>(IWDMlaProvider::getDefaultPriority, IWDMlaProvider::getUid);

        assertDoesNotThrow(() -> lookup.loadComplete(priorities));
    }
}
