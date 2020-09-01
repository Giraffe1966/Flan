package com.flemmli97.flan.event;

import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.ClaimStorage;
import com.flemmli97.flan.claim.EnumPermission;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class WorldEvents {

    public static void modifyExplosion(Explosion explosion, ServerWorld world) {
        ClaimStorage storage = ClaimStorage.get(world);
        explosion.getAffectedBlocks().removeIf(pos -> {
            Claim claim = storage.getClaimAt(pos);
            if (claim != null)
                return !claim.canInteract(null, EnumPermission.EXPLOSIONS, pos);
            return false;
        });
    }

    public static boolean pistonCanPush(BlockState state, World world, BlockPos blockPos, Direction direction, Direction pistonDir) {
        if (world.isClient || direction == Direction.UP || direction == Direction.DOWN)
            return true;
        boolean empty = state.isAir() || state.getPistonBehavior() == PistonBehavior.DESTROY;
        BlockPos dirPos = blockPos.offset(direction);
        ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
        Claim from = storage.getClaimAt(blockPos);
        Claim to = storage.getClaimAt(dirPos);
        boolean flag = true;
        if (!empty) {
            if ((from != null && !from.equals(to)) || (from == null && to != null))
                flag = false;
        }
        if (from != null && from.equals(to)) {
            Claim opp = storage.getClaimAt(blockPos.offset(direction.getOpposite()));
            flag = from.equals(opp);
        }
        if (!flag) {
            world.updateListeners(blockPos, state, state, 20);
            BlockState toState = world.getBlockState(dirPos);
            world.updateListeners(dirPos, toState, toState, 20);
        }
        return flag;
    }

    public static boolean canFlow(BlockState fluidBlockState, BlockView world, BlockPos blockPos, Direction direction) {
        if (!(world instanceof ServerWorld) || direction == Direction.UP || direction == Direction.DOWN)
            return true;
        ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
        Claim from = storage.getClaimAt(blockPos);
        Claim to = storage.getClaimAt(blockPos.offset(direction));
        boolean fl = from == null && to == null;
        if (from != null)
            fl = from.equals(to);
        return fl;
    }

    public static boolean canStartRaid(ServerPlayerEntity player){
        Claim claim = ClaimStorage.get(player.getServerWorld()).getClaimAt(player.getBlockPos());
        if(claim!=null && !claim.canInteract(player, EnumPermission.RAID, player.getBlockPos()))
            return false;
        return true;
    }

    public static boolean canFireSpread(ServerWorld world, BlockPos pos){
        Claim claim = ClaimStorage.get(world).getClaimAt(pos);
        if(claim!=null && !claim.canInteract(null, EnumPermission.FIRESPREAD, pos))
            return false;
        return true;
    }
}
