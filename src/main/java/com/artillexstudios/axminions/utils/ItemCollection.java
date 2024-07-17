package com.artillexstudios.axminions.utils;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

public class ItemCollection extends ArrayList<ItemStack> {
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

    public ItemCollection(int size) {
        super(size);
    }

    public ItemCollection(Collection<ItemStack> collection) {
        super(collection);
    }
}
