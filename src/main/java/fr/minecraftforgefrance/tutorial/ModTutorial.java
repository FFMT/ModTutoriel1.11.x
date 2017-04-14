package fr.minecraftforgefrance.tutorial;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ModTutorial.MODID, name = "Mod Tutorial", version = "0.1", acceptedMinecraftVersions = "[1.11.2]")
public class ModTutorial
{
    public static final String MODID = "tutorial";
    public static Logger logger;
    
    @SidedProxy(clientSide = "fr.minecraftforgefrance.tutorial.client.TutorialClient", serverSide = "fr.minecraftforgefrance.tutorial.TutorialCommon")
    public static TutorialCommon proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        ModTutorial.logger = event.getModLog(); // initialise le logger. event.getModLog() retourne un logger avec votre modid
        ModTutorial.proxy.preInit(event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        ModTutorial.proxy.init();
    }
}
