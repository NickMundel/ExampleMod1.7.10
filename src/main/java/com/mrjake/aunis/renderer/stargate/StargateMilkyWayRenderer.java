package com.mrjake.aunis.renderer.stargate;


import com.mrjake.aunis.loader.ElementEnum;
import com.mrjake.aunis.loader.texture.TextureLoader;
import com.mrjake.aunis.util.math.MathFunction;
import com.mrjake.aunis.util.math.MathFunctionImpl;
import com.mrjake.aunis.util.math.MathRange;
import com.mrjake.aunis.util.minecraft.MathHelper;
import com.mrjake.aunis.util.minecraft.Vec3d;
import org.lwjgl.opengl.GL11;

public class StargateMilkyWayRenderer extends StargateClassicRenderer<StargateMilkyWayRendererState> {

	private static final Vec3d RING_LOC = new Vec3d(0.0, -0.122333, -0.000597);
	private static final float GATE_DIAMETER = 10.1815f;

	@Override
    protected void applyTransformations(StargateMilkyWayRendererState rendererState) {
        GL11.glTranslated(0.50, GATE_DIAMETER/2 + rendererState.stargateSize.renderTranslationY, 0.50);
        GL11.glScaled(rendererState.stargateSize.renderScale, rendererState.stargateSize.renderScale, rendererState.stargateSize.renderScale);
    }

	@Override
    protected void renderGate(StargateMilkyWayRendererState rendererState, double partialTicks) {
        renderRing(rendererState, partialTicks);
        GL11.glRotated(rendererState.horizontalRotation, 0, 1, 0);
        renderChevrons(rendererState, partialTicks);

        ElementEnum.MILKYWAY_GATE.bindTextureAndRender(rendererState.getBiomeOverlay());
    }

	// ----------------------------------------------------------------------------------------
	// Ring

    private void renderRing(StargateMilkyWayRendererState rendererState, double partialTicks) {
        GL11.glPushMatrix();
        float angularRotation = rendererState.spinHelper.currentSymbol.getAngle();

        if (rendererState.spinHelper.isSpinning)
            angularRotation += rendererState.spinHelper.apply(getWorldObj().getTotalWorldTime() + partialTicks);

        if (rendererState.horizontalRotation == 90 || rendererState.horizontalRotation == 0)
            angularRotation *= -1;

        if (rendererState.horizontalRotation == 90 || rendererState.horizontalRotation == 270) {
            GL11.glTranslated(RING_LOC.y, RING_LOC.z, RING_LOC.x);
            GL11.glRotated(angularRotation, 1, 0, 0);
            GL11.glTranslated(-RING_LOC.y, -RING_LOC.z, -RING_LOC.x);
        }

        else {
            GL11.glTranslated(RING_LOC.x, RING_LOC.z, RING_LOC.y);
            GL11.glRotated(angularRotation, 0, 0, 1);
            GL11.glTranslated(-RING_LOC.x, -RING_LOC.z, -RING_LOC.y);
        }

        GL11.glRotated(rendererState.horizontalRotation, 0, 1, 0);

        ElementEnum.MILKYWAY_RING.bindTextureAndRender(rendererState.getBiomeOverlay());

        GL11.glPopMatrix();
    }


	// ----------------------------------------------------------------------------------------
	// Chevrons

	private static MathRange chevronOpenRange = new MathRange(0, 1.57f);
	private static MathFunction chevronOpenFunction = new MathFunctionImpl(x -> x*x*x*x/80f);

	private static MathRange chevronCloseRange = new MathRange(0, 1.428f);
	private static MathFunction chevronCloseFunction = new MathFunctionImpl(x0 -> MathHelper.cos(x0*1.1f) / 12f);

	private float calculateTopChevronOffset(StargateMilkyWayRendererState rendererState, double partialTicks) {
		float tick = (float) (getWorldObj().getTotalWorldTime() - rendererState.chevronActionStart + partialTicks);
		float x = tick / 6.0f;

		if (rendererState.chevronOpening) {
			if (chevronOpenRange.test(x))
				return chevronOpenFunction.apply(x);
			else {
				rendererState.chevronOpen = true;
				rendererState.chevronOpening = false;
			}
		}

		else if (rendererState.chevronClosing) {
			if (chevronCloseRange.test(x))
				return chevronCloseFunction.apply(x);
			else {
				rendererState.chevronOpen = false;
				rendererState.chevronClosing = false;
			}
		}

		return rendererState.chevronOpen ? 0.08333f : 0;
	}

	@Override
    protected void renderChevron(StargateMilkyWayRendererState rendererState, double partialTicks, ChevronEnum chevron) {
        GL11.glPushMatrix();

        GL11.glRotated(chevron.rotation, 0, 0, 1);

        TextureLoader.getTexture(rendererState.chevronTextureList.get(rendererState.getBiomeOverlay(), chevron)).bindTexture();

        if (chevron.isFinal()) {
            float chevronOffset = calculateTopChevronOffset(rendererState, partialTicks);

            GL11.glPushMatrix();

            GL11.glTranslated(0, chevronOffset, 0);
            ElementEnum.MILKYWAY_CHEVRON_LIGHT.render();

            GL11.glTranslated(0, -2*chevronOffset, 0);
            ElementEnum.MILKYWAY_CHEVRON_MOVING.render();

            GL11.glPopMatrix();
        }

        else {
            ElementEnum.MILKYWAY_CHEVRON_MOVING.render();
            ElementEnum.MILKYWAY_CHEVRON_LIGHT.render();
        }

        ElementEnum.MILKYWAY_CHEVRON_FRAME.bindTextureAndRender(rendererState.getBiomeOverlay());
        ElementEnum.MILKYWAY_CHEVRON_BACK.render();

        GL11.glPopMatrix();
    }
}
