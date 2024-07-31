package com.artillexstudios.axminions.utils;

import com.artillexstudios.axapi.reflection.FastFieldAccessor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ItemCollection {
    private static final FastFieldAccessor ELEMENT_DATA = FastFieldAccessor.forClassField(ArrayList.class, "elementData");
    public static final ItemCollection EMPTY = new ItemCollection(0) {
        @Override
        public ItemStack remove(int index) {
            throw new RuntimeException();
        }

        @Override
        public void add(int index, ItemStack element) {
            throw new RuntimeException();
        }

        @Override
        public boolean addAll(Collection<? extends ItemStack> c) {
            throw new RuntimeException();
        }

        @Override
        public boolean addAll(int index, Collection<? extends ItemStack> c) {
            throw new RuntimeException();
        }

        @Override
        public boolean remove(Object o) {
            throw new RuntimeException();
        }

        @Override
        public boolean add(ItemStack itemStack) {
            throw new RuntimeException();
        }
    };

    private final List<ItemStack> items;

    public ItemCollection(int size) {
        this.items = new ArrayList<>(size);
    }

    public ItemCollection(Collection<ItemStack> collection) {
        LogUtils.debug("ItemCollection with class: {}", collection.getClass());
        this.items = collection instanceof List<ItemStack> list ? list : new ArrayList<>(collection);
    }

    public boolean add(ItemStack itemStack) {
        return this.items.add(itemStack);
    }

    public void add(int index, ItemStack element) {
        this.items.add(index, element);
    }

    public boolean addAll(Collection<? extends ItemStack> c) {
        return this.items.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends ItemStack> c) {
        return this.items.addAll(index, c);
    }

    public ItemStack get(int index) {
        return this.items.get(index);
    }

    public ItemStack remove(int index) {
        return this.items.remove(index);
    }

    public boolean remove(Object o) {
        return this.items.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        return this.items.removeAll(c);
    }

    public boolean removeIf(Predicate<? super ItemStack> filter) {
        return this.items.removeIf(filter);
    }

    public List<ItemStack> items() {
        return this.items;
    }

    public ItemStack[] elements() {
        return ELEMENT_DATA.get(this.items);
    }

    @NotNull
    public Iterator<ItemStack> iterator() {
        return this.items.iterator();
    }

    @NotNull
    public ListIterator<ItemStack> listIterator() {
        return this.items.listIterator();
    }

    @NotNull
    public ListIterator<ItemStack> listIterator(int index) {
        return this.items.listIterator(index);
    }

    public Stream<ItemStack> stream() {
        return this.items.stream();
    }

    @NotNull
    public Object[] toArray() {
        return this.items.toArray();
    }

    @NotNull
    public <T> T[] toArray(@NotNull T[] a) {
        return this.items.toArray(a);
    }

    public <T> T[] toArray(IntFunction<T[]> generator) {
        return this.items.toArray(generator);
    }

    public int size() {
        return this.items.size();
    }

    public void clear() {
        this.items.clear();
    }
}
