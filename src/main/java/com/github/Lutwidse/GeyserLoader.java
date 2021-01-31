package russian.vodka.volkswagen.geyserloader;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class GeyserLoader extends JavaPlugin implements Listener {

    private static final String PREFIX = "§a[GeyserLoader] §r";
    private final Server SERVER = this.getServer();
    private final Logger LOGGER = this.getLogger();

    private static final int CHUNK_TIMER_DELAY = 5;
    private static final int CHUNK_TIMER_DELAY_TICKRATE = CHUNK_TIMER_DELAY * 60 * 1000;

    @Override
    public void onEnable() {
        SERVER.getPluginManager().registerEvents(this, this);
        LOGGER.info(PREFIX);
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        String name = p.getName();
        World w = p.getWorld();

        int posX = (int) p.getLocation().getX();
        int posZ = (int) p.getLocation().getZ();

        if (name.contains("*")) {
            if (w.isChunkLoaded(posX, posZ)) {
                LOGGER.info(PREFIX + "{" + name + "}の周辺チャンクは既にロードされています");
                p.sendMessage(PREFIX + "貴方の周辺チャンクは既にロードされています");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {

        /*
        他のプレイヤーによって接続地点のチャンクロードが行われていない場合、
        約1分未満で切断されるため、当該プレイヤー切断後にチャンクロードを実施し解決する
         */

        Player p = e.getPlayer();
        String name = p.getName();
        World w = p.getWorld();

        int posX = (int) p.getLocation().getX();
        int posZ = (int) p.getLocation().getZ();

        // Geyserに於けるクロスプレイ接続時のプレイヤーIDにはアスタリスクの接頭辞が付与される
        if (name.contains("*")) {

            if (w.isChunkLoaded(posX, posZ)) {
                LOGGER.info(PREFIX + "{" + name + "}の周辺チャンクは既にロードされています");
                return;
            }

            LOGGER.info(PREFIX + "クロスプレイによるディスコネクトを感知しました");
            LOGGER.info(PREFIX + "{" + name + "}の周辺チャンクを" + CHUNK_TIMER_DELAY + "分間ロードします");
            p.sendMessage(PREFIX + "周辺チャンクのロードが開始されました");

            w.loadChunk(posX, posX);

            SERVER.getScheduler().runTaskLater(this, () -> {
                w.unloadChunk(posZ, posZ);
                LOGGER.info(PREFIX + "{" + name + "}のチャンクロードを停止しました");
                p.sendMessage(PREFIX + "チャンクロードを停止しました");
            }, CHUNK_TIMER_DELAY_TICKRATE);
        }
    }
}
