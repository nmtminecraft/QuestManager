package nmt.minecraft.QuestManager.Quest.Requirements;

import nmt.minecraft.QuestManager.QuestManagerPlugin;
import nmt.minecraft.QuestManager.Configuration.RequirementState;
import nmt.minecraft.QuestManager.Configuration.StatekeepingRequirement;
import nmt.minecraft.QuestManager.Quest.Goal;
import nmt.minecraft.QuestManager.Quest.Requirement;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/**
 * Requirement that a given entity must be slain.<br />
 * This requirement purposely does not require the entity be slain by certain participants.
 * Instead it is simply required that the provided entity is defeated.
 * @author Skyler
 *
 */
public class VanquishRequirement extends Requirement implements Listener, StatekeepingRequirement {
	
	private LivingEntity foe;
	
	public VanquishRequirement(Goal goal, LivingEntity foe) {
		this(goal, "", foe);
	}
	
	public VanquishRequirement(Goal goal, String description, LivingEntity foe) {
		super(goal, description);
		this.foe = foe;
		state = false;
		
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}

	/**
	 * @return the foe
	 */
	public LivingEntity getFoe() {
		return foe;
	}
	
	/**
	 * Catches entity death events and changes state to reflect whether or not this requirement
	 * is satisfied
	 * @param e
	 */
	@EventHandler
	public void onVanquish(EntityDeathEvent e) {
		
		if (e.getEntity().equals(foe)) {
			state = true;
			
			//unregister listen, as we'll never need to check again
			HandlerList.unregisterAll(this);
			updateQuest();
		}
		
	}
	
	/**
	 * Double checks current state information, updating incase somehow the entity death
	 * slipped through the cracks
	 * TODO what about reloading!?!?!?!?!?!?!?!???!?!
	 */
	@Override
	public void update() {
		if (state) {
			return;
		}
		
		state = !foe.isDead();
	}

	@Override
	public RequirementState getState() {
		RequirementState myState = new RequirementState();
		
		myState.set("type", "vr");
		
		ConfigurationSection foeSection = myState.createSection("foe");
		foeSection.set("type", foe.getType().name());
		foeSection.set("maxhp", foe.getMaxHealth());
		foeSection.set("hp", foe.getHealth());
		foeSection.set("name", foe.getCustomName());
		foeSection.set("location", foe.getLocation());
		
		//do equips the dirty way :S
		ConfigurationSection equips = foeSection.createSection("equipment");
		EntityEquipment equipment = foe.getEquipment();
		
		equips.set("head", equipment.getHelmet());
		equips.set("chest", equipment.getChestplate());
		equips.set("legs", equipment.getLeggings());
		equips.set("boots", equipment.getBoots());
		equips.set("held", equipment.getItemInHand());
		
		return myState;
	}

	@Override
	public void loadState(RequirementState myState) throws InvalidConfigurationException {
		
		if (!myState.contains("type") || !myState.getString("type").equals("vr")) {
			throw new InvalidConfigurationException();
		}
		
		ConfigurationSection foeState = myState.getConfigurationSection("foe");
		Location loc = (Location) foeState.get("location");
		
		foe = (LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.valueOf(foeState.getString("type")));
		foe.setMaxHealth(foeState.getDouble("maxhp"));
		foe.setHealth(foeState.getDouble("hp"));
		foe.setCustomName(foeState.getString("name"));
		
		EntityEquipment equipment = foe.getEquipment();
		equipment.setHelmet(
				(ItemStack) foeState.get("head"));
		equipment.setChestplate(
				(ItemStack) foeState.get("chest"));
		equipment.setLeggings(
				(ItemStack) foeState.get("legs"));
		equipment.setBoots(
				(ItemStack) foeState.get("boots"));
		equipment.setItemInHand(
				(ItemStack) foeState.get("held"));
	}

	@Override
	public void fromConfig(YamlConfiguration config) {
		// TODO Auto-generated method stub
		
	}
	
	
}