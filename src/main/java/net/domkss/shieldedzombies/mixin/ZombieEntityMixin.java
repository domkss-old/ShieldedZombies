package net.domkss.shieldedzombies.mixin;

import net.domkss.shieldedzombies.ShieldedZombiesMod;
import net.domkss.shieldedzombies.goals.ShieldBlockCooldownTracker;
import net.domkss.shieldedzombies.goals.ShieldBlockGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends HostileEntity implements ShieldBlockCooldownTracker {


	protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "initEquipment", at = @At(value = "TAIL"))
	protected void initOffHandShield(Random random, LocalDifficulty localDifficulty, CallbackInfo ci){
		float spawnChance = (float) ShieldedZombiesMod.modConfig.getNormalSpawnChance();
		float hardSpawnChance = (float) ShieldedZombiesMod.modConfig.getHardSpawnChance();

		if (random.nextFloat() < (this.world.getDifficulty() == Difficulty.HARD ? hardSpawnChance : spawnChance)) {
			this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
		}
	}

	@Inject(method = "initGoals", at = @At("TAIL"))
	private void addShieldBlockGoal(CallbackInfo ci) {
		this.goalSelector.add(3, new ShieldBlockGoal((ZombieEntity) (Object) this));
	}


	//Block damage when using shield
	@Inject(method = "damage", at = @At("HEAD"), cancellable = true)
	private void handleShieldBlock(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (this.isUsingItem() && this.getActiveItem().getItem() instanceof ShieldItem) {
			//Only block attacks and projectiles
			if((source.getAttacker() instanceof LivingEntity || source.isProjectile())
					&& !source.isExplosive() && !source.isFire()  && !source.isMagic() && !source.bypassesArmor()) {
				this.world.playSound(null, this.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.HOSTILE, 1.0F, 1.0F);
				cir.setReturnValue(false); // Block the damage
			}

		}
	}



	@Unique
	private static final TrackedData<Integer> SHIELD_COOLDOWN_TRACKER = DataTracker.registerData(ZombieEntity.class, TrackedDataHandlerRegistry.INTEGER);

	@Inject(method = "initDataTracker", at = @At("TAIL"))
	private void initCooldownTracker(CallbackInfo ci) {
		this.dataTracker.startTracking(SHIELD_COOLDOWN_TRACKER, 0); // Initialize with 0 cooldown
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void decrementCooldown(CallbackInfo ci) {
		int cooldown = this.dataTracker.get(SHIELD_COOLDOWN_TRACKER);
		if (cooldown > 0) {
			this.dataTracker.set(SHIELD_COOLDOWN_TRACKER, cooldown - 1);
		}
	}
	// Public accessors for the cooldown tracker
	public int getShieldCooldown() {
		return this.dataTracker.get(SHIELD_COOLDOWN_TRACKER);
	}

	public void setShieldCooldown(int ticks) {
		this.dataTracker.set(SHIELD_COOLDOWN_TRACKER, ticks);
	}


}
