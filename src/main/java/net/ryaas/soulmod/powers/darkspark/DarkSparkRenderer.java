package net.ryaas.soulmod.powers.darkspark;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.ryaas.soulmod.SoulMod;
import net.ryaas.soulmod.powers.rg.RGModel;
import net.ryaas.soulmod.powers.rg.RedGiant;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;


public class DarkSparkRenderer extends EntityRenderer<DarkSpark> {

    public DarkSparkRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DarkSpark entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        System.out.println("DarkSparkRenderer is rendering entity ID=" + entity.getId());
        // If start == end, there's no length, so nothing to draw
        Vec3 startPos = entity.getStartPos();
        Vec3 endPos = entity.getEndPos();
        if (startPos.equals(endPos)) {
            return;
        }

        // 1) Calculate the direction vector
        Vec3 mainVec = endPos.subtract(startPos);
        double length = mainVec.length();

        // 2) Shift & rotate the rendering coordinate system so "up" = mainVec
        poseStack.pushPose();

        // Move so that the bolt starts at (0,0,0) in local space
        poseStack.translate(
                startPos.x - entity.getX(),
                startPos.y - entity.getY(),
                startPos.z - entity.getZ()
        );

        // Rotate
        Vec3 dirNorm = mainVec.normalize();
        Vec3 axis = new Vec3(0, 1, 0).cross(dirNorm);
        double dot = new Vec3(0, 1, 0).dot(dirNorm);
        double angle = Math.acos(dot);
        float angleDegrees = (float) Math.toDegrees(angle);

        // Only rotate if the axis isn't zero-length
        if (axis.lengthSqr() > 1.0E-8) {
            axis = axis.normalize();

            // In newer versions of MC (1.19+):
            //   com.mojang.math.Axis.of(vector).rotationDegrees(deg)
            // Or build a quaternion manually:
            poseStack.mulPose(
                    com.mojang.math.Axis.of(
                            new org.joml.Vector3f((float)axis.x, (float)axis.y, (float)axis.z)
                    ).rotationDegrees(angleDegrees)
            );
        }

        // 3) Duplicate the vanilla random "zig-zag" logic
        float[] xPos = new float[8];
        float[] zPos = new float[8];
        float xBase = 0.0F;
        float zBase = 0.0F;
        RandomSource rand = RandomSource.create(entity.seed);

        for (int i = 7; i >= 0; --i) {
            xPos[i] = xBase;
            zPos[i] = zBase;
            xBase += (float)(rand.nextInt(3) - 1);
            zBase += (float)(rand.nextInt(3) - 1);
        }

        VertexConsumer vc = buffer.getBuffer(RenderType.endGateway());
        Matrix4f matrix = poseStack.last().pose();

        // Vanilla lightning draws in 16-unit tall segments
        int totalSegments = (int) (length / 16.0) + 1;
        if (totalSegments < 1) {
            totalSegments = 1;
        }
        //CHANGE PASS AND STEP TO CHANGE HOW MANY BOLTS. ITS PASS * STEP = BOLTS
        for (int pass = 0; pass < 1; ++pass) {
            RandomSource passRand = RandomSource.create(entity.seed);

            for (int step = 0; step < 1; ++step) {
                int startIndex = 7;
                int endIndex = 0;
                if (step > 0) {
                    startIndex = 7 - step;
                    endIndex = startIndex - 2;
                }

                float xOld = xPos[startIndex];
                float zOld = zPos[startIndex];

                for (int idx = startIndex; idx >= endIndex; --idx) {
                    float xCur = xOld;
                    float zCur = zOld;

                    // Random offsets
                    if (step == 0) {
                        //CHANGE THESE NUMBERS TO INCREASE HOW FAR THE ZIGZAG IS
                        xOld += (float)(passRand.nextInt(3) - 1);
                        zOld += (float)(passRand.nextInt(3) - 1);
                    } else {
                        xOld += (float)(passRand.nextInt(3) - 1);
                        zOld += (float)(passRand.nextInt(3) - 1);
                    }

                    // Lightning color
                    float r = 0.45F;
                    float g = 0.45F;
                    float b = 0.5F;

                    int charge = entity.getChargeLevel(); // from the entity
                    float baseSize = 0.1F;               // thickness at charge 0
                    float extraPerCharge = 0.01F;

                    //CHANGE THIS FOR THICKNESS
                    float size1 = baseSize + (charge * extraPerCharge);
                    float size2 = size1; // or do something slightly different

                    // Now draw each 16-unit "layer" up the Y-axis
                    for (int seg = 0; seg < totalSegments; seg++) {
                        int baseY = seg * 16;
                        int nextY = (seg + 1) * 16;
                        if (nextY > length) {
                            nextY = (int) length;
                        }

                        // Draw the 4 corners:
                        quad(matrix, vc, xOld, zOld, baseY, xCur, zCur,
                                r, g, b, size1, size2, false, false, true, false);
                        quad(matrix, vc, xOld, zOld, baseY, xCur, zCur,
                                r, g, b, size1, size2, true, false, true, true);
                        quad(matrix, vc, xOld, zOld, baseY, xCur, zCur,
                                r, g, b, size1, size2, true, true, false, true);
                        quad(matrix, vc, xOld, zOld, baseY, xCur, zCur,
                                r, g, b, size1, size2, false, true, false, false);

                        if (nextY == (int) length) {
                            break;
                        }
                    }
                }
            }
        }

        poseStack.popPose();
    }

    /**
     * Same helper the vanilla renderer uses to draw a single rectangular face.
     */
    private static void quad(Matrix4f matrix, VertexConsumer consumer,
                             float x1, float z1, int y1,
                             float x2, float z2,
                             float r, float g, float b,
                             float size1, float size2,
                             boolean bool1, boolean bool2, boolean bool3, boolean bool4) {

        consumer.vertex(matrix, x1 + (bool1 ? size2 : -size2), (float) y1,
                        z1 + (bool2 ? size2 : -size2))
                .color(r, g, b, 0.3F)
                .endVertex();

        consumer.vertex(matrix, x2 + (bool1 ? size1 : -size1), (float) (y1 + 16),
                        z2 + (bool2 ? size1 : -size1))
                .color(r, g, b, 0.3F)
                .endVertex();

        consumer.vertex(matrix, x2 + (bool3 ? size1 : -size1), (float) (y1 + 16),
                        z2 + (bool4 ? size1 : -size1))
                .color(r, g, b, 0.3F)
                .endVertex();

        consumer.vertex(matrix, x1 + (bool3 ? size2 : -size2), (float) y1,
                        z1 + (bool4 ? size2 : -size2))
                .color(r, g, b, 0.3F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(DarkSpark entity) {
        // Vanilla lightning references blocks atlas, so let's do the same:
        return TextureAtlas.LOCATION_BLOCKS;
    }
}