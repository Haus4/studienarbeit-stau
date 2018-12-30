package com.momo5502.stauanalyse;

import java.util.List;

public interface CameraLoaderListener {

    void onLoad(List<Camera> cameras, Exception e);
}
