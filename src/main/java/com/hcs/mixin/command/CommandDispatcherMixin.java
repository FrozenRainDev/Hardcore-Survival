package com.hcs.mixin.command;

import com.hcs.main.helper.EntityHelper;
import com.hcs.misc.accessor.StatAccessor;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;

import static com.hcs.main.helper.EntityHelper.thePlayer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@Mixin(CommandDispatcher.class)
@Deprecated
public class CommandDispatcherMixin {
    @Deprecated LiteralCommandNode<ServerCommandSource> hcs = literal("hcs").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)).build();
    @Deprecated LiteralCommandNode<ServerCommandSource> setThirst = literal("setThirst").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)).then(argument("thirstVal", FloatArgumentType.floatArg(0.0F, 1.0F)).executes(ctx -> setThirst(ctx.getArgument("thirstVal", Float.class)))).build();
    @Deprecated LiteralCommandNode<ServerCommandSource> setHunger = literal("setHunger").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)).then(argument("hungerVal", IntegerArgumentType.integer(0, 20)).executes(ctx -> setHunger(ctx.getArgument("hungerVal", Integer.class)))).build();
    @Deprecated LiteralCommandNode<ServerCommandSource> getMainHandNbt = literal("getMainHandNbt").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)).executes(ctx -> getMainHandNbt(Objects.requireNonNull(ctx.getSource().getPlayer()))).build();
    @Deprecated LiteralCommandNode<ServerCommandSource> setSaturation = literal("setSaturation").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)).then(argument("saturationVal", IntegerArgumentType.integer(0, 20)).executes(ctx -> setSaturation(ctx.getArgument("saturationVal", Integer.class)))).build();


    //Delete this when output because of obfuscation mapping abnormal

/*
    @Inject(at = @At("RETURN"), method = "register")
    public void register(LiteralArgumentBuilder<?> command, CallbackInfoReturnable<LiteralCommandNode<ServerCommandSource>> cir) {
        if(Objects.equals(command.getLiteral(), "debug")){
            hcs.addChild(setThirst);
            hcs.addChild(setHunger);
            hcs.addChild(getMainHandNbt);
            hcs.addChild(setSaturation);
            cir.getReturnValue().addChild(hcs);
        }
    }
 */


    private static int setThirst(float val) {
        if (thePlayer != null) ((StatAccessor) thePlayer).getThirstManager().set(val);
        return Command.SINGLE_SUCCESS;
    }

    private int setHunger(int val) {
        if (thePlayer != null) thePlayer.getHungerManager().setFoodLevel(val);
        return Command.SINGLE_SUCCESS;
    }

    private int setSaturation(int val) {
        if (thePlayer != null) thePlayer.getHungerManager().setSaturationLevel(val);
        return Command.SINGLE_SUCCESS;
    }

    private int getMainHandNbt(@NotNull PlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        if (stack.hasNbt()) {
            assert stack.getNbt() != null;
            EntityHelper.msg(player, stack.getNbt().toString(), false);
            return Command.SINGLE_SUCCESS;
        }
        EntityHelper.msg(player, "null", false);
        return 0;
    }

}
