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

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.geom.Matrix3f;
import org.terasology.math.geom.Matrix4f;
import org.terasology.rendering.assets.material.BaseMaterial;
import org.terasology.rendering.assets.material.MaterialData;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;

/**
 * Apparently, we don't do anything with materials other than use them as a texture storehouse.
 */
public class AwtMaterial extends BaseMaterial {
    private Map<String, Texture> textureMap = new HashMap<String, Texture>();

    public AwtMaterial(ResourceUrn urn, AssetType<?, MaterialData> assetType, MaterialData data) {
        super(urn, assetType);
        reload(data);
    }

	@Override
	public boolean isRenderable() {
        for (Texture texture : textureMap.values()) {
            if (!texture.isLoaded()) {
                return false;
            }
        }
        return true;
	}

	@Override
	protected void doReload(MaterialData newData) {
        textureMap.clear();
	}

    @Override
    public void setTexture(String name, Texture texture) {
        this.textureMap.put(name, texture);
    }

    public Texture getTexture(String name) {
        return this.textureMap.get(name);
    }

    @Override
    public void recompile() {
        // Do nothing
    }

    @Override
    public void enable() {
        // Do nothing
    }

    @Override
    public void setFloat(String name, float f, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat1(String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat2(String name, float f1, float f2, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat2(String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat3(String name, float f1, float f2, float f3, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat3(String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat4(String name, float f1, float f2, float f3, float f4, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat4(String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setInt(String name, int i, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setBoolean(String name, boolean value, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setMatrix3(String name, Matrix3f matrix, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setMatrix3(String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setMatrix4(String name, Matrix4f matrix, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setMatrix4(String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public boolean supportsFeature(ShaderProgramFeature feature) {
        return false;
    }

    @Override
    public void activateFeature(ShaderProgramFeature feature) {
        // Do nothing
    }

    @Override
    public void deactivateFeature(ShaderProgramFeature feature) {
        // Do nothing
    }

    @Override
    public void deactivateFeatures(ShaderProgramFeature ... features) {
        // Do nothing
    }

    @Override
    public void bindTextures() {
        // Do nothing
    }
}
