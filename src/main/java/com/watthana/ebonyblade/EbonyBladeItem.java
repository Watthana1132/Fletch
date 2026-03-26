package com.watthana.ebonyblade;

import java.util.List;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;

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
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 10, 2, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 10, 3, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 10, 2, false, false, true));


            if (player.isSprinting()) {
                // กำลังวิ่ง: ให้ติดเอฟเฟกต์หิว และลบ Saturation ออก
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 1, false, false, false));
                player.removeEffect(MobEffects.SATURATION);
            } else {
                // ไม่ได้วิ่ง: ลบเอฟเฟกต์หิว แล้วเพิ่ม Saturation
                player.removeEffect(MobEffects.HUNGER);
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 0, false, false, false));
            }
            
            if (player.hasEffect(MobEffects.SPEED)) {
                player.removeEffect(MobEffects.SLOWNESS);
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 10, 1, false, false, true));
            }

            if (player.isSprinting()) {
                // ถ้ากด Shift: ให้ม็อบในระยะ 5 บล็อกติด Levitation
                List<LivingEntity> nearbyMobs = player.level().getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(5.0),
                        target -> target != player && target instanceof Mob);

                for (LivingEntity mob : nearbyMobs) {
                    mob.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 14, false, false, false));
                }

            } else {
                // ถ้าไม่ได้กด Shift: ลบ Levitation จากม็อบในระยะ 5 บล็อก
                List<LivingEntity> nearbyMobs = player.level().getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(5.0),
                        target -> target != player && target instanceof Mob);

                for (LivingEntity mob : nearbyMobs) {
                    mob.removeEffect(MobEffects.LEVITATION);
                }
            }
        }



        if (holdingThisBladeInMainHand) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 10, 5, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 10, 2, false, false, true));

            if (player.isSprinting()) {
                // กำลังวิ่ง: ให้ติดเอฟเฟกต์หิว และลบ Saturation ออก
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 1, false, false, false));
                player.removeEffect(MobEffects.SATURATION);
            } else {
                // ไม่ได้วิ่ง: ลบเอฟเฟกต์หิว แล้วเพิ่ม Saturation
                player.removeEffect(MobEffects.HUNGER);
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 0, false, false, false));
            }

            if (player.isShiftKeyDown()) {
                // ถ้ากด Shift: ให้ม็อบในระยะ 5 บล็อกติด Levitation
                List<LivingEntity> nearbyMobs = player.level().getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(5.0),
                        target -> target != player && target instanceof Mob);

                for (LivingEntity mob : nearbyMobs) {
                    mob.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 80, 14, false, false, false));
                }

            } else {
                // ถ้าไม่ได้กด Shift: ลบ Levitation จากม็อบในระยะ 5 บล็อก
                List<LivingEntity> nearbyMobs = player.level().getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(5.0),
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

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            player.removeEffect(MobEffects.SPEED);
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 2, false, false, true));

        }

        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 1, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, false, true));

        super.postHurtEnemy(stack, target, attacker);
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
    
}

// .\gradlew.bat runClient