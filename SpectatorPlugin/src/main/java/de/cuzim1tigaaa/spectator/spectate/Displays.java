package de.cuzim1tigaaa.spectator.spectate;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.Cycle;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.util.SchedulerUtils;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.boss.*;
import org.bukkit.entity.Player;

public class Displays {

	private final Spectator plugin;
	private final SpectateAPI spectateAPI;

	public Displays(Spectator plugin) {
		this.plugin = plugin;
		this.spectateAPI = plugin.getSpectateAPI();
	}

	public void showCycleDisplay(Player spectator) {
		if(!spectateAPI.isCyclingSpectator(spectator))
			return;

		CycleTask task = spectateAPI.getCycleTask(spectator);
		if(task == null)
			return;

		Cycle cycle = task.getCycle();
		String mode = Config.getString(Paths.CONFIG_CYCLE_SHOW_TARGET).toUpperCase();

		if(mode.equalsIgnoreCase("ACTIONBAR")) {
			SchedulerUtils.cancel(task.getShowTargetTask());

			task.setShowTargetTask(SchedulerUtils.runEntityTimer(plugin, spectator, () -> {
				Player target = cycle.getLastPlayer();
				spectator.spigot().sendMessage(ChatMessageType.ACTION_BAR,
						target != null
								? new TextComponent(Messages.getMessage(target, Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getName()))
								: new TextComponent(Messages.getMessage(spectator, Paths.MESSAGES_CYCLING_SEARCHING_TARGET)));
			}, 1L, 10L));
			return;
		}

		if(mode.contains("TITLE")) {
			SchedulerUtils.cancel(task.getShowTargetTask());

			task.setShowTargetTask(SchedulerUtils.runEntityTimer(plugin, spectator, () -> {
				Player player = cycle.getLastPlayer();
				String title = player != null
						? Messages.getMessage(player, Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", player.getName())
						: Messages.getMessage(spectator, Paths.MESSAGES_CYCLING_SEARCHING_TARGET);

				if(mode.equals("SUBTITLE")) {
					spectator.sendTitle("", title, 0, 20, 0);
					return;
				}
				spectator.sendTitle(title, "", 0, 20, 0);
			}, 1L, 10L));
			return;
		}

		if(mode.equalsIgnoreCase("BOSSBAR")) {
			SchedulerUtils.cancel(task.getShowTargetTask());

			BarColor barColor = Config.getBarColor();
			BossBar bar = task.getBossBar() == null
					? Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID)
					: task.getBossBar();
			bar.setVisible(true);
			bar.addPlayer(spectator);
			task.setBossBar(bar);

			Player initialTarget = cycle.getLastPlayer();
			if(initialTarget == null) {
				bar.setTitle(Messages.getMessage(spectator, Paths.MESSAGES_CYCLING_SEARCHING_TARGET));
				bar.setColor(BarColor.RED);
			} else {
				bar.setTitle(Messages.getMessage(initialTarget, Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", initialTarget.getName()));
				bar.setColor(barColor);
			}

			int seconds = Config.getInt(Paths.CONFIG_CYCLE_BOSSBAR_FACTOR) <= 0
					? 1 : Config.getInt(Paths.CONFIG_CYCLE_BOSSBAR_FACTOR);
			final int intervalTicks = Math.max(1, task.getInterval() * seconds - 1);
			final double[] counter = {intervalTicks};

			task.setShowTargetTask(SchedulerUtils.runEntityTimer(plugin, spectator, () -> {
				final Player currentTarget = cycle.getLastPlayer();
				if(currentTarget == null) {
					bar.setTitle(Messages.getMessage(spectator, Paths.MESSAGES_CYCLING_SEARCHING_TARGET));
					bar.setColor(BarColor.RED);
				} else {
					bar.setTitle(Messages.getMessage(currentTarget, Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", currentTarget.getName()));
					bar.setColor(barColor);
				}

				if(counter[0] == 0) {
					bar.removeAll();
					bar.addPlayer(cycle.getOwner());
					counter[0] = intervalTicks;
					bar.setProgress(counter[0] / intervalTicks);
					return;
				}

				if(cycle.getOwner().getGameMode() == GameMode.SPECTATOR)
					spectateAPI.setRelation(cycle.getOwner(), currentTarget);
				counter[0]--;
				bar.setProgress(counter[0] / intervalTicks);
			}, 20L / seconds, 20L / seconds));
			return;
		}
		Spectator.debug("No valid display mode found: " + mode);
	}
}
