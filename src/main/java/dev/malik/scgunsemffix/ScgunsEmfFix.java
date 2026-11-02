package dev.malik.scgunsemffix;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ScgunsEmfFix.MODID)
public class ScgunsEmfFix {
    public static final String MODID = "scguns_emf_fix";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ScgunsEmfFix(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("ScorchedGuns EMF Compat Perf Fix loaded (caches OptionalClientCompat's reflective EMF lookups, bridges them to EMF 3.x)");
    }
}
