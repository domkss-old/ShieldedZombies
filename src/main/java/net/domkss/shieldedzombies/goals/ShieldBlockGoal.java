package net.domkss.shieldedzombies.goals;

import net.domkss.shieldedzombies.ShieldedZombiesMod;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;


public class ShieldBlockGoal extends Goal {
    private final ZombieEntity zombie;
    private static final int BLOCK_DURATION = ShieldedZombiesMod.modConfig.getBlockDuration()*20;
    private static final int COOLDOWN_DURATION = ShieldedZombiesMod.modConfig.getShieldCooldown()*20;
    private boolean isBlocking; // Whether the zombie is currently blocking
    private int blockDuration; // Duration of blocking (ticks)

    public ShieldBlockGoal(ZombieEntity zombie) {
        this.zombie = zombie;
    }

    @Override
    public boolean canStart() {
        // Start if cooldown is over, there's a shield, and the zombie is under attack
        return ((ShieldBlockCooldownTracker) zombie).getShieldCooldown()  <= 0
                && !zombie.isDead()
                && zombie.getOffHandStack().getItem() instanceof ShieldItem
                && zombie.getAttacker() != null;
    }

    @Override
    public void start() {
        // Begin blocking
        isBlocking = true;
        blockDuration = BLOCK_DURATION; // Set block duration
        zombie.setCurrentHand(Hand.OFF_HAND);
        zombie.getNavigation().stop(); // Stop movement
    }

    @Override
    public void stop() {
        // End blocking and set the cooldown
        isBlocking = false;
        blockDuration = 0;
        zombie.stopUsingItem();
        ((ShieldBlockCooldownTracker) zombie).setShieldCooldown(COOLDOWN_DURATION);
    }

    @Override
    public boolean shouldContinue() {
        // Continue blocking while block duration is active
        return isBlocking && blockDuration > 0 && zombie.getOffHandStack().getItem() instanceof ShieldItem;
    }

    @Override
    public void tick() {
        if (isBlocking) {
            // Decrease block duration
            blockDuration--;

            // Freeze the zombie (stop movement, disable attacking)
            zombie.getNavigation().stop();
            zombie.setVelocity(0, zombie.getVelocity().y, 0);
            zombie.setAttacking(false);
        }
    }


}
