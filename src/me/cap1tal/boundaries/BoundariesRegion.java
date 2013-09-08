package me.cap1tal.boundaries;

import java.util.HashMap;
import java.util.Map;

import me.cap1tal.bukkitutil.SerLocation;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BoundariesRegion implements ConfigurationSerializable
{
	private Location loc;
	private Vector max, min;
	private String name;
	
	public BoundariesRegion(Location loc, String name)
	{
		max = new Vector(0, 0, 0);
		min = new Vector(0, 0, 0);
		this.loc = loc.clone();
		this.name = new String(name);
	}
	
	public BoundariesRegion(Map<String, Object> map) throws IllegalArgumentException
	{
		Object o;
		
		/* Get loc */
		if ((o = map.get("loc")) != null && o instanceof SerLocation)
		{
			loc = ((SerLocation)o).toLocation();
		}
		else
		{
			throw new IllegalArgumentException();
		}
		
		/* Get max */
		if ((o = map.get("max")) != null && o instanceof Vector)
		{
			max = ((Vector)o).clone();
		}
		else
		{
			throw new IllegalArgumentException();
		}
		
		/* Get min */
		if ((o = map.get("min")) != null && o instanceof Vector)
		{
			min = ((Vector)o).clone();
		}
		else
		{
			throw new IllegalArgumentException();
		}
		
		/* Get name */
		if ((o = map.get("name")) != null && o instanceof String)
		{
			name = new String((String)o);
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public Map<String, Object> serialize()
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		map.put("loc", new SerLocation(loc));
		map.put("max", max.clone());
		map.put("min", min.clone());
		map.put("name", new String(name));
		
		return map;
	}
	
	/* Checks if a player is in this region */
	public boolean playerIsInRegion(Player p)
	{
		/* Check world */
		if (!p.getWorld().equals(loc.getWorld()))
		{
			return false;
		}
		
		Location loc;
		
		/* Get player's location */
		loc = p.getLocation();
		
		/* Check x, y, z */
		if (!(loc.getX() >= this.loc.getX() + min.getX() && loc.getX() <= this.loc.getX() + max.getX()) ||
			!(loc.getY() >= this.loc.getY() + min.getY() && loc.getY() <= this.loc.getY() + max.getY()) ||
			!(loc.getZ() >= this.loc.getZ() + min.getZ() && loc.getZ() <= this.loc.getZ() + max.getZ()))
		{
			return false;
		}
		
		return true;
	}
	
	/* Accessors */
	public Location getLocation()
	{
		return loc;
	}
	
	public Vector getMax()
	{
		return max;
	}
	
	public Vector getMin()
	{
		return min;
	}
	
	public String getName()
	{
		return name;
	}
	
	/* Mutators */
	public void setName(String name)
	{
		this.name = new String(name);
	}
	
	public void setLocation(Location loc)
	{
		this.loc = loc.clone();
	}
	
	public void setMin(Vector min)
	{
		this.min = min.clone();
	}
	
	public void setMax(Vector max)
	{
		this.max = max.clone();
	}
}
