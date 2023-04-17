package com.yanzhenjie.album.gpu;

import com.daasuu.gpuv.egl.filter.GlFilter;

/**
 * @description:
 * @author: wj
 * @date :   2023/4/14 17:38
 */
public class GPUCamreaManager {

    private static GPUCamreaManager instance=new GPUCamreaManager();

    // cpucamera的滤镜
    private boolean isUseGPUCamera;
    private GlFilter glFilter;
    private  boolean isPortrait = true;

    public static GPUCamreaManager getInstance() {
        return instance;
    }

    public static void setInstance(GPUCamreaManager instance) {
        GPUCamreaManager.instance = instance;
    }

    public GlFilter getGlFilter() {
        return glFilter;
    }

    public void setGlFilter(GlFilter glFilter) {
        this.glFilter = glFilter;
    }

    public boolean isPortrait() {
        return isPortrait;
    }

    public void setPortrait(boolean portrait) {
        isPortrait = portrait;
    }

    public boolean isUseGPUCamera() {
        return isUseGPUCamera;
    }

    public void setUseGPUCamera(boolean useGPUCamera) {
        isUseGPUCamera = useGPUCamera;
    }

    public void reset() {
        isUseGPUCamera=false;
        isPortrait=true;
        glFilter=null;
    }
}
