package com.gtnewhorizons.wdmla.plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.discovery.ASMDataTable.ASMData;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.utils.WailaExceptionHandler;

public enum PluginScanner {

    INSTANCE;

    public final List<IWDMlaPlugin> results = Lists.newArrayList();

    public final List<String> blackListedRegistrationMethods = Lists.newArrayList();

    @SuppressWarnings("unchecked")
    public void scan(FMLPreInitializationEvent event) {
        Set<ASMData> datas = event.getAsmData().getAll(WDMlaPlugin.class.getName());
        for (ASMData data : datas) {
            Map<String, Object> annotationInfo = data.getAnnotationInfo();
            String uid = (String) annotationInfo.get("uid");
            if (Strings.isNullOrEmpty(uid)) {
                uid = null;
            }
            List<String> deps = (List<String>) annotationInfo.get("dependencies");
            if (!allModsLoaded(deps)) {
                Waila.log.info(String.format("skipped plugin %s loading: missing dependency", uid));
                continue;
            }
            List<String> excludedDeps = (List<String>) annotationInfo.get("excludedDependencies");
            if (anyModsLoaded(excludedDeps)) {
                Waila.log.info(String.format("skipped plugin %s loading: excluded dependency loaded", uid));
                continue;
            }
            String overridingMethod = (String) annotationInfo.get("overridingRegistrationMethodName");
            if (!Strings.isNullOrEmpty(overridingMethod)) {
                blackListedRegistrationMethods.add(overridingMethod);
            }

            try {
                Class<?> clazz = Class.forName(data.getClassName());
                if (IWDMlaPlugin.class.isAssignableFrom(clazz)) {
                    try {
                        IWDMlaPlugin inst = (IWDMlaPlugin) clazz.getDeclaredConstructor().newInstance();
                        results.add(inst);
                    } catch (IllegalAccessException | InstantiationException | InvocationTargetException
                            | NoSuchMethodException e) {
                        WailaExceptionHandler.handleErr(e, clazz.getName(), null);
                    }
                } else {
                    Waila.log.info("skipped plugin %s loading: class is not IWDMlaPlugin", uid);
                }
            } catch (ClassNotFoundException e) {
                WailaExceptionHandler.handleErr(e, this.getClass().getName(), null);
            }
        }
    }

    private boolean allModsLoaded(Iterable<String> mods) {
        if (mods == null) {
            return true;
        }
        for (String s : mods) {
            if (!Loader.isModLoaded(s)) {
                return false;
            }
        }
        return true;
    }

    /** Returns whether at least one listed mod is loaded. */
    private boolean anyModsLoaded(Iterable<String> mods) {
        if (mods == null) {
            return false;
        }
        for (String mod : mods) {
            if (Loader.isModLoaded(mod)) {
                return true;
            }
        }
        return false;
    }
}
