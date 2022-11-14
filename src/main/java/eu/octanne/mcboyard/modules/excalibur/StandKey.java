package eu.octanne.mcboyard.modules.excalibur;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.entity.standkey.CrochetEntity;
import eu.octanne.mcboyard.entity.standkey.KeyEntity;
import eu.octanne.mcboyard.entity.standkey.MiddleEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.type.Grindstone;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.impl.CraftAnvil;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class StandKey {

    private static List<StandKey> standKeys = new ArrayList<>();

    private double[] locStand;
    private String locWorld;

    private CrochetEntity[] crochetEntities;

    private MiddleEntity middleEntity;

    private KeyEntity keyEntity;

    private final UUID id;
    private boolean isComplete;

    private int[] cordeResistance;
    private static final int minDura = 12, maxDura = 12;

    public StandKey(String locWorld, double locX, double locY, double locZ) {
        this.locStand = new double[]{locX, locY, locZ};
        this.locWorld = locWorld;
        id = UUID.randomUUID();
        genRandomResistances(minDura,maxDura);

        constructCrochetAndMiddleEntities();
        setBlocks();

        isComplete = true;
    }

    public StandKey(UUID id) {
        this.id = id;
        genRandomResistances(minDura,maxDura);

        // set crochet and middle entities to null
        crochetEntities = new CrochetEntity[]{null, null, null, null};
        middleEntity = null;

        isComplete = false;
    }

    public static void clearStandKeys() {
        standKeys = new ArrayList<>();
    }

    public boolean attachMiddleEntity(MiddleEntity middleEntity) {
        if (isComplete) return false;
        this.middleEntity = middleEntity;
        this.locWorld = middleEntity.getWorld().getWorld().getName();
        this.locStand = new double[]{
                middleEntity.getBukkitEntity().getLocation().getX(),
                middleEntity.getBukkitEntity().getLocation().getY()-0.5,
                middleEntity.getBukkitEntity().getLocation().getZ()
        };
        setBlocks();
        return checkIfComplete();
    }

    public boolean attachKeyEntity(KeyEntity keyEntity) {
        if (isComplete) return false;
        this.keyEntity = keyEntity;
        return checkIfComplete();
    }

    public boolean attachCrochetEntity(CrochetEntity crochetEntity) {
        if (isComplete) return false;
        for (int i = 0; i < crochetEntities.length; i++) {
            if (crochetEntities[i] == null) {
                crochetEntities[i] = crochetEntity;
                break;
            }
        }
        return checkIfComplete();
    }

    private boolean checkIfComplete() {
        if (this.crochetEntities[0] != null && this.crochetEntities[1] != null
                && this.crochetEntities[2] != null && this.crochetEntities[3] != null &&
                this.keyEntity != null && this.middleEntity != null) {
            isComplete = true;
            reset();
        }
        return true;
    }

    public boolean detachCrochetEntity(CrochetEntity crochetEntity) {
        for (int i = 0; i < crochetEntities.length; i++) {
            if (crochetEntities[i] != null && crochetEntities[i].getStandKeyID().equals(crochetEntity.getStandKeyID())) {
                crochetEntities[i] = null;
                break;
            }
        }
        if (this.crochetEntities[0] == null || this.crochetEntities[1] == null
                || this.crochetEntities[2] == null || this.crochetEntities[3] == null) {
            isComplete = false;
        }
        return true;
    }

    public boolean detachMiddleEntity() {
        this.middleEntity = null;
        isComplete = false;
        return true;
    }

    public boolean detachKeyEntity() {
        this.keyEntity = null;
        isComplete = false;
        return true;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public UUID getID() {
        return id;
    }

    public Location getBukkitLocation() {
        return new Location(Bukkit.getWorld(locWorld), locStand[0], locStand[1], locStand[2]);
    }

    public void despawn() {
        for (CrochetEntity crochetEntity : crochetEntities) {
            if (crochetEntity != null) crochetEntity.despawn();
        }
        if (middleEntity != null) middleEntity.despawn();
        if (keyEntity != null) keyEntity.despawn();
        for (Location loc : getBlocksLoc()) {
            loc.getBlock().setType(org.bukkit.Material.AIR);
        }
    }

    private void constructCrochetAndMiddleEntities() {
        List<CrochetEntity> crochetEntityList = new ArrayList<>();
        int i = 0;
        for (Location loc : getBlocksLoc()) {
            if (i == 4) {
                // Middle Entity
                Location locMiddle = loc.clone();
                locMiddle.setY(locMiddle.getY() + 0.25);
                middleEntity = new MiddleEntity(((CraftWorld)loc.getWorld()).getHandle(), locMiddle, this);
            } else {
                // Crochet Entity
                Location locCrochet = loc.clone();
                locCrochet.setY(loc.getY() + 0.40);
                crochetEntityList.add(new CrochetEntity(((CraftWorld)loc.getWorld()).getHandle(), locCrochet, this));
            }
            i++;
        }
        crochetEntities = crochetEntityList.toArray(new CrochetEntity[0]);
        Location keyLoc = getBukkitLocation().clone();
        keyLoc.setY(keyLoc.getY() - 0.35);
        keyLoc.setYaw(90.0f);
        keyEntity = new KeyEntity(((CraftWorld)middleEntity.getBukkitEntity().getWorld()).getHandle(), keyLoc, this);
        reset();
    }

    public void reset() {
        for (CrochetEntity crochetEntity : crochetEntities) {
            crochetEntity.attachStringFromMiddle();
        }
        keyEntity.restoreKey();
        genRandomResistances(minDura,maxDura);
    }

    private void genRandomResistances(int min, int max) {
        cordeResistance = new int[4];
        for (int i = 0; i < cordeResistance.length; i++) {
            // Generate random resistance between min and max
            cordeResistance[i] = (int)(Math.random() * (max - min + 1) + min);
        }
    }

    public boolean attaquerCorde(CrochetEntity en, Player p) {
        if (!isComplete() || !en.isAttachToMiddle() || !p.getInventory().getItemInMainHand().getType().equals(Material.IRON_SWORD)) return false;
        ItemStack item = p.getInventory().getItemInMainHand();
        for (int i = 0; i < crochetEntities.length; i++) {
            if (crochetEntities[i].equals(en)) {
                cordeResistance[i] -= 1;

                if (((Damageable)item.getItemMeta()).getDamage() == 250) {
                    item.setAmount(0);
                } else {
                    ItemMeta meta = item.getItemMeta();
                    ((Damageable)meta).setDamage(((Damageable)meta).getDamage() + 1);
                    item.setItemMeta(meta);
                }

                if (cordeResistance[i] <= 0) {
                    crochetEntities[i].detachStringFromMiddle();
                    // play Sound
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_DISPENSER_LAUNCH, 2f, 1);
                } else {
                    // play Sound
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.7f, 1);
                }

                if (allCordeBroken()) {
                    // play Sound
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_CHAIN_PLACE, 2.5f, 1);
                    // drop key
                    keyEntity.lootKey();
                }
                return true;
            }
        }
        return false;
    }

    public boolean allCordeBroken() {
        for (int i = 0; i < cordeResistance.length; i++) {
            if (cordeResistance[i] > 0) return false;
        }
        return true;
    }

    public CrochetEntity getCloserCrochet(Location loc) {
        double distance = 999999999;
        CrochetEntity crochetEntity = null;
        for (CrochetEntity crochetEntity1 : crochetEntities) {
            if (crochetEntity1.getBukkitEntity().getLocation().distance(loc) < distance) {
                distance = crochetEntity1.getBukkitEntity().getLocation().distance(loc);
                crochetEntity = crochetEntity1;
            }
        }
        return crochetEntity;
    }

    public List<Location> getBlocksLoc() {
        List<Location> locs = new ArrayList<>();
        // Loc Left Top
        locs.add(new Location(Bukkit.getWorld(locWorld), locStand[0]-2, locStand[1]+1, locStand[2]-2));
        // Loc Right Top
        locs.add(new Location(Bukkit.getWorld(locWorld), locStand[0]+2, locStand[1]+1, locStand[2]+2));
        // Loc Left Bottom
        locs.add(new Location(Bukkit.getWorld(locWorld), locStand[0]-2, locStand[1]+1, locStand[2]+2));
        // Loc Right Bottom
        locs.add(new Location(Bukkit.getWorld(locWorld), locStand[0]+2, locStand[1]+1, locStand[2]-2));
        // Loc anvil
        locs.add(new Location(Bukkit.getWorld(locWorld), locStand[0], locStand[1], locStand[2]));
        return locs;
    }

    private void setBlocks() {
        int i = 0;
        for (Location loc : getBlocksLoc()) {
            if (i == 4) {
                loc.getBlock().setType(Material.DAMAGED_ANVIL);
                CraftAnvil anvil = (CraftAnvil) loc.getBlock().getState().getBlockData();
                anvil.setFacing(BlockFace.EAST);
                loc.getBlock().setBlockData(anvil);
            } else {
                loc.getBlock().setType(Material.GRINDSTONE);
                Grindstone grindstone = (Grindstone) loc.getBlock().getBlockData();
                grindstone.setAttachedFace(FaceAttachable.AttachedFace.FLOOR);
                grindstone.setFacing(BlockFace.SOUTH);
                loc.getBlock().setBlockData(grindstone);
            }
            i++;
        }
    }

    public void delete() {
        standKeys.remove(this);
        despawn();
    }

    public MiddleEntity getMiddleEntity() {
        return middleEntity;
    }

    public static List<StandKey> getStandKeys() {
        return standKeys;
    }

    public static Optional<StandKey> getStandKey(UUID id) {
        for (StandKey standKey : standKeys) {
            if (standKey.id.equals(id)) {
                return Optional.of(standKey);
            }
        }
        return Optional.empty();
    }

    public static StandKey getStandKeyRegenIfNotLoad(UUID idStandKey) {
        Optional<StandKey> standKey = getStandKey(idStandKey);
        if (standKey.isPresent()) {
            return standKey.get();
        } else {
            StandKey standKey1 = new StandKey(idStandKey);
            standKeys.add(standKey1);
            return standKey1;
        }
    }

    public static boolean removeStandKey(UUID standKeyID) {
        Optional<StandKey> standKey = getStandKey(standKeyID);
        if (standKey.isPresent()) {
            standKey.get().delete();
            return true;
        } else return false;
    }

    public static Optional<StandKey> createStandKey(String locWorld, double locX, double locY, double locZ) {
        // check if standkey is already exist
        for (StandKey standKey : standKeys) {
            if (standKey.locWorld.equals(locWorld) && standKey.locStand[0] == locX
                    && standKey.locStand[1] == locY && standKey.locStand[2] == locZ) {
                return Optional.empty();
            }
        }

        StandKey standKey = new StandKey(locWorld, locX, locY, locZ);
        standKeys.add(standKey);
        return Optional.of(standKey);
    }
}
