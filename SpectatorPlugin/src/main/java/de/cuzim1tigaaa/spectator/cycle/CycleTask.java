package de.cuzim1tigaaa.spectator.cycle;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.listener.TeleportListener;
import de.cuzim1tigaaa.spectator.spectate.Displays;
import de.cuzim1tigaaa.spectator.spectate.SpectateState;
import de.cuzim1tigaaa.spectator.util.SchedulerUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class CycleTask {

	@Getter
	private static final Map<UUID, SpectateState> stateChange = new HashMap<>();
	private static final Displays displays = new Displays(Spectator.getPlugin());

	private final int interval;

	/** Running handle for the cycle interval timer; null means not running. */
	private ScheduledTask runningTask;
	@Setter
	private Cycle cycle;
	@Setter
	private BossBar bossBar;
	@Setter
	private ScheduledTask showTargetTask;

	public CycleTask(int interval, Cycle cycle) {
		this.interval = interval;
		this.cycle = cycle;
		this.showTargetTask = null;
		this.runningTask = null;
	}

	public boolean isRunning() {
		return runningTask != null && !runningTask.isCancelled();
	}

	public void startTask(Spectator plugin) {
		if(isRunning())
			return;

		runningTask = SchedulerUtils.runEntityTimer(plugin, cycle.getOwner(),
				() -> selectNextPlayer(plugin), 1L, interval * 20L);
		displays.showCycleDisplay(cycle.getOwner());
	}

	public void selectNextPlayer(Spectator plugin) {
		final SpectateAPI spectateAPI = plugin.getSpectateAPI();
		Player spectator = cycle.getOwner();

		if(spectateAPI.getSpectateablePlayers().isEmpty()) {
			if(!Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS)) {
				spectateAPI.getSpectateCycle().stopCycle(spectator);
				Messages.sendMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_STOP);
				return;
			}
			spectateAPI.getSpectateCycle().pauseCycle(spectator);
			Messages.sendMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_PAUSE);
			return;
		}

		Player next = cycle.getNextTarget(plugin);
		Player last = cycle.getLastPlayer();

		if(next == null || next.isDead() || !next.isOnline())
			return;

		if(last == null || !last.equals(next))
			spectateAPI.getSpectateGeneral().notifyTarget(last, spectator, false);

		if(next.getWorld() != spectator.getWorld()) {
			TeleportListener.getWorldChange().put(spectator.getUniqueId(), next);
			getStateChange().put(spectator.getUniqueId(), SpectateState.CYCLING);
		}
		spectateAPI.getSpectateGeneral().spectate(spectator, next);
	}

	public CycleTask stopTask() {
		if(!isRunning())
			return this;
		runningTask.cancel();
		runningTask = null;

		if(getBossBar() != null)
			getBossBar().removeAll();

		if(getShowTargetTask() != null) {
			getShowTargetTask().cancel();
			setShowTargetTask(null);
		}

		return this;
	}
}
