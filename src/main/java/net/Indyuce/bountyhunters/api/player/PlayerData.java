package net.Indyuce.bountyhunters.api.player;

import net.Indyuce.bountyhunters.BountyHunters;
import net.Indyuce.bountyhunters.api.AltChar;
import net.Indyuce.bountyhunters.api.Bounty;
import net.Indyuce.bountyhunters.api.CustomItem;
import net.Indyuce.bountyhunters.api.event.HunterLevelUpEvent;
import net.Indyuce.bountyhunters.api.language.Language;
import net.Indyuce.bountyhunters.api.language.Message;
import net.Indyuce.bountyhunters.api.player.reward.BountyAnimation;
import net.Indyuce.bountyhunters.api.player.reward.HunterTitle;
import net.Indyuce.bountyhunters.api.player.reward.LevelUpItem;
import net.Indyuce.bountyhunters.manager.PlayerDataManager;
import net.Indyuce.bountyhunters.util.PlayerProfile;
import net.Indyuce.bountyhunters.util.UtilityMethods;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;

public class PlayerData implements OfflinePlayerData {
    private final PlayerProfile profile;

    // Player data that must be saved when the server shuts down
    private int level, successful, claimed, illegalStreak, illegalKills;
    private BountyAnimation animation;
    private HunterTitle title;
    private final List<UUID> redeemHeads = new ArrayList<>();

    // Temp stuff that is not being saved when the server closes
    private long lastBounty, lastTarget, lastSelect;
    private PlayerHunting hunting;

    public PlayerData(@NotNull Player player) {
        this.profile = new PlayerProfile(player);
    }

    @NotNull
    public PlayerProfile getProfile() {
        return profile;
    }

    @NotNull
    public UUID getUniqueId() {
        return profile.getUniqueId();
    }

    @Deprecated
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(getUniqueId());
    }

    /**
     * Will throw an error if the player is offline
     *
     * @return The corresponding player instance.
     */
    @NotNull
    public Player getPlayer() {
        return profile.getPlayer();
    }

    /**
     * Will return a value even if the player is offline. Unlike
     * #getPlayer()#getName(), this method is guaranteed to return
     * a result.
     *
     * @return The last cached player name
     */
    @NotNull
    public String getPlayerName() {
        return profile.getName();
    }

    @Deprecated
    public void updatePlayer(@Nullable Player player) {
        profile.update(player);
    }

    public boolean isOnline() {
        return profile.isOnline();
    }

    public long getLastBounty() {
        return lastBounty;
    }

    public long getLastTarget() {
        return lastTarget;
    }

    public long getLastLogActivity() {
        return profile.getLastLogActivity();
    }

    public int getLevel() {
        return level;
    }

    public int getSuccessfulBounties() {
        return successful;
    }

    public int getClaimedBounties() {
        return claimed;
    }

    public BountyAnimation getAnimation() {
        return animation;
    }

    public HunterTitle getTitle() {
        return title;
    }

    public int getBountiesNeededToLevelUp() {
        int needed = BountyHunters.getInstance().getLevelManager().getBountiesPerLevel();
        return needed - (claimed % needed);
    }

    public String getLevelProgressBar() {
        String advancement = "";
        int needed = BountyHunters.getInstance().getLevelManager().getBountiesPerLevel();
        for (int j = 0; j < needed; j++)
            advancement += (getClaimedBounties() % needed > j ? ChatColor.GREEN : ChatColor.WHITE) + AltChar.square;
        return advancement;
    }

    /**
     * @return Item with no texture, the skull owner is set using
     *         an async task when the inventory is opened otherwise
     *         texture requests can freeze the main server thread.
     */
    public ItemStack getProfileItem() {
        ItemStack profile = CustomItem.PROFILE.toItemStack().clone();
        SkullMeta meta = (SkullMeta) profile.getItemMeta();
        meta.setDisplayName(meta.getDisplayName().replace("{name}", getPlayerName()).replace("{level}", "" + getLevel()));
        List<String> profileLore = meta.getLore();

        String title = hasTitle() ? getTitle().format() : Language.NO_TITLE.format();
        for (int j = 0; j < profileLore.size(); j++)
            profileLore.set(j,
                    profileLore.get(j).replace("{level_progress}", getLevelProgressBar()).replace("{claimed_bounties}", "" + getClaimedBounties())
                            .replace("{successful_bounties}", "" + getSuccessfulBounties()).replace("{current_title}", title)
                            .replace("{level}", "" + getLevel()));

        meta.setLore(profileLore);
        profile.setItemMeta(meta);

        return profile;
    }

    public int getIllegalKillStreak() {
        return illegalStreak;
    }

    public int getIllegalKills() {
        return illegalKills;
    }

    public boolean hasUnlocked(LevelUpItem item) {
        return level >= item.getUnlockLevel();
    }

    public boolean hasAnimation() {
        return animation != null;
    }

    public boolean hasTitle() {
        return title != null;
    }

    public boolean canSelectItem() {
        return lastSelect + 3000 < System.currentTimeMillis();
    }

    public void log(String... message) {
        for (String line : message)
            BountyHunters.getInstance().getLogger().log(Level.WARNING, "[Player Data] " + getPlayerName() + ": " + line);
    }

    public void setLastBounty() {
        lastBounty = System.currentTimeMillis();
    }

    public void setLastTarget() {
        lastTarget = System.currentTimeMillis();
    }

    public void setLastSelect() {
        lastSelect = System.currentTimeMillis();
    }

    public void setLevel(int value) {
        level = Math.max(0, value);
    }

    public void setSuccessfulBounties(int value) {
        successful = Math.max(0, value);
    }

    public void setClaimedBounties(int value) {
        claimed = Math.max(0, value);
    }

    public void setIllegalKills(int value) {
        illegalKills = Math.max(0, value);
    }

    public void setIllegalKillStreak(int value) {
        illegalStreak = Math.max(0, value);
    }

    public void setAnimation(BountyAnimation animation) {
        this.animation = animation;
    }

    public void setTitle(HunterTitle title) {
        this.title = title;
    }

    public void addLevels(int value) {
        setLevel(level + value);
    }

    @Override
    public void addSuccessfulBounties(int value) {
        setSuccessfulBounties(getSuccessfulBounties() + value);
    }

    public void addClaimedBounties(int value) {
        setClaimedBounties(claimed + value);
    }

    public void addIllegalKills(int value) {
        setIllegalKills(illegalKills + value);
        setIllegalKillStreak(illegalStreak + value);
    }

    public void addRedeemableHead(UUID uuid) {
        redeemHeads.add(uuid);
    }

    public void removeRedeemableHead(UUID uuid) {
        redeemHeads.remove(uuid);
    }

    public List<UUID> getRedeemableHeads() {
        return redeemHeads;
    }

    @Override
    public void givePlayerHead(OfflinePlayer owner) {

        if (!isOnline()) {
            redeemHeads.add(owner.getUniqueId());
            return;
        }

        Player player = getPlayer();
        if (player.getInventory().firstEmpty() == -1) {
            redeemHeads.add(owner.getUniqueId());
            Message.MUST_REDEEM_HEAD.format("target", owner.getName()).send(player);
            return;
        }

        player.getInventory().addItem(UtilityMethods.getHead(owner));
        Message.OBTAINED_HEAD.format("target", owner.getName()).send(player);
    }

    public void refreshLevel(Player player) {
        while (levelUp(player))
            ;
    }

    private boolean levelUp(Player player) {
        int nextLevel = getLevel() + 1;
        int neededBounties = nextLevel * BountyHunters.getInstance().getLevelManager().getBountiesPerLevel();
        if (getClaimedBounties() < neededBounties)
            return false;

        Bukkit.getPluginManager().callEvent(new HunterLevelUpEvent(player, nextLevel));
        Message.LEVEL_UP.format("level", nextLevel, "bounties", BountyHunters.getInstance().getLevelManager().getBountiesPerLevel()).send(player);

        final List<String> chatDisplay = new ArrayList<>();

        // Titles
        for (HunterTitle title : BountyHunters.getInstance().getLevelManager().getTitles())
            if (nextLevel == title.getUnlockLevel())
                chatDisplay.add(title.format());

        // Death anims
        for (BountyAnimation anim : BountyHunters.getInstance().getLevelManager().getAnimations())
            if (nextLevel == anim.getUnlockLevel())
                chatDisplay.add(anim.format());

        // Send commands
        if (BountyHunters.getInstance().getLevelManager().hasCommands(nextLevel))
            BountyHunters.getInstance().getLevelManager().getCommands(nextLevel).forEach(
                    cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), BountyHunters.getInstance().getPlaceholderParser().parse(player, cmd)));

        // Money
        final double moneyEarned = BountyHunters.getInstance().getLevelManager().calculateLevelMoney(nextLevel);
        BountyHunters.getInstance().getEconomy().depositPlayer(player, moneyEarned);

        // Send json list
        String jsonList = moneyEarned > 0 ? "\n" + Language.LEVEL_UP_REWARD_MONEY.format("amount", UtilityMethods.format(moneyEarned)) : "";
        for (String s : chatDisplay)
            jsonList += "\n" + Language.LEVEL_UP_REWARD.format("reward", AltChar.apply(s));
        player.spigot().sendMessage(ChatMessageType.CHAT,
                new ComponentBuilder()
                        .append(Language.LEVEL_UP_REWARDS.format())
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(jsonList.substring(1))))
                        .color(net.md_5.bungee.api.ChatColor.YELLOW)
                        .create());

        setLevel(nextLevel);
        return true;
    }

    public boolean isHunting() {
        return hunting != null;
    }

    @Nullable
    public PlayerHunting getHunting() {
        return Objects.requireNonNull(hunting, "Player is not hunting");
    }

    public void setHunting(@NotNull Bounty bounty) {
        Validate.notNull(bounty, "Bounty cannot be null");
        Validate.isTrue(!isHunting(), "Player is already hunting");

        hunting = new PlayerHunting(bounty);
    }

    public void stopHunting() {
        Validate.notNull(hunting, "Player is not hunting");

        // Close and collect garbage
        if (hunting.isCompassActive())
            hunting.disableCompass();
        hunting = null;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof PlayerData && ((PlayerData) object).getUniqueId().equals(getUniqueId());
    }

    @Override
    public String toString() {
        return "{Level=" + level + ", ClaimedBounties=" + claimed + ", SuccessfulBounties=" + successful + ", IllegalKills=" + illegalKills
                + ", IllegalKillStreak=" + illegalStreak + (hasTitle() ? ", Title=" + title.getId() : "")
                + (hasAnimation() ? ", Quote=" + animation.getId() : "") + ", RedeemHeads=" + redeemHeads.toString() + "}";
    }

    /**
     * @deprecated Use {@link PlayerDataManager#getLoaded()} instead
     */
    @Deprecated
    public static Collection<PlayerData> getLoaded() {
        return BountyHunters.getInstance().getPlayerDataManager().getLoaded();
    }

    public static PlayerData get(OfflinePlayer player) {
        return BountyHunters.getInstance().getPlayerDataManager().get(player.getUniqueId());
    }

    /**
     * @deprecated Use {@link PlayerDataManager#login(Player)} instead
     */
    @Deprecated
    public static void load(OfflinePlayer player) {
        Validate.isTrue(player.isOnline(), "Player must be online");
        BountyHunters.getInstance().getPlayerDataManager().login(player.getPlayer());
    }

    public static boolean isLoaded(UUID uuid) {
        return BountyHunters.getInstance().getPlayerDataManager().isLoaded(uuid);
    }

    /**
     * Caution: this method does NOT save any of the player data. You MUST save
     * the player data using saveFile() before unloading the player data from
     * the map.
     *
     * @deprecated Use {@link PlayerDataManager#unload(UUID)} instead
     */
    @Deprecated
    public void unload() {
        BountyHunters.getInstance().getPlayerDataManager().unload(getUniqueId());
    }
}
