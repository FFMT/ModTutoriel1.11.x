package fr.minecraftforgefrance.tutorial.client;

import java.io.File;

import fr.minecraftforgefrance.tutorial.TutorialCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TutorialClient extends TutorialCommon
{
    @Override
    public void preInit(File configFile)
    {
        super.preInit(configFile);
        System.out.println("pre init côté client");
    }

    @Override
    public void init()
    {
        super.init();
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onOpenGui(GuiOpenEvent event)
    {
        if(event.getGui() != null && event.getGui().getClass() == GuiMainMenu.class)
        {
            event.setGui(new GuiCustomMainMenu());
        }
    }
    
    @SubscribeEvent
    public void onClick(PlayerInteractEvent.LeftClickEmpty event)
    {
        if(Minecraft.getMinecraft().objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY)
        {
            event.getEntityPlayer().sendMessage(new TextComponentString("You hit : " + Minecraft.getMinecraft().objectMouseOver.entityHit.getName()));
        }
    }
}