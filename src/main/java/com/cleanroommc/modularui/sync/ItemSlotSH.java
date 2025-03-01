package com.cleanroommc.modularui.sync;

import com.cleanroommc.modularui.api.sync.SyncHandler;
import com.cleanroommc.modularui.utils.ClickData;
import com.cleanroommc.modularui.widgets.slot.ICustomSlot;
import com.cleanroommc.modularui.widgets.slot.SlotCustomSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Predicate;

/**
 * This does NOT sync items between server and client. It makes sure there is a
 * mc slot in the container class, which will handle syncing.
 * This will also handle interaction in case this is a phantom slot.
 */
public class ItemSlotSH extends SyncHandler {

    private final SlotCustomSlot customSlot;
    private final Slot slot;
    private Predicate<ItemStack> filter;
    private String slotGroup;

    private boolean phantom = false;
    private ItemStack lastStoredPhantomItem = ItemStack.EMPTY;

    public static ItemSlotSH phantom(Slot slot) {
        return new ItemSlotSH(slot).phantom(true);
    }

    public static ItemSlotSH phantom(IItemHandlerModifiable itemHandler, int index) {
        return new ItemSlotSH(itemHandler, index).phantom(true);
    }

    public static ItemSlotSH phantom(int slotLimit, Predicate<ItemStack> filter) {
        ItemStackHandler itemStackHandler = new ItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) {
                return slotLimit;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return filter == null || filter.test(stack);
            }
        };
        return phantom(itemStackHandler, 0);
    }

    public static ItemSlotSH phantom(int slotLimit) {
        return phantom(slotLimit, null);
    }

    public static ItemSlotSH phantom() {
        return phantom(64, null);
    }

    public ItemSlotSH(Slot slot) {
        this.slot = slot;
        this.customSlot = slot instanceof SlotCustomSlot ? (SlotCustomSlot) slot : null;
    }

    public ItemSlotSH(IItemHandlerModifiable itemHandler, int index) {
        this(new SlotCustomSlot(itemHandler, index, 0, 0));
    }

    public ItemSlotSH(IInventory itemHandler, int index) {
        this(new Slot(itemHandler, index, 0, 0));
    }

    @Override
    public void init(String key, GuiSyncHandler syncHandler) {
        super.init(key, syncHandler);
        syncHandler.getContainer().registerSlot(this);
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 4) {
            setEnabled(buf.readBoolean(), false);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == 2) {
            phantomClick(ClickData.readPacket(buf));
        } else if (id == 3) {
            phantomScroll(ClickData.readPacket(buf));
        } else if (id == 4) {
            setEnabled(buf.readBoolean(), false);
        } else if (id == 5) {
            ItemStack stack = buf.readItemStack();
            this.slot.putStack(stack);
        }
    }

    protected void phantomClick(ClickData clickData) {
        ItemStack cursorStack = getSyncHandler().getCursorItem();
        ItemStack slotStack = getSlot().getStack();
        ItemStack stackToPut;
        if (slotStack.isEmpty()) {
            if (cursorStack.isEmpty()) {
                if (clickData.mouseButton == 1 && !this.lastStoredPhantomItem.isEmpty()) {
                    stackToPut = this.lastStoredPhantomItem.copy();
                } else {
                    return;
                }
            } else {
                stackToPut = cursorStack.copy();
            }
            if (clickData.mouseButton == 1) {
                stackToPut.setCount(1);
            }
            slot.putStack(stackToPut);
            this.lastStoredPhantomItem = stackToPut.copy();
        } else {
            if (clickData.mouseButton == 0) {
                if (clickData.shift) {
                    this.slot.putStack(ItemStack.EMPTY);
                } else {
                    incrementStackCount(-1);
                }
            } else if (clickData.mouseButton == 1) {
                incrementStackCount(1);
            }
        }
    }

    protected void phantomScroll(ClickData clickData) {
        ItemStack currentItem = this.slot.getStack();
        int amount = clickData.mouseButton;
        if (clickData.shift) {
            amount *= 4;
        }
        if (clickData.ctrl) {
            amount *= 16;
        }
        if (clickData.alt) {
            amount *= 64;
        }
        if (amount > 0 && currentItem.isEmpty() && !lastStoredPhantomItem.isEmpty()) {
            ItemStack stackToPut = this.lastStoredPhantomItem.copy();
            stackToPut.setCount(amount);
            this.slot.putStack(stackToPut);
        } else {
            incrementStackCount(amount);
        }
    }

    public void incrementStackCount(int amount) {
        ItemStack stack = getSlot().getStack();
        if (stack.isEmpty()) {
            return;
        }
        int oldAmount = stack.getCount();
        if (amount < 0) {
            amount = Math.max(0, oldAmount + amount);
        } else {
            if (Integer.MAX_VALUE - amount < oldAmount) {
                amount = Integer.MAX_VALUE;
            } else {
                int maxSize = getSlot().getSlotStackLimit();
                if ((this.customSlot == null || !this.customSlot.isIgnoreMaxStackSize()) && stack.getMaxStackSize() < maxSize) {
                    maxSize = stack.getMaxStackSize();
                }
                amount = Math.min(oldAmount + amount, maxSize);
            }
        }
        if (oldAmount != amount) {
            stack.setCount(amount);
            getSlot().onSlotChanged();
        }
    }

    public void setEnabled(boolean enabled, boolean sync) {
        if (this.slot instanceof ICustomSlot) {
            ((ICustomSlot) this.slot).setEnabled(enabled);
            if (sync) {
                sync(4, buffer -> buffer.writeBoolean(enabled));
            }
        }
    }

    public void updateFromClient(ItemStack stack) {
        syncToServer(5, buf -> buf.writeItemStack(stack));
    }

    public Slot getSlot() {
        return slot;
    }

    public boolean isItemValid(ItemStack itemStack) {
        return getSlot().isItemValid(itemStack) && (this.filter == null || this.filter.test(itemStack));
    }

    public boolean isPhantom() {
        return phantom;
    }

    public String getSlotGroup() {
        return slotGroup;
    }

    public ItemSlotSH filter(Predicate<ItemStack> filter) {
        this.filter = filter;
        return this;
    }

    public ItemSlotSH phantom(boolean phantom) {
        this.phantom = phantom;
        return this;
    }

    public ItemSlotSH ignoreMaxStackSize(boolean ignore) {
        if (this.customSlot == null) {
            throw new IllegalStateException("Slot must be a SlotCustomSlot");
        }
        this.customSlot.setIgnoreMaxStackSize(ignore);
        return this;
    }

    public ItemSlotSH slotGroup(String name) {
        this.slotGroup = name;
        return this;
    }
}
