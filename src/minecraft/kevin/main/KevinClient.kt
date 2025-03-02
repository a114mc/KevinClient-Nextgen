/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package kevin.main

//import kevin.module.modules.render.Renderer
import cn.a114.skid.AudioManager
import kevin.cape.CapeManager
import kevin.command.CommandManager
import kevin.command.bind.BindCommandManager
import kevin.event.ClientShutdownEvent
import kevin.event.EventManager
import kevin.file.ConfigManager
import kevin.file.FileManager
import kevin.file.ImageManager
import kevin.file.ResourceManager
import kevin.font.FontGC
import kevin.font.FontManager
import kevin.hud.HUD
import kevin.hud.HUD.Companion.createDefault
import kevin.module.ModuleManager
import kevin.module.modules.misc.ConfigsManager
import kevin.module.modules.render.ClickGui.ClickGUI
import kevin.module.modules.render.ClickGui.NewClickGui
import kevin.persional.milk.guis.clickgui.MilkClickGui
import kevin.plugin.PluginManager
import kevin.script.ScriptLoader
import kevin.skin.SkinManager
import kevin.utils.CombatManager
import kevin.utils.RotationUtils
import kevin.via.ViaVersion
import net.minecraft.client.Minecraft.logger
import org.lwjgl.opengl.Display
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.lwjgl.Sys

object KevinClient {
    var name = "Kevin"
    var version = "u-FINAL" // GG

    var isStarting = true


    val debug = true
    private var wasStop = false
    val lwjglversion = /*org.lwjgl.Sys.getVersion()!!*/"2.9.4";


    lateinit var moduleManager: ModuleManager
    lateinit var fileManager: FileManager
    lateinit var eventManager: EventManager
    lateinit var commandManager: CommandManager
    lateinit var fontManager: FontManager
    lateinit var clickGUI: ClickGUI
    lateinit var newClickGui: NewClickGui
    lateinit var milkClickGui: MilkClickGui
    lateinit var hud: HUD
    lateinit var capeManager: CapeManager
    lateinit var combatManager: CombatManager
    lateinit var audioManager: AudioManager
//    lateinit var audioPlayer: AudioPlayer
    @JvmStatic
    val pool: ExecutorService = Executors.newCachedThreadPool()

    var cStart = "[Kevin]"

    fun run() {

        moduleManager = ModuleManager()
        fileManager = FileManager()
        fileManager.load()
        commandManager = CommandManager()
        eventManager = EventManager()
        fontManager = FontManager()
        fontManager.loadFonts()
        eventManager.registerListener(FontGC)
//        Renderer.load()
        try {
            moduleManager.load()
        }
        catch (e: Exception) {
            logger.error("Error while loading moduleManager\n$e")
        }
        ScriptLoader.load()
        PluginManager.initialize()

        fileManager.loadConfig(fileManager.modulesConfig)
        fileManager.loadConfig(fileManager.bindCommandConfig)

        eventManager.registerListener(BindCommandManager)
        eventManager.registerListener(RotationUtils())
        audioManager = AudioManager()
        hud = createDefault()

        fileManager.loadConfig(fileManager.hudConfig)

        commandManager.load()
        clickGUI = ClickGUI()
        newClickGui = NewClickGui()
        milkClickGui = MilkClickGui()

        capeManager = CapeManager()

        capeManager.load()

        SkinManager.load()
        ImageManager.load()
        ResourceManager.init()
        ConfigManager.load()
        ConfigsManager.updateValue()

        combatManager = CombatManager()

        ViaVersion.start()
        PluginManager.initialize()

        Display.setTitle("Minecraft 1.8.9 | " + name + ' ' + version + " | LWJGL Version " + lwjglversion)

        isStarting = false
        // ?!
        Runtime.getRuntime().addShutdownHook(Thread(/* target = */ ::stop))
        logger.info("Client loaded, enjoy the hack!")
        Thread{ Thread.sleep(1500);PenisAudioPlayer().play() }.start()


    }

    fun stop() {
        if (wasStop) return
        wasStop = true
        eventManager.callEvent(ClientShutdownEvent())
        fileManager.saveAllConfigs()
        capeManager.save()
        SkinManager.save()
        ImageManager.save()
        val env = System.getenv("TEMP")
        try {
            Files.walk(Paths.get(env)).use {
                it.forEachOrdered { p: Path ->
                    val file = p.toFile()
                    val name = file.getName()
                    if (name.endsWith(".tmp") && name.contains("+~JF")) {
                        file.deleteOnExit()
                    }
                }
            }
        } catch (_: Exception) {}
    }
}