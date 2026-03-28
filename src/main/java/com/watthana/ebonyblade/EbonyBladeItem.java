package com.watthana.ebonyblade;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import net.minecraft.world.entity.Mob;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;



import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class EbonyBladeItem extends Item {
    private static final double BASE_DAMAGE = 6.0;
    private static final double BASE_ATTACK_SPEED_TOTAL = 5.0;

    private static final double PLAYER_BASE_ATTACK_DAMAGE = 1.0;
    private static final double PLAYER_BASE_ATTACK_SPEED = 4.0;

    private static final Identifier EBONY_ATTACK_DAMAGE_ID = Identifier.fromNamespaceAndPath(EbonyBlade.MOD_ID,
            "ebony_attack_damage");
    private static final Identifier EBONY_ATTACK_SPEED_ID = Identifier.fromNamespaceAndPath(EbonyBlade.MOD_ID,
            "ebony_attack_speed");

    public EbonyBladeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay displayComponent,
            Consumer<Component> textConsumer,
            TooltipFlag type) {
        int killCount = getKillCount(stack);
        double bonusDamage = getBonusDamage(stack);
        double totalDamage = getStoredDamage(stack);

        textConsumer.accept(Component.literal("Kills: " + killCount).withStyle(ChatFormatting.DARK_RED));
        textConsumer.accept(Component.literal("Bonus Damage: " + bonusDamage).withStyle(ChatFormatting.RED));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        syncEbonyAttributes(stack);

        if (!(entity instanceof Player player)) {
            return;
        }

        boolean holdingThisBladeInMainHand = slot == EquipmentSlot.MAINHAND;
        boolean holdingThisBladeInLeftHand = slot == EquipmentSlot.OFFHAND;
        boolean holdingAnyEbonyBladeInMainHand = player.getMainHandItem().is(ModItems.EBONY_BLADE);



         if (holdingThisBladeInLeftHand) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 10, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 10, 2, false, false, true));
        
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 10, 2, false, false, true));


            if (player.isSprinting()) {
                // กำลังวิ่ง: ให้ติดเอฟเฟกต์หิว และลบ Saturation ออก
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 2, false, false, false));
                player.removeEffect(MobEffects.SATURATION);
            } else {
                // ไม่ได้วิ่ง: ลบเอฟเฟกต์หิว แล้วเพิ่ม Saturation
                player.removeEffect(MobEffects.HUNGER);
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 10, 0, false, false, false));
            }
        
            if (player.isSprinting()) {
                // ถ้ากด Shift: ให้ม็อบในระยะ 5 บล็อกติด Levitation
                List<LivingEntity> nearbyMobs = player.level().getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(3.0),
                        target -> target != player && target instanceof Mob);

                for (LivingEntity mob : nearbyMobs) {
                    mob.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 10, 7, false, false, false));
                }

            } else {
                // ถ้าไม่ได้กด Shift: ลบ Levitation จากม็อบในระยะ 5 บล็อก
                List<LivingEntity> nearbyMobs = player.level().getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(40.0),
                        target -> target != player && target instanceof Mob);

                for (LivingEntity mob : nearbyMobs) {
                    mob.removeEffect(MobEffects.LEVITATION);
                }
            }
        }



        if (holdingThisBladeInMainHand) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 10, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 10, 1, false, false, true));

            if (player.isSprinting()) {
                // กำลังวิ่ง: ให้ติดเอฟเฟกต์หิว และลบ Saturation ออก
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 2, false, false, false));
                player.removeEffect(MobEffects.SATURATION);
            } else {
                // ไม่ได้วิ่ง: ลบเอฟเฟกต์หิว แล้วเพิ่ม Saturation
                player.removeEffect(MobEffects.HUNGER);
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 10, 0, false, false, false));
            }

            if (player.isShiftKeyDown()) {
                // ถ้ากด Shift: ให้ม็อบในระยะ 5 บล็อกติด Levitation
                List<LivingEntity> nearbyMobs = player.level().getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(3.0),
                        target -> target != player && target instanceof Mob);

                for (LivingEntity mob : nearbyMobs) {
                    mob.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 14, false, false, false));
                }

            } else {
                // ถ้าไม่ได้กด Shift: ลบ Levitation จากม็อบในระยะ 5 บล็อก
                List<LivingEntity> nearbyMobs = player.level().getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(20.0),
                        target -> target != player && target instanceof Mob);

                for (LivingEntity mob : nearbyMobs) {
                    mob.removeEffect(MobEffects.LEVITATION);
                }
            }

            if (player.hasEffect(MobEffects.SPEED)) {
                player.removeEffect(MobEffects.SLOWNESS);
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 10, 1, false, false, true));
            }

            player.removeEffect(MobEffects.BLINDNESS);
            player.removeEffect(MobEffects.POISON);
            player.removeEffect(MobEffects.WITHER);
            player.removeEffect(MobEffects.WEAKNESS);
            player.removeEffect(MobEffects.NAUSEA);

        } else if (!holdingAnyEbonyBladeInMainHand) {
            

        }
    }


///


@Override
public InteractionResult use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);

    if (player.getCooldowns().isOnCooldown(stack)) {
        
        return InteractionResult.FAIL;
    }

    if (level.isClientSide()) {
        return InteractionResult.SUCCESS;
        
    }

    fireSonicBoom((ServerLevel) level, player);
    tryWindBurstLaunch(player);
    // คูลดาวน์ 2 วินาที
    player.getCooldowns().addCooldown(stack, 10);


    return InteractionResult.SUCCESS;
}

//
private void tryWindBurstLaunch(Player player) {
    // ใช้มุมมองปัจจุบันของผู้เล่น
    Vec3 look = player.getViewVector(1.0F).normalize();

    // ต้องก้มลงพอสมควร ไม่งั้นไม่เด้ง
    if (look.y > -0.55D) {
        return;
    }

    // ยิงเช็กพื้นใกล้ตัว
    HitResult hit = player.pick(3.25D, 1.0F, false);
    if (hit.getType() != HitResult.Type.BLOCK) {
        return;
    }

    // ถ้าอยู่กลางอากาศก็ยังใช้ได้ แต่แรงจะลดลงนิดหน่อย
    boolean grounded = player.onGround();

    double upBoost = grounded ? 1.0D : 0.95D;
    double backBoost = grounded ? 1.0D : 0.95D;

    // ทิศดีด = ย้อนจากทิศที่มอง + ดันขึ้นบน
    Vec3 launch = new Vec3(
            -look.x * backBoost,
            upBoost,
            -look.z * backBoost
    );

    // ลดความเร็วเดิมก่อน กันพุ่งซ้อนแรงเกินไป
    Vec3 current = player.getDeltaMovement();
    player.setDeltaMovement(current.x * 0.20D, Math.max(current.y, 0.0D), current.z * 0.20D);

    // ดีดตัว
    player.addDeltaMovement(launch);

    // กันตกเจ็บหลังดีด
    player.fallDistance = 0.0F;
    player.hurtMarked = true;
}








//

private void fireSonicBoom(ServerLevel level, Player player) {
    double range = 16.0D;
    double beamRadius = 2.0D;
    float damage = 3F;

    Vec3 start = player.getEyePosition();
    Vec3 direction = player.getViewVector(1.0F).normalize();
    Vec3 end = start.add(direction.scale(range));

    // เสียงคลื่นพลัง
    level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.WARDEN_ROAR,
            SoundSource.PLAYERS,
            0.5F,
            0.25F
    );

    // อนุภาคเป็นแนวคลื่น
    int steps = 24;
    for (int i = 0; i <= steps; i++) {
        double t = (double) i / steps;
        Vec3 particlePos = start.lerp(end, t);

        level.sendParticles(
                ParticleTypes.SONIC_BOOM,
                particlePos.x,
                particlePos.y,
                particlePos.z,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
        );
    }

    

    // กล่องค้นหาเป้าหมายตามแนวสายตา
    AABB searchBox = player.getBoundingBox()
            .expandTowards(direction.scale(range))
            .inflate(beamRadius + 0.5D);

    List<LivingEntity> targets = level.getEntitiesOfClass(
            LivingEntity.class,
            searchBox,
            target -> target != player && target.isAlive()
    );

    for (LivingEntity living : targets) {
        Vec3 targetPos = living.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D);
        Vec3 toTarget = targetPos.subtract(start);

        double forwardDistance = toTarget.dot(direction);
        if (forwardDistance < 0.0D || forwardDistance > range) {
            continue;
        }

        double sideDistance = toTarget.subtract(direction.scale(forwardDistance)).length();
        if (sideDistance > beamRadius) {
            continue;
        }

        // ดาเมจแบบ sonic boom
        living.hurt(level.damageSources().sonicBoom(player), damage);
        living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,40,1,false,true,true));
        living.addEffect(new MobEffectInstance(MobEffects.WITHER,60,1,false,true,true));
        living.addEffect(new MobEffectInstance(MobEffects.LEVITATION,40,1,false,true,true));



        // ดาเมจแบบ sonic boom
living.hurt(level.damageSources().sonicBoom(player), damage);

// พาร์ติเคิลติดที่ตัวม็อบตอนโดน

level.sendParticles(
        ParticleTypes.SONIC_BOOM,
        living.getX(),
        living.getY() + living.getBbHeight() * 0.5D,
        living.getZ(),
        1,      // จำนวน
        0.0D,
        0.0D,
        0.0D,
        0.0D
);
level.sendParticles(
        ParticleTypes.SCULK_SOUL,
        living.getX(),
        living.getY() + living.getBbHeight() * 0.5D,
        living.getZ(),
        6,      // จำนวน
        0.05D,
        0.01D,
        0.01D,
        0.01D
);




level.sendParticles(
        ParticleTypes.SCULK_SOUL,
        living.getX(),
        living.getY() + living.getBbHeight() * 0.6D,
        living.getZ(),
        
        18,     // จำนวน
        0.25D,  // กระจายแกน X
        0.35D,  // กระจายแกน Y
        0.25D,  // กระจายแกน Z
        0.02D   // ความเร็วกระจาย
);
        


        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 2, false, false, true));
        

        // ผลักเป้าหมายไปด้านหน้าเล็กน้อย
        Vec3 knockback = direction.scale(1.2D).add(0.0D, 0.2D, 0.0D);
        living.addDeltaMovement(knockback);
living.hurtMarked = true;
    }
}


/////

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            player.removeEffect(MobEffects.SPEED);
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 2, false, false, true));

        }

        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, false, true));

        super.postHurtEnemy(stack, target, attacker);

           restoreDurabilityOnKill(stack, target);
    }


    
    private static int getKillCount(ItemStack stack) {
        return stack.getOrDefault(ModComponents.KILL_COUNT, 0);
    }

    private static double getBonusDamage(ItemStack stack) {
        return getKillCount(stack) * 0.25;
    }

    private static double getStoredDamage(ItemStack stack) {
        return BASE_DAMAGE + getBonusDamage(stack);
    }

    public static void syncEbonyAttributes(ItemStack stack) {
        int kills = getKillCount(stack);
        double totalDamage = BASE_DAMAGE + (kills * 0.25);

        double itemDamageModifierAmount = totalDamage - PLAYER_BASE_ATTACK_DAMAGE;
        double itemSpeedModifierAmount = BASE_ATTACK_SPEED_TOTAL - PLAYER_BASE_ATTACK_SPEED;

        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                EBONY_ATTACK_DAMAGE_ID,
                                itemDamageModifierAmount,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                EBONY_ATTACK_SPEED_ID,
                                itemSpeedModifierAmount,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
    }



    //
    
private void restoreDurabilityOnKill(ItemStack stack, LivingEntity target) {
    if (!(target instanceof Mob)) {
        return;
    }

    if (!stack.isDamageableItem()) {
        return;
    }

    if (!target.isDeadOrDying()) {
        return;
    }

    int repairedDamage = Math.max(0, stack.getDamageValue() - 10);
    stack.setDamageValue(repairedDamage);
}
    //
}

// .\gradlew.bat runClient