package com.radimous.nbtsizetooltip;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.List;

@Mod("nbtsizetooltip")
@Mod.EventBusSubscriber(modid = "nbtsizetooltip", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Nbtsizetooltip {

    public Nbtsizetooltip() {
    }

    static ItemStack hoveredStack = ItemStack.EMPTY;
    static int nbtSize = 0;
    static int vgdSize = 0;
    static int nbtvgdSize = 0;
    static int ccacheSize = 0;

    @SubscribeEvent
    public static void tooltip(final ItemTooltipEvent t) {
        CompoundTag nbt = t.getItemStack().getTag();
        if (nbt == null || !t.getFlags().isAdvanced()) {
            return;
        }
        List<Component> tooltips = t.getToolTip();
        ItemStack eventStack = t.getItemStack();
        if (!hoveredStack.equals(eventStack)) {
            nbtSize = getNbtSize(t.getItemStack());
            vgdSize = getVgdSize(t.getItemStack());
            nbtvgdSize = getNbtVgdSize(t.getItemStack());
            ccacheSize = getCcacheSize(t.getItemStack());
        }
        hoveredStack = eventStack;
        if (nbtSize > 0) {
            tooltips.add(new TextComponent("NBT Size: ").append(new TextComponent("" + nbtSize).withStyle(
                    nbtSize > 2000000 ? ChatFormatting.DARK_RED : ChatFormatting.GREEN)
                .append(new TextComponent(" bytes"))));
        }
        if (vgdSize > 0) {
            tooltips.add(new TextComponent("VGD Size: ").append(new TextComponent("" + vgdSize).withStyle(
                    vgdSize > 2000000 ? ChatFormatting.DARK_RED : ChatFormatting.GREEN)
                .append(new TextComponent(" bytes"))));
        }
        if (nbtvgdSize > 0) {
            tooltips.add(new TextComponent("Unpacked VGD Size: ").append(new TextComponent("" + nbtvgdSize).withStyle(
                    nbtvgdSize > 2000000 ? ChatFormatting.DARK_RED : ChatFormatting.GREEN)
                .append(new TextComponent(" bytes"))));
        }
        if (ccacheSize > 0) {
            tooltips.add(new TextComponent("CCache Size: ").append(new TextComponent("" + ccacheSize).withStyle(
                    ccacheSize > 2000000 ? ChatFormatting.DARK_RED : ChatFormatting.GREEN)
                .append(new TextComponent(" bytes"))));
        }
    }

    public static int getNbtSize(ItemStack stack) {
        return getNbtSize(stack.getTag());
    }

    public static int getVgdSize(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null) {
            return 0;
        }
        return getNbtSize(nbt.get("vaultGearData"));
    }

    public static int getNbtVgdSize(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null) {
            return 0;
        }
        return getNbtSize(nbt.get("nbtGearData"));
    }

    public static int getCcacheSize(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null) {
            return 0;
        }
        return getNbtSize(nbt.get("clientCache"));
    }


    public static int getNbtSize(Tag nbt) {
        if (nbt == null) {
            return 0;
        }
        CompoundTag compound;
        if (!(nbt instanceof CompoundTag)) {
            // this will slightly increase the size
            compound = new CompoundTag();
            compound.put("", nbt);
        } else {
            compound = (CompoundTag) nbt;
        }
        ByteBuf buf = Unpooled.buffer();
        try {
            NbtIo.write(compound, new ByteBufOutputStream(buf));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int cnt = buf.readableBytes();
//        byte[] bytes = new byte[cnt];
//        buf.readBytes(bytes);
//        printBytes(bytes);
        return cnt;
    }

    // example:
    // 10 0 0 3 0 6 D a m a g e 0 0 0 9 0
    // means:

    // 10 -> compound tag
    // 0 0 -> tag name length
    // 3 -> int tag
    // 0 6 -> tag name length
    // D a m a g e -> tag name
    // 0 0 0 9 -> tag value
    // 0 -> end tag

    private static void printBytes(byte[] bytes) {
        for (byte b : bytes) {
            // if ascii, print ascii, else print dec
            if (b >= 32 && b <= 126) {
                System.out.print((char) b + " ");
            } else {
                System.out.printf(b + " ");
            }
        }
        System.out.println();
    }
}
