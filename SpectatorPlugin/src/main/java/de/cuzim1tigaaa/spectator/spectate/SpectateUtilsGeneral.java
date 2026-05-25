package de.cuzim1tigaaa.spectator.spectate;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.util.SchedulerUtils;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;

@Getter
public class SpectateUtilsGeneral {

	private final Spectator plugin;
	private final SpectateAPI spectateAPI;

	private final Map<UUID, Location> spectateStartLocation = new HashMap<>();
	private final Map<UUID, CycleTask> spectateCycle;
	private final PendingTeleports pendingTeleports;

	public SpectateUtilsGeneral(SpectateAPI spectateAPI) {
		this.plugin = spectateAPI.getPlugin();
		this.spectateAPI = spectateAPI;

		this.spectateCycle = new HashMap<>();
		this.pendingTeleports = new PendingTeleports(plugin);

		this.run();
	}

	private void run() {
		SchedulerUtils.runGlobalTimer(plugin, () ->
				new ArrayList<>(spectateAPI.getSpectators()).forEach(spectator ->
						spectator.getScheduler().run(plugin, st -> {
							SpectateInformation info = spectateAPI.getSpectateInfo(spectator);
							if(info == null) {
								spectator.setGameMode(GameMode.SURVIVAL);
								return;
							}

							if(info.getTarget() == null)
								return;

							Player target = info.getTarget();

							plugin.getInventory().getTargetInventory(spectator, target);

							if(spectator.getSpectatorTarget() == null)
								spectateAPI.setRelation(spectator, target);

							if(!spectator.getWorld().equals(target.getWorld())
									|| spectator.getLocation().distanceSquared(target.getLocation()) > 10) {
								spectateAPI.dismount(spectator);
								spectator.teleportAsync(target.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
							}
							spectateAPI.setRelation(spectator, target);
						}, null)), 1, 15);
	}


	public void spectate(Player spectator, Player target) {
		spectateAPI.getSpectatorsOf(spectator).forEach(spectateAPI::dismount);

		SpectateInformation info;
		if(spectateAPI.isSpectator(spectator)) {
			spectateAPI.dismount(spectator);
			info = spectateAPI.getSpectateInfo(spectator);
			info.setTarget(target);
		}else {
			info = new SpectateInformation(spectator, target);
			getSpectateStartLocation().put(spectator.getUniqueId(), spectator.getLocation());
		}

		spectateAPI.toggleTabList(spectator, true);

		if(!info.getAttributes().containsKey(spectator.getWorld()))
			info.saveAttributes();

		spectateAPI.hideArmorstands(spectator);
		boolean sameWorld = target == null || spectator.getWorld().equals(target.getWorld());

		if(target != null)
			SchedulerUtils.runEntity(plugin, spectator, () ->
					spectator.teleportAsync(target.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN));

		spectator.setGameMode(GameMode.SPECTATOR);
		spectateAPI.getSpectateInfo().add(info);

		if(sameWorld) {
			Spectator.debug("Spectator is in same world as target");
			spectateAPI.setRelation(spectator, target);
		}else {
			Spectator.debug("Spectator is in different world than target");
			SchedulerUtils.runEntity(plugin, spectator, () ->
					spectator.teleportAsync(target.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN));
			spectateAPI.dismount(spectator);
			SchedulerUtils.runEntityLater(plugin, spectator, () ->
					spectateAPI.setRelation(spectator, target), 15L);
		}

		plugin.getInventory().getTargetInventory(spectator, target);
		notifyTarget(target, spectator, true);
	}

	public void unspectate(Player spectator, boolean oldLocation) {
		SpectateInformation info = spectateAPI.getSpectateInfo(spectator);
		if(info == null)
			return;

		if(info.getState() == SpectateState.CYCLING)
			spectateAPI.getSpectateCycle().stopCycle(spectator);

		spectateAPI.dismount(spectator);
		spectateAPI.showArmorstands(spectator);

		Location location = spectateStartLocation.getOrDefault(spectator.getUniqueId(), spectator.getLocation());
		if(!oldLocation || !Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_LOCATION)) {
			Spectator.debug(String.format("Saved Location: %s", location));
			Spectator.debug("Using current location of player");
			location = spectator.getLocation();
		}
		final Location finalLocation = location;

		if(!Objects.equals(spectator.getWorld(), location.getWorld()))
			info.restoreAttributes(true);

		try {
			SchedulerUtils.runEntity(plugin, spectator, () -> {
				if(spectator.isOnline()) {
					spectator.teleportAsync(finalLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
					return;
				}
				this.pendingTeleports.save(spectator.getUniqueId(), finalLocation);
			}, () -> this.pendingTeleports.save(spectator.getUniqueId(), finalLocation));
		}catch(Exception e) {
			// triggered during server shutdown — teleportAsync may not be available
			spectator.teleportAsync(finalLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
		}

		spectateAPI.toggleTabList(spectator, false);
		spectateAPI.getSpectateInfo().remove(info);
		plugin.getInventory().resetInventory(spectator);
		info.restoreAttributes(true);
	}

	public void restore() {
		spectateAPI.getSpectators().forEach(p ->
				unspectate(p, false));
	}

	public void notifyTarget(Player target, Player spectator, boolean spectate) {
		if(target == null)
			return;
		if(Config.getString(Paths.CONFIG_NOTIFY_CURRENT_TARGET).equalsIgnoreCase("NONE"))
			return;
		if(spectator.hasPermission(Permissions.BYPASS_NOTIFY))
			return;

		String message = Messages.getMessage(target, spectate ? Paths.MESSAGES_GENERAL_NOTIFY_SPECTATE :
				Paths.MESSAGES_GENERAL_NOTIFY_UNSPECTATE, "TARGET", spectator.getName());

		switch(Config.getString(Paths.CONFIG_NOTIFY_CURRENT_TARGET).toUpperCase()) {
			case "CHAT" -> target.spigot().sendMessage(ChatMessageType.CHAT, new TextComponent(message));
			case "ACTIONBAR" -> target.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
			case "TITLE" -> target.sendTitle(message, "", 5, 50, 5);
			case "SUBTITLE" -> target.sendTitle("", message, 5, 50, 5);
		}
	}
}
