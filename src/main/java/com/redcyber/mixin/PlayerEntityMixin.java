package com.redcyber.mixin;

import com.redcyber.core.CyberAccess;
import com.redcyber.core.CyberSlot;
import com.redcyber.core.SurgeryManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.Map;

/**
 * Хранит установленные импланты в NBT игрока и держит игрока лежащим на столе,
 * пока открыта сессия операции.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements CyberAccess {
    @Unique
    private Map<CyberSlot, String> redcyber$parts;

    @Override
    public Map<CyberSlot, String> redcyber$getParts() {
        if (this.redcyber$parts == null) {
            this.redcyber$parts = new EnumMap<>(CyberSlot.class);
        }
        return this.redcyber$parts;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void redcyber$writeNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound tag = new NbtCompound();
        this.redcyber$getParts().forEach((slot, part) -> tag.putString(slot.lowerName(), part));
        nbt.put("RedCyberParts", tag);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void redcyber$readNbt(NbtCompound nbt, CallbackInfo ci) {
        Map<CyberSlot, String> parts = this.redcyber$getParts();
        parts.clear();
        if (nbt.contains("RedCyberParts", NbtElement.COMPOUND_TYPE)) {
            NbtCompound tag = nbt.getCompound("RedCyberParts");
            for (CyberSlot slot : CyberSlot.values()) {
                String value = tag.getString(slot.lowerName());
                if (!value.isEmpty()) {
                    parts.put(slot, value);
                }
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void redcyber$tick(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self.getWorld().isClient()) {
            return;
        }
        SurgeryManager.onPlayerTick(self);
    }
}
