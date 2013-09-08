package me.cap1tal.boundaries;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class BoundariesThread implements Runnable
{
	private BoundariesPlugin plugin;
	private HashMap<OfflinePlayer, Long> zoneTimes = new HashMap<OfflinePlayer, Long>();
	private int id = -1;
	private boolean scheduled = false;
	
	public long getTicksPerCheck()
	{
		return plugin.getSettings().get().getLong("ticksPerCheck");
	}
	
	public long getTicksBeforeDeath()
	{
		return plugin.getSettings().get().getLong("ticksBeforeDeath");
	}
	
	public String getOutOfBoundsMessage()
	{
		return plugin.getSettings().get().getString("outOfBoundsMessage");
	}
	
	public String getBackInBoundsMessage()
	{
		return plugin.getSettings().get().getString("backInBoundsMessage");
	}
	
	public void schedule()
	{
		/* Cancel old task */
		cancel();
		
		/* Schedule task */
		id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 0, getTicksPerCheck());
		scheduled = true;
	}
	
	public void cancel()
	{
		if (scheduled)
		{
			plugin.getServer().getScheduler().cancelTask(id);
		}
	}
	
	public boolean playerIsInAnyRegion(Player p)
	{
		for (BoundariesRegion r : plugin.getRegions())
		{
			if (r.playerIsInRegion(p))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public BoundariesThread(BoundariesPlugin instance)
	{
		plugin = instance;
	}
	
	public void reset()
	{
		zoneTimes.clear();
	}
	
	@Override
	public void run()
	{
		long ticksPerCheck = getTicksPerCheck();
		long ticksBeforeDeath = getTicksBeforeDeath();
		String outOfBoundsMessage = getOutOfBoundsMessage();
		String backInBoundsMessage = getBackInBoundsMessage();
		
		/* Loop through all players and check if their locations */
		for (World w : plugin.getServer().getWorlds())
		{
			for (Player p : w.getPlayers())
			{
				if (p.hasPermission("boundaries.exempt"))
				{
					continue;
				}
				
				Long l;
				
				if ((l = zoneTimes.get(p)) != null)
				{
					zoneTimes.remove(p);
					
					if (playerIsInAnyRegion(p))
					{
						if (l != -1)
						{
							p.sendMessage(ChatColor.BLUE + backInBoundsMessage);
						}
					}
					else
					{
						if (l == -1l)
						{
							zoneTimes.put(p, 0l);
						}
						else
						{
							if (l + ticksPerCheck >= ticksBeforeDeath)
							{
								p.setHealth(0);
							}
							//else if (ticksBeforeDeath - (l + ticksPerCheck) < 100l)
							else
							{
								zoneTimes.put(p, l + ticksPerCheck);
								p.getWorld().playEffect(p.getLocation(), Effect.GHAST_SHRIEK, 0);
								p.sendMessage(ChatColor.RED + "TICKS UNTIL DEATH: " + ChatColor.DARK_PURPLE + Long.toString(ticksBeforeDeath - (l + ticksPerCheck)));
							}
							/*else
							{
								zoneTimes.put(p, l + ticksPerCheck);
							}*/
						}
					}
				}
				else
				{
					if (!playerIsInAnyRegion(p))
					{
						zoneTimes.put(p, 0l);
						
						p.getWorld().playEffect(p.getLocation(), Effect.GHAST_SHRIEK, 0);
						p.sendMessage(ChatColor.RED + outOfBoundsMessage);
					}
				}
			}
		}
	}
}
