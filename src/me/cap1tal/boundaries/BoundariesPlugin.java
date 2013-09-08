package me.cap1tal.boundaries;

import java.util.ArrayList;

import me.cap1tal.bukkitutil.CustomConfig;
import me.cap1tal.bukkitutil.SerLocation;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class BoundariesPlugin extends JavaPlugin
{
	private CustomConfig config = new CustomConfig(this, "config.yml");
	private CustomConfig regionConfig = new CustomConfig(this, "regions.yml");
	private ArrayList<BoundariesRegion> regions = new ArrayList<BoundariesRegion>();
	private BoundariesThread thread = new BoundariesThread(this);
	
	static
	{
		ConfigurationSerialization.registerClass(SerLocation.class);
		ConfigurationSerialization.registerClass(BoundariesRegion.class);
	}
	
	@Override
	public void onEnable()
	{	
		/* Load config and regions */
		config.reload();
		regionConfig.reload();
		loadRegions();
		
		/* Start thread */
		thread.schedule();
	}
	
	@Override
	public void onDisable()
	{
		/* Stop threads */
		thread.cancel();
		getServer().getScheduler().cancelTasks(this);
		
		/* Save regions */
		saveRegions();
	}
	
	/* Saves all regions */
	public void saveRegions()
	{
		int i = 0;
		
		/* Save number of regions */
		regionConfig.get().set("total", regions.size());
		
		/* Save region data */
		for (BoundariesRegion reg : regions)
		{
			regionConfig.get().set(Integer.toString(i), reg);
			i++;
		}
		
		/* Save the config file */
		regionConfig.save();
	}
	
	/* Loads all regions */
	public void loadRegions()
	{
		int size, i;
		Object o;
		
		/* Clear the region list */
		regions.clear();
		
		/* Load the new regions */
		size = regionConfig.get().getInt("total", 0);
		
		for (i = 0; i < size; i++)
		{
			/* Load the region */
			if ((o = regionConfig.get().get(Integer.toString(i))) != null)
			{
				if (o instanceof BoundariesRegion)
				{
					regions.add((BoundariesRegion)o);
				}
			}
		}
	}
	
	/* Gets a region by name */
	public BoundariesRegion getRegionByName(String name)
	{
		for (BoundariesRegion reg : regions)
		{
			if (reg.getName().equalsIgnoreCase(name))
			{
				return reg;
			}
		}
		
		return null;
	}
	
	/* Command processor */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("boundaries"))
		{
			if (args.length == 1)
			{
				if (args[0].equalsIgnoreCase("reload"))
				{
					thread.cancel();
					thread.schedule();
					config.reload();
					loadRegions();
					
					sender.sendMessage(ChatColor.GREEN + "Reloaded");
				}
				else if (args[0].equalsIgnoreCase("list"))
				{
					int i = 0;
					
					sender.sendMessage(ChatColor.DARK_AQUA + "Regions (" + ChatColor.GOLD + Integer.toString(regions.size()) + ChatColor.DARK_AQUA + " total):");
					
					for (BoundariesRegion reg : regions)
					{
						sender.sendMessage(ChatColor.DARK_AQUA + Integer.toString(++i) + ". " + ChatColor.GOLD  + reg.getName() + ": (" + reg.getLocation().getWorld().getName() + ") (" + reg.getLocation().toVector().toString() + ")");
					}
				}
				else if (args[0].equalsIgnoreCase("disable"))
				{
					sender.sendMessage(ChatColor.GREEN + "Boundaries are now " + ChatColor.RED + "disabled");
					thread.cancel();
					thread.reset();
				}
				else if (args[0].equalsIgnoreCase("enable"))
				{
					sender.sendMessage(ChatColor.GREEN + "Boundaries are now enabled");
					thread.schedule();
				}
				else
				{
					return false;
				}
				
				return true;
			}
			else if (args.length == 2)
			{
				if (args[0].equalsIgnoreCase("create"))
				{
					if (!(sender instanceof Player))
					{
						sender.sendMessage("You must be a player to use this command");
					}
					
					if (getRegionByName(args[1]) != null)
					{
						sender.sendMessage(ChatColor.RED + "A region with that name already exists");
					}
					else
					{
						regions.add(new BoundariesRegion(((Player)sender).getLocation(), args[1]));
						sender.sendMessage(ChatColor.GREEN + "Region created");
					}
				}
				else if (args[0].equalsIgnoreCase("remove"))
				{
					BoundariesRegion reg;
					
					if ((reg = getRegionByName(args[1])) != null)
					{
						regions.remove(reg);
						
						sender.sendMessage(ChatColor.GREEN + "Region removed");
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "Region not found");
					}
				}
				else if (args[0].equalsIgnoreCase("info"))
				{
					BoundariesRegion reg;
					
					if ((reg = getRegionByName(args[1])) != null)
					{
						sender.sendMessage(ChatColor.DARK_AQUA + reg.getName() + ":");
						sender.sendMessage(ChatColor.DARK_AQUA + "Location: " + ChatColor.GOLD + "(" + reg.getLocation().getWorld().getName() + ") (" + reg.getLocation().toVector().toString() + ")");
						sender.sendMessage(ChatColor.DARK_AQUA + "Min x, y, z: " + ChatColor.GOLD + "(" + reg.getMin().toString() + ")");
						sender.sendMessage(ChatColor.DARK_AQUA + "Max x, y, z: " + ChatColor.GOLD + "(" + reg.getMax().toString() + ")");
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "Region not found");
					}
				}
				else if (args[0].equalsIgnoreCase("move"))
				{
					BoundariesRegion reg;
					
					if (!(sender instanceof Player))
					{
						sender.sendMessage("You must be a player to use this command");
					}
					
					if ((reg = getRegionByName(args[1])) != null)
					{
						reg.setLocation(((Player)sender).getLocation());
						sender.sendMessage(ChatColor.GREEN + "Region moved");
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "Region not found");
					}
				}
				else
				{
					return false;
				}
				
				return true;
			}
			else if (args.length == 3)
			{
				if (args[0].equalsIgnoreCase("rename"))
				{
					BoundariesRegion reg;
					
					if ((reg = getRegionByName(args[1])) != null)
					{
						if (getRegionByName(args[2]) != null)
						{
							sender.sendMessage(ChatColor.RED + "A region with that name already exists");
						}
						else
						{
							reg.setName(args[2]);
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "Region not found");
					}
				}
				if (args[0].equalsIgnoreCase("create"))
				{	
					if (getRegionByName(args[1]) != null)
					{
						sender.sendMessage(ChatColor.RED + "A region with that name already exists");
					}
					else
					{
						World w;
						
						if ((w = getServer().getWorld(args[2])) == null)
						{
							int size = getServer().getWorlds().size();
							String str = new String("");
							
							sender.sendMessage(ChatColor.RED + "Invalid world name");
							
							for (int i = 0; i < size; i++)
							{
								str += getServer().getWorlds().get(i).getName();
								
								if (i != size - 1)
								{
									str += ", ";
								}
							}
							
							sender.sendMessage(ChatColor.RED + "Worlds: " + str);
							
							return true;
						}
						
						regions.add(new BoundariesRegion(w.getSpawnLocation(), args[1]));
						sender.sendMessage(ChatColor.GREEN + "Region created");
					}
				}
			}
			else if (args.length == 5)
			{
				if (args[0].equalsIgnoreCase("setmin") || args[0].equalsIgnoreCase("setmax"))
				{
					BoundariesRegion reg;
					
					if ((reg = getRegionByName(args[1])) != null)
					{
						double x, y, z;
						
						try
						{
							x = Double.parseDouble(args[2]);
							y = Double.parseDouble(args[3]);
							z = Double.parseDouble(args[4]);
						}
						catch (NumberFormatException e)
						{
							sender.sendMessage(ChatColor.RED + "Invalid number");
							return true;
						}
						
						if (args[0].equalsIgnoreCase("setmin"))
						{
							reg.setMin(new Vector(x, y, z));
							sender.sendMessage(ChatColor.GREEN + "Minimum values set");
						}
						else if (args[0].equalsIgnoreCase("setmax"))
						{
							reg.setMax(new Vector(x, y, z));
							sender.sendMessage(ChatColor.GREEN + "Maximum values set");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "Region not found");
					}
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	/* Accessors */
	public ArrayList<BoundariesRegion> getRegions()
	{
		return regions;
	}
	
	public CustomConfig getSettings()
	{
		return config;
	}
	
	/* Mutators */
	public void removeRegion(BoundariesRegion region)
	{
		regions.remove(region);
	}
}
