package com.infamous.dungeons_libraries.commands;

import com.infamous.dungeons_libraries.entities.elite.EliteMobEvents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

public class SummonEliteCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.summonelite.failed"));
    private static final SimpleCommandExceptionType ERROR_DUPLICATE_UUID = new SimpleCommandExceptionType(Component.translatable("commands.summonelite.failed.uuid"));
    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.summonelite.invalidPosition"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("summonelite")
            .requires((source) -> source.hasPermission(2))
            .then(Commands.argument("entity", ResourceLocationArgument.id())
                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES) // Use built-in summonable entities
                .executes((context) -> spawnEntity(
                    context.getSource(),
                    ResourceLocationArgument.getId(context, "entity"),
                    context.getSource().getPosition(),
                    new CompoundTag(),
                    true))
                .then(Commands.argument("pos", Vec3Argument.vec3())
                    .executes((context) -> spawnEntity(
                        context.getSource(),
                        ResourceLocationArgument.getId(context, "entity"),
                        Vec3Argument.getVec3(context, "pos"),
                        new CompoundTag(),
                        true))
                    .then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                        .executes((context) -> spawnEntity(
                            context.getSource(),
                            ResourceLocationArgument.getId(context, "entity"),
                            Vec3Argument.getVec3(context, "pos"),
                            CompoundTagArgument.getCompoundTag(context, "nbt"),
                            false))))));
    }

    private static int spawnEntity(CommandSourceStack p_138821_, ResourceLocation p_138822_, Vec3 p_138823_, CompoundTag p_138824_, boolean p_138825_) throws CommandSyntaxException {
        BlockPos blockpos = BlockPos.containing(p_138823_);
        if (!Level.isInSpawnableBounds(blockpos)) {
            throw INVALID_POSITION.create();
        } else {
          CompoundTag compoundtag = p_138824_.copy();
          compoundtag.putString("id", p_138822_.toString());
          ServerLevel serverlevel = p_138821_.getLevel();
          Entity entity = EntityType.loadEntityRecursive(compoundtag, serverlevel, (p_138828_) -> {
              p_138828_.moveTo(p_138823_.x, p_138823_.y, p_138823_.z, p_138828_.getYRot(), p_138828_.getXRot());
              return p_138828_;
          });
          if (entity == null) {
              throw ERROR_FAILED.create();
          } else {
              if (p_138825_ && entity instanceof Mob) {
                  ForgeEventFactory.onFinalizeSpawn(
                      (Mob) entity,
                      p_138821_.getLevel(),
                      p_138821_.getLevel().getCurrentDifficultyAt(entity.blockPosition()),
                      MobSpawnType.COMMAND,
                      null,  // SpawnGroupData
                      null   // CompoundTag
                  );
              }
              if(entity instanceof LivingEntity livingEntity) {
                  EliteMobEvents.makeElite(livingEntity);
              }
              if (!serverlevel.tryAddFreshEntityWithPassengers(entity)) {
                  throw ERROR_DUPLICATE_UUID.create();
              } else {
                  p_138821_.sendSuccess(() -> Component.translatable("commands.summon.success", entity.getDisplayName()), true);
                  return 1;
              }
          }
        }
    }
}