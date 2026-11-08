package com.redcyber.mixin;

import com.redcyber.core.CyberAccess;
import com.redcyber.core.CyberSlot;
import com.redcyber.core.SurgeryManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Хранит установленные импланты в NBT игрока и держит игрока лежащим на столе,
 * пока открыта сессия операции.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements CyberAccess {
    @Unique
    private Map<CyberSlot, String> redcyber$parts;

    @Unique
    private Set<CyberSlot> redcyber$disabled;

    @Override
    public Map<CyberSlot, String> redcyber$getParts() {
        if (this.redcyber$parts == null) {
            this.redcyber$parts = new EnumMap<>(CyberSlot.class);
        }
        return this.redcyber$parts;
    }

    @Override
    public Set<CyberSlot> redcyber$getDisabled() {
        if (this.redcyber$disabled == null) {
            this.redcyber$disabled = EnumSet.noneOf(CyberSlot.class);
        }
        return this.redcyber$disabled;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void redcyber$writeNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound tag = new NbtCompound();
        this.redcyber$getParts().forEach((slot, part) -> tag.putString(slot.lowerName(), part));
        nbt.put("RedCyberParts", tag);

        NbtList off = new NbtList();
        for (CyberSlot slot : this.redcyber$getDisabled()) {
            off.add(NbtString.of(slot.lowerName()));
        }
        nbt.put("RedCyberDisabled", off);
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

        Set<CyberSlot> disabled = this.redcyber$getDisabled();
        disabled.clear();
        if (nbt.contains("RedCyberDisabled", NbtElement.LIST_TYPE)) {
            NbtList off = nbt.getList("RedCyberDisabled", NbtElement.STRING_TYPE);
            for (int i = 0; i < off.size(); i++) {
                for (CyberSlot slot : CyberSlot.values()) {
                    if (slot.lowerName().equals(off.getString(i))) {
                        disabled.add(slot);
                    }
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
