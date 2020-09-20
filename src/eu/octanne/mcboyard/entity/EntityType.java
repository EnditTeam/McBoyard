package eu.octanne.mcboyard.entity;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.RegistryMaterials;

public enum EntityType {
	//NAME("Entity name", Entity ID, yourcustomclass.class);
	CHAIR("chair", 399, ChairEntity.class, "Chair"); //You can add as many as you want.

	private EntityType(String name, int id, Class<? extends Entity> custom, String str2)
	{
		addToMaps(custom, name, id, str2);
	}

	public static void spawnEntity(Entity entity, Location loc)
	{
		entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		((CraftWorld) loc.getWorld()).getHandle().addEntity(entity);
	}

	@SuppressWarnings("unchecked")
	private static void addToMaps(Class<?> clazz, String name, int id, String str2)
	{
		MinecraftKey key = new MinecraftKey(name);
		((RegistryMaterials<MinecraftKey, Class<? extends Entity>>)getPrivateField("b", EntityTypes.class, null)).a(id, key, (Class<? extends Entity>) clazz);
		((Set<MinecraftKey>) getPrivateField("d", EntityTypes.class, null)).add(key);
		List<String> g = (List<String>) getPrivateField("g", EntityTypes.class, null);
		while (g.size() <= id) {
			g.add(null);
		}
		g.set(id, str2);
	}

	public static Object getPrivateField(String fieldName, Class<?> clazz, Object object)
	{
		Field field;
		Object o = null;
		try
		{
			field = clazz.getDeclaredField(fieldName);


			field.setAccessible(true);
			o = field.get(object);
		}
		catch(NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return o;
	}
}
