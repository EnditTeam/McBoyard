package eu.octanne.mcboyard.entity_old;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.MinecraftKey;

public enum EntityCustom {
	
	//NAME("Entity name", Entity ID, yourcustomclass.class);
	TYRO_TAIL("tyro_tail", 55, TyroEntity.class, "TyroTail"), // 55
	TYRO_SEAT("tyro_seat", 401, TyroSeatEntity.class, "TyroSeat"),
	TYRO_HITCH("tyro_hitch", 8, TyroHitchEntity.class, "TyroHitch"); // 8
	
	private EntityCustom(String name, int id, Class<? extends Entity> custom, String str2)
	{
		addToMaps(custom, name, id, str2);
	}

	public static void spawnEntity(Entity entity, Location loc)
	{
		entity.setPosition(loc.getX(), loc.getY(), loc.getZ());
		//entity.spawnIn(((CraftWorld) loc.getWorld()).getHandle());
		((CraftWorld) loc.getWorld()).getHandle().addEntity(entity);
	}

	@SuppressWarnings("unchecked")
	private static void addToMaps(Class<? extends Entity> clazz, String name, int id, String str2)
	{
		MinecraftKey key = new MinecraftKey(name);
		//((RegistryMaterials<MinecraftKey, Class<? extends Entity>>)getPrivateField("b", EntityTypes.class, null)).a(id, key, clazz);
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
