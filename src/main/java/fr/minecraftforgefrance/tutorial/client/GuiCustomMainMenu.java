package fr.minecraftforgefrance.tutorial.client;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GLContext;

import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import fr.minecraftforgefrance.tutorial.ModTutorial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonLanguage;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.ServerPinger;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.client.FMLClientHandler;

public class GuiCustomMainMenu extends GuiScreen
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new Random();

    /** The splash message. */
    private String splashText;
    private GuiButton buttonResetDemo;
    /** Timer used to rotate the panorama, increases every tick. */
    private int panoramaTimer;
    /** Texture allocated for the current viewport of the main menu's panorama background. */
    private DynamicTexture viewportTexture;
    /** The Object object utilized as a thread lock when performing non thread-safe operations */
    private final Object threadLock = new Object();
    public static final String MORE_INFO_TEXT = "Please click " + TextFormatting.UNDERLINE + "here" + TextFormatting.RESET + " for more information.";
    /** Width of openGLWarning2 */
    private int openGLWarning2Width;
    /** Width of openGLWarning1 */
    private int openGLWarning1Width;
    /** Left x coordinate of the OpenGL warning */
    private int openGLWarningX1;
    /** Top y coordinate of the OpenGL warning */
    private int openGLWarningY1;
    /** Right x coordinate of the OpenGL warning */
    private int openGLWarningX2;
    /** Bottom y coordinate of the OpenGL warning */
    private int openGLWarningY2;
    /** OpenGL graphics card warning. */
    private String openGLWarning1;
    /** OpenGL graphics card warning. */
    private String openGLWarning2;
    /** Link to the Mojang Support about minimum requirements */
    private String openGLWarningLink;
    private static final ResourceLocation SPLASH_TEXTS = new ResourceLocation("texts/splashes.txt");
    // titre personnalisé :
    private static final ResourceLocation MFF_TITLE = new ResourceLocation(ModTutorial.MODID, "textures/gui/mff_title.png");
    // arrière plan personnalisé :
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(ModTutorial.MODID, "textures/gui/background.png");
    
    private ResourceLocation backgroundTexture;
    private GuiButton modButton;

    // début information serveur
    private final ServerPinger serverPinger = new ServerPinger();
    private ServerData server = new ServerData("localserver", "localhost:25565", false);
    private static final ThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());
    // fin information serveur
    
    // début texte defilant
    private int scrollingTextPosX;
    private String scrollingText;
    // fin texte defilant
    
    public GuiCustomMainMenu()
    {
        // début texte defilant
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    URL url = new URL("http://dl.mcnanotech.fr/mff/tutoriels/mainmenutext.txt");
                    InputStream is = url.openStream();
                    GuiCustomMainMenu.this.scrollingText = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
                }
                catch(Exception e)
                {
                    GuiCustomMainMenu.this.scrollingText = "Impossible de lire le texte";
                }
            }
        }.start();
        // fin texte defilant

        
        FMLClientHandler.instance().setupServerList();
        this.openGLWarning2 = MORE_INFO_TEXT;
        this.splashText = "missingno";
        IResource iresource = null;

        try
        {
            List<String> list = Lists.<String>newArrayList();
            iresource = Minecraft.getMinecraft().getResourceManager().getResource(SPLASH_TEXTS);
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8));
            String s;

            while ((s = bufferedreader.readLine()) != null)
            {
                s = s.trim();

                if (!s.isEmpty())
                {
                    list.add(s);
                }
            }

            if (!list.isEmpty())
            {
                while (true)
                {
                    this.splashText = (String)list.get(RANDOM.nextInt(list.size()));

                    if (this.splashText.hashCode() != 125780783)
                    {
                        break;
                    }
                }
            }
        }
        catch (IOException var8)
        {
            ;
        }
        finally
        {
            IOUtils.closeQuietly((Closeable)iresource);
        }

        this.openGLWarning1 = "";

        if (!GLContext.getCapabilities().OpenGL20 && !OpenGlHelper.areShadersSupported())
        {
            this.openGLWarning1 = I18n.format("title.oldgl1", new Object[0]);
            this.openGLWarning2 = I18n.format("title.oldgl2", new Object[0]);
            this.openGLWarningLink = "https://help.mojang.com/customer/portal/articles/325948?ref=game";
        }

        String s1 = System.getProperty("java.version");

        if (s1 != null && (s1.startsWith("1.6") || s1.startsWith("1.7")))
        {
            this.openGLWarning1 = I18n.format("title.oldjava1", new Object[0]);
            this.openGLWarning2 = I18n.format("title.oldjava2", new Object[0]);
            this.openGLWarningLink = "https://help.mojang.com/customer/portal/articles/2636196?ref=game";
        }
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        ++this.panoramaTimer;
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        this.viewportTexture = new DynamicTexture(256, 256);
        this.backgroundTexture = this.mc.getTextureManager().getDynamicTextureLocation("background", this.viewportTexture);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        if (calendar.get(2) + 1 == 12 && calendar.get(5) == 24)
        {
            this.splashText = "Merry X-mas!";
        }
        else if (calendar.get(2) + 1 == 1 && calendar.get(5) == 1)
        {
            this.splashText = "Happy new year!";
        }
        else if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31)
        {
            this.splashText = "OOoooOOOoooo! Spooky!";
        }

        int j = this.height / 4 + 48;

        if (this.mc.isDemo())
        {
            this.addDemoButtons(j, 24);
        }
        else
        {
            this.addSingleplayerMultiplayerButtons(j, 24);
        }

        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, j + 72 + 12, 98, 20, I18n.format("menu.options", new Object[0])));
        this.buttonList.add(new GuiButton(4, this.width / 2 + 2, j + 72 + 12, 98, 20, I18n.format("menu.quit", new Object[0])));
        this.buttonList.add(new GuiButtonLanguage(5, this.width / 2 - 124, j + 72 + 12));
        this.buttonList.add(new GuiButtonDiscord(21, this.width / 2 - 124, j + 24));

        synchronized (this.threadLock)
        {
            this.openGLWarning1Width = this.fontRenderer.getStringWidth(this.openGLWarning1);
            this.openGLWarning2Width = this.fontRenderer.getStringWidth(this.openGLWarning2);
            int k = Math.max(this.openGLWarning1Width, this.openGLWarning2Width);
            this.openGLWarningX1 = (this.width - k) / 2;
            this.openGLWarningY1 = ((GuiButton)this.buttonList.get(0)).yPosition - 24;
            this.openGLWarningX2 = this.openGLWarningX1 + k;
            this.openGLWarningY2 = this.openGLWarningY1 + 24;
        }

        this.mc.setConnectedToRealms(false);
    }

    /**
     * Adds Singleplayer and Multiplayer buttons on Main Menu for players who have bought the game.
     */
    private void addSingleplayerMultiplayerButtons(int yPos, int yInterval)
    {
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, yPos, I18n.format("menu.singleplayer")));
        //this.buttonList.add(new GuiButton(2, this.width / 2 - 100, yPos + yInterval * 1, I18n.format("menu.multiplayer", new Object[0])));
        this.buttonList.add(new GuiButton(20, this.width / 2 - 100, yPos + yInterval * 1, I18n.format("menu.localserver")));
        this.buttonList.add(modButton = new GuiButton(6, this.width / 2 - 100, yPos + yInterval * 2, I18n.format("fml.menu.mods")));
    }

    /**
     * Adds Demo buttons on Main Menu for players who are playing Demo.
     */
    private void addDemoButtons(int p_73972_1_, int p_73972_2_)
    {
        this.buttonList.add(new GuiButton(11, this.width / 2 - 100, p_73972_1_, I18n.format("menu.playdemo", new Object[0])));
        this.buttonResetDemo = this.addButton(new GuiButton(12, this.width / 2 - 100, p_73972_1_ + p_73972_2_ * 1, I18n.format("menu.resetdemo", new Object[0])));
        ISaveFormat isaveformat = this.mc.getSaveLoader();
        WorldInfo worldinfo = isaveformat.getWorldInfo("Demo_World");

        if (worldinfo == null)
        {
            this.buttonResetDemo.enabled = false;
        }
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        }

        if (button.id == 5)
        {
            this.mc.displayGuiScreen(new GuiLanguage(this, this.mc.gameSettings, this.mc.getLanguageManager()));
        }

        if (button.id == 1)
        {
            this.mc.displayGuiScreen(new GuiWorldSelection(this));
        }

        if (button.id == 2)
        {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        }

        if (button.id == 4)
        {
            this.mc.shutdown();
        }

        if (button.id == 6)
        {
            this.mc.displayGuiScreen(new net.minecraftforge.fml.client.GuiModList(this));
        }

        if (button.id == 11)
        {
            this.mc.launchIntegratedServer("Demo_World", "Demo_World", DemoWorldServer.DEMO_WORLD_SETTINGS);
        }

        if (button.id == 12)
        {
            ISaveFormat isaveformat = this.mc.getSaveLoader();
            WorldInfo worldinfo = isaveformat.getWorldInfo("Demo_World");

            if (worldinfo != null)
            {
                this.mc.displayGuiScreen(new GuiYesNo(this, I18n.format("selectWorld.deleteQuestion", new Object[0]), "\'" + worldinfo.getWorldName() + "\' " + I18n.format("selectWorld.deleteWarning", new Object[0]), I18n.format("selectWorld.deleteButton", new Object[0]), I18n.format("gui.cancel", new Object[0]), 12));
            }
        }
        
        if(button.id == 20)
        {
            FMLClientHandler.instance().connectToServer(this, new ServerData("localserver", "127.0.0.1:25565", false));
            /*
             * on peut aussi utiliser :
            FMLClientHandler.instance().connectToServer(this, new ServerData("localserver", "localhost:25565", false));
             * comme les noms de domaine fonctionnent aussi
             */
        }
        
        if(button.id == 21)
        {
            try
            {
                Desktop.getDesktop().browse(new URI("https://discordapp.com/invite/0uXCYNHVWGM8sAYS"));
            }
            catch(URISyntaxException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void confirmClicked(boolean result, int id)
    {
        if (result && id == 12)
        {
            ISaveFormat isaveformat = this.mc.getSaveLoader();
            isaveformat.flushCache();
            isaveformat.deleteWorldDirectory("Demo_World");
            this.mc.displayGuiScreen(this);
        }
        else if (id == 12)
        {
            this.mc.displayGuiScreen(this);
        }
        else if (id == 13)
        {
            if (result)
            {
                try
                {
                    Class<?> oclass = Class.forName("java.awt.Desktop");
                    Object object = oclass.getMethod("getDesktop", new Class[0]).invoke((Object)null, new Object[0]);
                    oclass.getMethod("browse", new Class[] {URI.class}).invoke(object, new Object[] {new URI(this.openGLWarningLink)});
                }
                catch (Throwable throwable)
                {
                    LOGGER.error("Couldn\'t open link", throwable);
                }
            }

            this.mc.displayGuiScreen(this);
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        // début de l'affichage de l'arrière plan
        mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawScaledCustomSizeModalRect(0, 0, 0, 0, 1, 1, this.width, this.height, 1, 1);
        GlStateManager.enableAlpha();
        // fin de l'affichage de l'arrière plan
        
        // début de l'affichage du titre
        this.mc.getTextureManager().bindTexture(MFF_TITLE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend(); // pour la semi-transparence
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA); // pour la semi-transparence
        Gui.drawScaledCustomSizeModalRect(this.width / 2 - 150, 25, 0, 0, 1, 1, 300, 60, 1, 1);
        GlStateManager.disableBlend(); // pour la semi-transparence
        // fin de l'affichage du titre
        
        // début info serveur
        if(!this.server.pinged)
        {
            this.server.pinged = true;
            this.server.pingToServer = -2L;
            this.server.serverMOTD = "";
            this.server.populationInfo = "";
            EXECUTOR.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        GuiCustomMainMenu.this.serverPinger.ping(GuiCustomMainMenu.this.server);
                    }
                    catch(UnknownHostException unknowHostException)
                    {
                        GuiCustomMainMenu.this.server.pingToServer = -1L;
                        GuiCustomMainMenu.this.server.serverMOTD = TextFormatting.DARK_RED + "Impossible de résoudre le nom d'hôte";
                        // on peut aussi utiliser I18n pour passer par les fichiers de langage
                    }
                    catch(Exception exception)
                    {
                        GuiCustomMainMenu.this.server.pingToServer = -1L;
                        GuiCustomMainMenu.this.server.serverMOTD = TextFormatting.DARK_RED + "Impossible de se connecter au serveur";
                    }
                }
            });
        }
        if(this.server.pingToServer >= 0L)
        {
            this.drawString(this.fontRenderer, this.server.populationInfo + TextFormatting.RESET + " joueurs", this.width / 2 + 110, this.height / 4 + 72, 0x245791);
            this.drawString(this.fontRenderer, this.server.pingToServer + " ms", this.width / 2 + 110, this.height / 4 + 82, 0x245791);
            if(this.server.playerList != null && !this.server.playerList.isEmpty())
            {
                List<String> list = this.mc.fontRenderer.listFormattedStringToWidth(this.server.playerList, this.width - (this.width / 2 + 110));
                for(int i = 0;i < list.size(); i++)
                {
                    if(i >= 10)
                    {
                        break;
                    }
                    this.drawString(this.fontRenderer, list.get(i), this.width / 2 + 110, this.height / 4 + 92 + 10 * i, 0x245791);
                }
            }
            this.drawCenteredString(this.fontRenderer, this.server.serverMOTD, this.width / 2, this.height / 4 + 24, 0x245791);
        }
        else
        {
            this.drawString(this.fontRenderer, this.server.serverMOTD, this.width / 2 + 110, this.height / 4 + 72, 0x245791);
        }
        // fin info serveur
        
        // début texte défilant
        this.scrollingTextPosX --;
        if(this.scrollingTextPosX < -this.fontRenderer.getStringWidth(this.scrollingText))
        {
            this.scrollingTextPosX = this.width;
        }
        this.drawRect(0, 0, this.width, 12, 0x77FFFFFF);
        this.fontRenderer.drawString(this.scrollingText, this.scrollingTextPosX, 2, 0x245791);
        // fin texte défilant

        GlStateManager.pushMatrix();
        GlStateManager.translate((float)(this.width / 2 + 90), 70.0F, 0.0F);
        GlStateManager.rotate(-20.0F, 0.0F, 0.0F, 1.0F);
        float f = 1.8F - MathHelper.abs(MathHelper.sin((float)(Minecraft.getSystemTime() % 1000L) / 1000.0F * ((float)Math.PI * 2F)) * 0.1F);
        f = f * 100.0F / (float)(this.fontRenderer.getStringWidth(this.splashText) + 32);
        GlStateManager.scale(f, f, f);
        this.drawCenteredString(this.fontRenderer, this.splashText, 0, 0, -256); // affichage du slash texte. Il peut être intéressant d'adapter la position
        GlStateManager.popMatrix();
        String s = "Minecraft 1.11.2";

        if (this.mc.isDemo())
        {
            s = s + " Demo";
        }
        else
        {
            s = s + ("release".equalsIgnoreCase(this.mc.getVersionType()) ? "" : "/" + this.mc.getVersionType());
        }

        java.util.List<String> brandings = com.google.common.collect.Lists.reverse(net.minecraftforge.fml.common.FMLCommonHandler.instance().getBrandings(true));
        for (int brdline = 0; brdline < brandings.size(); brdline++)
        {
            String brd = brandings.get(brdline);
            if (!com.google.common.base.Strings.isNullOrEmpty(brd))
            {
                this.drawString(this.fontRenderer, brd, 2, this.height - ( 10 + brdline * (this.fontRenderer.FONT_HEIGHT + 1)), 16777215);
            }
        }
        String s1 = "Copyright Mojang AB. Do not distribute!";
        this.drawString(this.fontRenderer, "Copyright Mojang AB. Do not distribute!", this.width - this.fontRenderer.getStringWidth("Copyright Mojang AB. Do not distribute!") - 2, this.height - 10, -1);

        if (this.openGLWarning1 != null && !this.openGLWarning1.isEmpty())
        {
            drawRect(this.openGLWarningX1 - 2, this.openGLWarningY1 - 2, this.openGLWarningX2 + 2, this.openGLWarningY2 - 1, 1428160512);
            this.drawString(this.fontRenderer, this.openGLWarning1, this.openGLWarningX1, this.openGLWarningY1, -1);
            this.drawString(this.fontRenderer, this.openGLWarning2, (this.width - this.openGLWarning2Width) / 2, ((GuiButton)this.buttonList.get(0)).yPosition - 12, -1);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        synchronized (this.threadLock)
        {
            if (!this.openGLWarning1.isEmpty() && !StringUtils.isNullOrEmpty(this.openGLWarningLink) && mouseX >= this.openGLWarningX1 && mouseX <= this.openGLWarningX2 && mouseY >= this.openGLWarningY1 && mouseY <= this.openGLWarningY2)
            {
                GuiConfirmOpenLink guiconfirmopenlink = new GuiConfirmOpenLink(this, this.openGLWarningLink, 13, true);
                guiconfirmopenlink.disableSecurityWarning();
                this.mc.displayGuiScreen(guiconfirmopenlink);
            }
        }

        net.minecraftforge.client.ForgeHooksClient.mainMenuMouseClick(mouseX, mouseY, mouseButton, this.fontRenderer, this.width);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {

    }
}