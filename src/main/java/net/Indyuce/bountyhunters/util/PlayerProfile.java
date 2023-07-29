package net.Indyuce.bountyhunters.util;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Keeps track of basic player information even when the player is logged off.
 * This is used to keep track of the last log activity timestamp as well as
 * the last username used by the player.
 */
public class PlayerProfile {
    private final UUID uuid;

    private String name;
    private Player player;
    private long lastLogActivity;

    public PlayerProfile(@NotNull OfflinePlayer player) {
        this.uuid = player.getUniqueId();

        update(player);
    }

    public PlayerProfile(@NotNull PlayerProfile profile) {
        this.uuid = profile.uuid;
        this.name = profile.name;
        this.player = profile.player;
        this.lastLogActivity = profile.lastLogActivity;
    }

    /**
     * Used when loading a leaderboard profile from the config
     *
     * @param config Config to read data from
     */
    public PlayerProfile(@NotNull ConfigurationSection config) {
        this.uuid = Objects.requireNonNull(UUID.fromString(config.getName()));
        this.name = Objects.requireNonNull(config.getString("name"));
        this.lastLogActivity = config.getLong("last-log", System.currentTimeMillis());
    }

    public void update(@Nullable OfflinePlayer player) {
        lastLogActivity = System.currentTimeMillis();

        // Avoid memory leak
        if (player == null) {
            this.player = null;
            return;
        }

        this.player = player.isOnline() ? player.getPlayer() : null;
        this.name = player.getName();
    }

    public boolean isOnline() {
        return player != null;
    }

    @NotNull
    public UUID getUniqueId() {
        return uuid;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    public long getLastLogActivity() {
        return lastLogActivity;
    }

    public void save(FileConfiguration config) {
        config.set(uuid + ".name", name);
        config.set(uuid + ".last-log", lastLogActivity);
        whenSaved(config.getConfigurationSection(uuid.toString()));
    }

    public void whenSaved(ConfigurationSection config) {
        // Nothing
    }
}
