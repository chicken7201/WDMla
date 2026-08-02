package com.gtnewhorizons.wdmla.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.gtnewhorizons.wdmla.api.provider.IWDMlaProvider;

/**
 * The plugin annotating this must implement {@link IWDMlaPlugin}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WDMlaPlugin {

    /**
     * The uid of the plugin.<br>
     * don't get confused with {@link IWDMlaProvider#getUid()}. There are no direct association between them (for now)
     */
    String uid() default "";

    /**
     * Mod ID dependencies of the plugin
     */
    String[] dependencies() default {};

    /**
     * Mod IDs whose presence prevents this plugin from loading.
     */
    String[] excludedDependencies() default {};

    /**
     * If the config is enabled, WDMla will disable this registration method provided by IMC message from the mod,<br>
     * to avoid duplicated Waila tooltips implementation registered.
     */
    String overridingRegistrationMethodName() default "";
}
