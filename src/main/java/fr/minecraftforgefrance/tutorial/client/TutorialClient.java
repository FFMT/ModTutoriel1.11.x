package fr.minecraftforgefrance.tutorial.client;

import java.io.File;

import fr.minecraftforgefrance.tutorial.TutorialCommon;

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
    }
    
}