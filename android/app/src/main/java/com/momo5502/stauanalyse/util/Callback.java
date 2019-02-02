package com.momo5502.stauanalyse.util;

public interface Callback<T> {
    void run(T value, Exception error);
}
