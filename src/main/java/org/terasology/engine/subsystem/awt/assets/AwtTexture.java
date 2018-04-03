/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.subsystem.awt.assets;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.Border;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.nui.Color;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AwtTexture extends Texture {

    private static int idCounter;

    private final TextureResources resources;

    public AwtTexture(ResourceUrn urn, AssetType<?, TextureData> assetType, TextureData data) {
        super(urn, assetType);
        this.resources = new TextureResources();
        getDisposalHook().setDisposeAction(resources);
        reload(data);
        

    }

    public void setId(int id) {
        resources.id = id;
    }

    @Override
    protected void doReload(TextureData data) {
        resources.id = idCounter++;
        resources.loadedTextureInfo = new LoadedTextureInfo(data);
        // TODO: Might need to handle 3d resources differently when data.getType() == TEXTURE3D
        // see org.terasology.rendering.opengl.OpenGLTexture.doReload(TextureData data)
    }

    @Override
    public int getId() {
        return resources.id;
    }

    @Override
    public int getDepth() {
        if (resources.loadedTextureInfo != null) {
            return resources.loadedTextureInfo.getDepth();
        }
        return 0;
    }

    @Override
    public int getWidth() {
        if (resources.loadedTextureInfo != null) {
            return resources.loadedTextureInfo.getWidth();
        }
        return 0;
    }

    @Override
    public int getHeight() {
        if (resources.loadedTextureInfo != null) {
            return resources.loadedTextureInfo.getHeight();
        }
        return 0;
    }

    @Override
    public Vector2i size() {
        return new Vector2i(getWidth(), getHeight());
    }

    @Override
    public Texture.WrapMode getWrapMode() {
        return resources.loadedTextureInfo.getWrapMode();
    }

    @Override
    public FilterMode getFilterMode() {
        return resources.loadedTextureInfo.getFilterMode();
    }

    @Override
    public TextureData getData() {
        return new TextureData(resources.loadedTextureInfo.getTextureData());
    }

    @Override
    public Texture getTexture() {
        return this;
    }

    @Override
    public Rect2f getRegion() {
        return FULL_TEXTURE_REGION;
    }

    @Override
    public Rect2i getPixelRegion() {
        return Rect2i.createFromMinAndSize(0, 0, getWidth(), getHeight());
    }

    @Override
    public synchronized void subscribeToDisposal(Runnable subscriber) {
        resources.disposalSubscribers.add(subscriber);
    }

    @Override
    public synchronized void unsubscribeToDisposal(Runnable subscriber) {
        resources.disposalSubscribers.remove(subscriber);
    }

    @Override
    public boolean isLoaded() {
        return resources.id != 0;
    }

    private static class LoadedTextureInfo {
        private final TextureData textureData;

         LoadedTextureInfo(TextureData data) {
            this.textureData = data;
        }

        public int getWidth() {
            return textureData.getWidth();
        }

        public int getHeight() {
            return textureData.getHeight();
        }

        public WrapMode getWrapMode() {
            return textureData.getWrapMode();
        }

        public FilterMode getFilterMode() {
            return textureData.getFilterMode();
        }

        public int getDepth() {
            return 1;
        }

        public TextureData getTextureData() {
            return textureData;
        }
    }

    private static class TextureResources implements Runnable {

        private volatile int id;
        private volatile LoadedTextureInfo loadedTextureInfo;

        private final Map<BufferedImageCacheKey, BufferedImage> bufferedImageByParametersMap = new HashMap<BufferedImageCacheKey, BufferedImage>();
        private final Map<BufferedImageCacheKey, BufferedImage> cachedBorderedTextures = Maps.newHashMap();

        private final List<Runnable> disposalSubscribers = Lists.newArrayList();

        TextureResources() {
        }


        @Override
        public void run() {
            if (loadedTextureInfo != null) {
                disposalSubscribers.forEach(java.lang.Runnable::run);
                loadedTextureInfo = null;
                id = 0;
                
                bufferedImageByParametersMap.clear();
                cachedBorderedTextures.clear();

            }
        }
    }


    public synchronized BufferedImage getBufferedImage(int width, int height, float alpha, Color color) {
        BufferedImageCacheKey key = new BufferedImageCacheKey(width, height, alpha, color);

        BufferedImage bufferedImage = resources.bufferedImageByParametersMap.get(key);

        if (null == bufferedImage) {
            ByteBuffer[] buffers = getData().getBuffers();
            ByteBuffer byteBuffer = buffers[0];

            final IntBuffer buf = byteBuffer.asIntBuffer();
            DataBuffer dataBuffer;
            if (!color.equals(Color.WHITE)) {
                dataBuffer = new IntBufferBackedDataBufferAlphaAndColor(buf, alpha, color);
            } else if (alpha != 1f) {
                dataBuffer = new IntBufferBackedDataBufferAlphaOnly(buf, alpha);
            } else {
                dataBuffer = new IntBufferBackedDataBufferUnmodified(buf);
            }
            SampleModel sm = new SinglePixelPackedSampleModel(
                    DataBuffer.TYPE_INT,
                    width,
                    height,
                    new int[]{0xFF000000, 0xFF0000, 0xFF00, 0xFF});
            // WritableRaster raster = Raster.createWritableRaster(sm, dataBuffer, null);
            WritableRaster raster = new WritableRaster(sm, dataBuffer, new java.awt.Point()) {
            };
            bufferedImage = new BufferedImage(
                    new DirectColorModel(32, 0xFF000000, 0xFF0000, 0xFF00, 0xFF),
                    raster, false, null);

            BufferedImage compatibleBufferedImage = createCompatibleImage(bufferedImage);

            resources.bufferedImageByParametersMap.put(key, compatibleBufferedImage);
        }

        return bufferedImage;
    }

    /**
     * This content is from Stack Overflow.
     * http://stackoverflow.com/questions/6319465/fast-loading-and-drawing-of-rgb-data-in-bufferedimage
     * http://creativecommons.org/licenses/by-sa/3.0/
     * by awinbra
     * http://stackoverflow.com/users/167884/awinbra
     * 
     * Apparently, it's ok to use this code without doing anything further than the above:
     * http://meta.stackoverflow.com/questions/139698/re-using-ideas-or-small-pieces-of-code-from-stackoverflow-com#139701
     * 
     * Note that there are several optimizations available above what this method provides in the same stackoverflow page.
     * We might want to look at this at some point.
     * 
    */
    private synchronized BufferedImage createCompatibleImage(BufferedImage image) {
        //  worked for msteiger, just in case what we're doing below breaks it again
        //      BufferedImage bufferedImageArgb = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        BufferedImage newImage = gc.createCompatibleImage(
                image.getWidth(),
                image.getHeight(),
                Transparency.TRANSLUCENT);

        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return newImage;
    }

    public synchronized BufferedImage getCachedBorderTexture(BufferedImageCacheKey key) {
        return resources.cachedBorderedTextures.get(key);
    }

    public synchronized BufferedImage putCachedBorderTexture(BufferedImageCacheKey key, BufferedImage bufferedImage) {
        return resources.cachedBorderedTextures.put(key, bufferedImage);
    }

    /**
     * A key that identifies an entry in the bufferedImage cache. It contains the elements that affect the generation of mesh for texture rendering.
     */
    public static class BufferedImageCacheKey {

        private Vector2i textureSize;
        private Vector2i areaSize;
        private Border border;
        private boolean tiled;
        private float uw;
        private float uh;
        private float alpha;
        private Color color;

        public BufferedImageCacheKey(int width, int height, float alpha, Color color) {
            this.textureSize = new Vector2i(width, height);
            this.alpha = alpha;
            this.color = color;
        }

        public BufferedImageCacheKey(Vector2i textureSize, Vector2i areaSize, Border border, boolean tiled, float uw, float uh, float alpha) {
            this.textureSize = new Vector2i(textureSize);
            this.areaSize = new Vector2i(areaSize);
            this.border = border;
            this.tiled = tiled;
            this.uw = uw;
            this.uh = uh;
            this.alpha = alpha;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof BufferedImageCacheKey) {
                BufferedImageCacheKey other = (BufferedImageCacheKey) obj;
                return Objects.equals(textureSize, other.textureSize)
                       && Objects.equals(areaSize, other.areaSize)
                       && Objects.equals(border, other.border)
                       && tiled == other.tiled
                       && uw == other.uw
                       && uh == other.uh
                       && alpha == other.alpha
                       && Objects.equals(color, other.color);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(textureSize, areaSize, border, tiled, uw, uh, alpha);
        }
    }

    public static class IntBufferBackedDataBufferAlphaAndColor extends DataBuffer {
        private final IntBuffer buf;
        private final float red;
        private final float green;
        private final float blue;
        private final float alpha;

        public IntBufferBackedDataBufferAlphaAndColor(IntBuffer buf, float alpha, Color color) {
            super(DataBuffer.TYPE_INT, buf.limit());
            this.buf = buf;
            this.red = color.rf();
            this.green = color.gf();
            this.blue = color.bf();
            this.alpha = alpha * color.af();
        }

        @Override
        public int getElem(int bank, int i) {
            int v = buf.get(i);
            return ((int) ((((v & 0xFF000000) >> 24) * red)) << 24)
                   | ((int) ((((v & 0xFF0000) >> 16) * green)) << 16)
                   | ((int) ((((v & 0xFF00) >> 8) * blue)) << 8)
                   | ((int) ((v & 0xFF) * alpha));
        }

        @Override
        public void setElem(int bank, int i, int val) {
        }
    }

    public static final class IntBufferBackedDataBufferAlphaOnly extends DataBuffer {
        private final IntBuffer buf;
        private final float alpha;

        public IntBufferBackedDataBufferAlphaOnly(IntBuffer buf, float alpha) {
            super(DataBuffer.TYPE_INT, buf.limit());
            this.buf = buf;
            this.alpha = alpha;
        }

        @Override
        public int getElem(int bank, int i) {
            int v = buf.get(i);
            return (v & 0xFFFFFF00)
                   | ((int) ((v & 0xFF) * alpha));
        }

        @Override
        public void setElem(int bank, int i, int val) {
        }
    }

    public static final class IntBufferBackedDataBufferUnmodified extends DataBuffer {
        private final IntBuffer buf;

        public IntBufferBackedDataBufferUnmodified(IntBuffer buf) {
            super(DataBuffer.TYPE_INT, buf.limit());
            this.buf = buf;
        }

        @Override
        public int getElem(int bank, int i) {
            int v = buf.get(i);
            return v;
        }

        @Override
        public void setElem(int bank, int i, int val) {
        }
    }

}
