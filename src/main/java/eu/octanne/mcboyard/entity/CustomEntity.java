package eu.octanne.mcboyard.entity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.v1_16_R3.*;
import net.minecraft.server.v1_16_R3.EntityTypes.Builder;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CustomEntity {

    public static EntityTypes<ExcaliburStand> EXCALIBUR_STAND;

    public static void registerEntities() {
        try {
            EXCALIBUR_STAND = registerEntity("armor_stand",
                    Builder.a(EntityArmorStand::new, EnumCreatureType.MISC).a(0.5F, 1.975F).trackingRange(10),
                    null);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T extends Entity> EntityTypes<T> registerEntity(String s, Builder entitytypes_builder, AttributeProvider.Builder attributesBuilder) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Class<EntityTypes> clazz = EntityTypes.class;
        Method aMethod = clazz.getDeclaredMethod("a", String.class, Builder.class);
        aMethod.setAccessible(true);

        EntityTypes<T> type = (EntityTypes<T>) aMethod.invoke(null, s, entitytypes_builder);

        final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        Field entityTypesField = EntityTypes.class.getField(s);
        entityTypesField.setAccessible(true);
        Object staticFieldBase = unsafe.staticFieldBase(entityTypesField);
        long staticFieldOffset = unsafe.staticFieldOffset(entityTypesField);
        unsafe.putObject(staticFieldBase, staticFieldOffset, type);

        Field attributesMapField = AttributeDefaults.class.getDeclaredField("b");
        attributesMapField.setAccessible(true);
        Map<EntityTypes<? extends EntityLiving>, AttributeProvider> attributesMap = (Map<EntityTypes<? extends EntityLiving>, AttributeProvider>) attributesMapField.get(null);
        if (attributesMap instanceof ImmutableMap) {
            attributesMap = new HashMap<>(attributesMap);
            staticFieldBase = unsafe.staticFieldBase(attributesMapField);
            staticFieldOffset = unsafe.staticFieldOffset(attributesMapField);
            unsafe.putObject(staticFieldBase, staticFieldOffset, attributesMap);
        }
        attributesMap.put(type, attributesBuilder.a());


        return type;
    }

}
