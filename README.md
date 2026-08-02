# WDMla
**What DreamMaster looks at** is what you look at<br>
The current state of the mod:<br>
![buggy](image/buggy.png)

# Main Features
- Allows rich non-text expression like item, entity, progress bar
- 3D block and entity icons!

![smeltery.gif](image/smeltery.gif)

- Backported many code features from Jade mod including new and fast registration system
- 2D layout codebase makes layout creation more flexible (was using ones from The One Probe, slowly rewriting to original)
- Better config screen and options
- Includes reimplemented WailaHarvestability and WAWLA features
- Includes the WAILAPlugins integrations used by GTNH: Blood Magic, Forestry/MagicBees, Harvestcraft, IC2 Crops, and Railcraft
- Uses WDMla's native textured gauges for fluid storage and progress instead of the old WAILAPlugins text renderer

![water.png](image/water.png)

- Achieved almost full backward compatibility by keeping legacy Waila api

# Supported mods
- almost every mods that Waila supports
- almost every mods that supports Waila
- And more...?

# Install step
Remove

- Waila (bundled)
- Waila Harvestability (bundled)
- WAWLA (bundled)
- WAILAPlugins (the GTNH integrations are bundled)

from your mods folder, then throw [GTNHLib](https://github.com/GTNewHorizons/GTNHLib/releases/latest) and this mod into it<br>
Your Waila config will be applied to WDMla automatically.<br>
No, NEI is no longer required

# Standalone compatibility
- WDMla remains the only Waila implementation JAR required by the client or server.
- The legacy `Waila` mod ID and `mcp.mobius.waila` API packages remain available for binary compatibility.
- Do not install Waila, WAWLA, WailaHarvestability, or WAILAPlugins alongside WDMla; their active providers are integrated here.

# Other items to Note:
- Enchant Screen has been removed
- NEI ore dictionary search function has been removed (GTNH NEI has this by default)

# Credits
- [GTNH WAWLA](https://github.com/GTNewHorizons/Wawla)
  - WAWLA features were reimplemented for WDMla's native component system under the [LGPL-2.1 license](https://github.com/GTNewHorizons/Wawla/blob/master/LICENSE)
- [GTNH WAILAPlugins](https://github.com/GTNewHorizons/WAILAPlugins)
  - The GTNH-used providers were adapted and substantially rewritten for WDMla gauges and layouts under the [CC BY-NC-SA 4.0 license](https://github.com/GTNewHorizons/WAILAPlugins/blob/master/LICENSE)
- [Jade](https://github.com/Snownee/Jade) 
  - Backported many codes under the same license
- [The One Probe](https://github.com/McJtyMods/TheOneProbe)
  - Backported some HUD component codes under [MIT license](https://github.com/McJtyMods/TheOneProbe/blob/1.20/LICENCE)
- [Refined Storage 2](https://github.com/refinedmods/refinedstorage2)
  - Uses some ui icons under [MIT license](https://github.com/refinedmods/refinedstorage2/blob/develop/LICENSE.md)

How far did we progress?: https://github.com/Quarri6343/Wdmla/compare/2f738bc...master
