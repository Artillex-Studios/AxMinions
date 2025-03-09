package com.artillexstudios.axminions.utils;

@FunctionalInterface
public interface QuadFunction<A, B, C, D, E> {

    A apply(B input1, C input2, D input3, E input4);
}
