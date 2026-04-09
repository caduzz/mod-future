package net.caduzz.futuremod.client;

import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caduzz.futuremod.block.ChessBlock;
import net.caduzz.futuremod.block.entity.ChessBlockEntity;
import net.caduzz.futuremod.block.entity.ChessBlockEntity.Piece;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class ChessBlockRenderer implements BlockEntityRenderer<ChessBlockEntity> {
    private static final ResourceLocation WHITE_TEX = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final ResourceLocation PAWN_WHITE_TEX = ResourceLocation.fromNamespaceAndPath("futuremod", "textures/block/pawn_white.png");
    private static final ResourceLocation PAWN_BLACK_TEX = ResourceLocation.fromNamespaceAndPath("futuremod", "textures/block/pawn_black.png");
    private static final ResourceLocation TOWER_WHITE_TEX = ResourceLocation.fromNamespaceAndPath("futuremod", "textures/block/tower_white.png");
    private static final ResourceLocation TOWER_BLACK_TEX = ResourceLocation.fromNamespaceAndPath("futuremod", "textures/block/tower_black.png");
    private static final ResourceLocation HORSE_WHITE_TEX = ResourceLocation.fromNamespaceAndPath("futuremod", "textures/block/horse_white.png");
    private static final ResourceLocation HORSE_BLACK_TEX = ResourceLocation.fromNamespaceAndPath("futuremod", "textures/block/horse_black.png");
    private static final ResourceLocation BISHOP_WHITE_TEX = ResourceLocation.fromNamespaceAndPath("futuremod", "textures/block/bishop_white.png");
    private static final ResourceLocation BISHOP_BLACK_TEX = ResourceLocation.fromNamespaceAndPath("futuremod", "textures/block/bishop_black.png");
    private static final ResourceLocation QUEEN_WHITE_TEX = ResourceLocation.fromNamespaceAndPath("futuremod", "textures/block/queen_white.png");
    private static final ResourceLocation QUEEN_BLACK_TEX = ResourceLocation.fromNamespaceAndPath("futuremod", "textures/block/queen_black.png");
    private static final ResourceLocation KING_WHITE_TEX = ResourceLocation.fromNamespaceAndPath("futuremod", "textures/block/king_white.png");
    private static final ResourceLocation KING_BLACK_TEX = ResourceLocation.fromNamespaceAndPath("futuremod", "textures/block/king_black.png");
    /**
     * Modelo do pawn tem base de ~6 pixels no JSON; reduzimos para ~0.9 pixel no mundo.
     */
    private static final float PAWN_MODEL_SCALE = 0.15f;
    private static final float TOWER_MODEL_SCALE = 0.15f;
    private static final float HORSE_MODEL_SCALE = 0.15f;
    private static final float BISHOP_MODEL_SCALE = 0.15f;
    private static final float QUEEN_MODEL_SCALE = 0.15f;
    private static final float KING_MODEL_SCALE = 0.15f;
    private static final float CELL = 1f / 8f;
    private static final float OVERLAY_Y = ChessBlock.BOARD_SURFACE_Y + 0.004f;
    private static final float CELL_INSET = CELL * 0.12f;

    public ChessBlockRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(ChessBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        ChessBlock.applyBoardRenderRotation(poseStack, be.getBlockState().getValue(ChessBlock.FACING));

        VertexConsumer translucent = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
        for (int i = 0; i < be.getValidMoveCount(); i++) {
            int packed = be.getValidMovePacked(i);
            drawValidCellOverlay(poseStack.last(), translucent, packed / 8, packed % 8, packedLight);
        }

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = be.getPiece(r, c);
                if (p == Piece.EMPTY) {
                    continue;
                }
                float cx = (c + 0.5f) * CELL;
                float cz = (r + 0.5f) * CELL;
                boolean selected = be.hasSelection() && r == be.getSelectedRow() && c == be.getSelectedCol();
                float scale = selected ? 1.15f : 1f;

                if (p == Piece.WHITE_PAWN || p == Piece.BLACK_PAWN) {
                    VertexConsumer pawnConsumer = buffer
                            .getBuffer(RenderType.entityCutoutNoCull(p == Piece.WHITE_PAWN ? PAWN_WHITE_TEX : PAWN_BLACK_TEX));
                    renderPawnModel(poseStack.last(), pawnConsumer, cx, cz, scale, packedLight);
                    continue;
                }
                if (p == Piece.WHITE_ROOK || p == Piece.BLACK_ROOK) {
                    VertexConsumer towerConsumer = buffer
                            .getBuffer(RenderType.entityCutoutNoCull(p == Piece.WHITE_ROOK ? TOWER_WHITE_TEX : TOWER_BLACK_TEX));
                    renderTowerModel(poseStack.last(), towerConsumer, cx, cz, scale, packedLight);
                    continue;
                }
                if (p == Piece.WHITE_KNIGHT || p == Piece.BLACK_KNIGHT) {
                    VertexConsumer horseConsumer = buffer
                            .getBuffer(RenderType.entityCutoutNoCull(p == Piece.WHITE_KNIGHT ? HORSE_WHITE_TEX : HORSE_BLACK_TEX));
                    renderHorseModel(poseStack.last(), horseConsumer, cx, cz, scale, packedLight);
                    continue;
                }
                if (p == Piece.WHITE_BISHOP || p == Piece.BLACK_BISHOP) {
                    VertexConsumer bishopConsumer = buffer
                            .getBuffer(RenderType.entityCutoutNoCull(p == Piece.WHITE_BISHOP ? BISHOP_WHITE_TEX : BISHOP_BLACK_TEX));
                    renderBishopModel(poseStack.last(), bishopConsumer, cx, cz, scale, packedLight);
                    continue;
                }
                if (p == Piece.WHITE_QUEEN || p == Piece.BLACK_QUEEN) {
                    VertexConsumer queenConsumer = buffer
                            .getBuffer(RenderType.entityCutoutNoCull(p == Piece.WHITE_QUEEN ? QUEEN_WHITE_TEX : QUEEN_BLACK_TEX));
                    renderQueenModel(poseStack.last(), queenConsumer, cx, cz, scale, packedLight);
                    continue;
                }
                if (p == Piece.WHITE_KING || p == Piece.BLACK_KING) {
                    VertexConsumer kingConsumer = buffer
                            .getBuffer(RenderType.entityCutoutNoCull(p == Piece.WHITE_KING ? KING_WHITE_TEX : KING_BLACK_TEX));
                    renderKingModel(poseStack.last(), kingConsumer, cx, cz, scale, packedLight);
                    continue;
                }

                VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(WHITE_TEX));
                float h = pieceHeight(p);
                float cy = ChessBlock.BOARD_SURFACE_Y + h + 0.002f;
                float halfW = CELL * 0.28f * scale;
                int[] rgb = pieceRgb(p);
                drawBox(poseStack.last(), consumer, cx, cy, cz, halfW, h * scale, rgb[0], rgb[1], rgb[2], 255, packedLight);
            }
        }
        poseStack.popPose();
    }

    private static float pieceHeight(Piece p) {
        return switch (p) {
            case WHITE_PAWN, BLACK_PAWN -> CELL * 0.13f;
            case WHITE_ROOK, BLACK_ROOK, WHITE_KNIGHT, BLACK_KNIGHT, WHITE_BISHOP, BLACK_BISHOP -> CELL * 0.17f;
            case WHITE_QUEEN, BLACK_QUEEN -> CELL * 0.20f;
            case WHITE_KING, BLACK_KING -> CELL * 0.22f;
            default -> CELL * 0.14f;
        };
    }

    private static int[] pieceRgb(Piece p) {
        return switch (p) {
            case WHITE_PAWN, WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING -> new int[] { 236, 236, 242 };
            default -> new int[] { 36, 36, 44 };
        };
    }

    private static void renderPawnModel(PoseStack.Pose pose, VertexConsumer consumer, float cx, float cz, float scale, int light) {
        renderTexturedPieceModel(pose, consumer, cx, cz, scale * PAWN_MODEL_SCALE, ChessBlockbenchModelLoader.pawnModel(), light);
    }

    private static void renderTowerModel(PoseStack.Pose pose, VertexConsumer consumer, float cx, float cz, float scale, int light) {
        renderTexturedPieceModel(pose, consumer, cx, cz, scale * TOWER_MODEL_SCALE, ChessBlockbenchModelLoader.towerModel(), light);
    }

    private static void renderHorseModel(PoseStack.Pose pose, VertexConsumer consumer, float cx, float cz, float scale, int light) {
        renderTexturedPieceModel(pose, consumer, cx, cz, scale * HORSE_MODEL_SCALE, ChessBlockbenchModelLoader.horseModel(), light);
    }

    private static void renderBishopModel(PoseStack.Pose pose, VertexConsumer consumer, float cx, float cz, float scale, int light) {
        renderTexturedPieceModel(pose, consumer, cx, cz, scale * BISHOP_MODEL_SCALE, ChessBlockbenchModelLoader.bishopModel(), light);
    }

    private static void renderQueenModel(PoseStack.Pose pose, VertexConsumer consumer, float cx, float cz, float scale, int light) {
        renderTexturedPieceModel(pose, consumer, cx, cz, scale * QUEEN_MODEL_SCALE, ChessBlockbenchModelLoader.queenModel(), light);
    }

    private static void renderKingModel(PoseStack.Pose pose, VertexConsumer consumer, float cx, float cz, float scale, int light) {
        renderTexturedPieceModel(pose, consumer, cx, cz, scale * KING_MODEL_SCALE, ChessBlockbenchModelLoader.kingModel(), light);
    }

    private static void renderTexturedPieceModel(PoseStack.Pose pose, VertexConsumer consumer, float cx, float cz, float scaled,
            ChessPieceModelData model, int light) {
        float baseY = ChessBlock.BOARD_SURFACE_Y + 0.002f;
        for (ChessPieceModelData.TexturedElement e : model.elements()) {
            drawTexturedElement(pose, consumer, cx, baseY, cz, scaled, e, light);
        }
    }

    private static void drawTexturedElement(PoseStack.Pose pose, VertexConsumer consumer, float cx, float baseY, float cz, float scaled,
            ChessPieceModelData.TexturedElement e, int light) {
        float fx = ((e.fromX - 8f) / 16f) * scaled;
        float tx = ((e.toX - 8f) / 16f) * scaled;
        float fz = ((e.fromZ - 8f) / 16f) * scaled;
        float tz = ((e.toZ - 8f) / 16f) * scaled;
        float minX = cx + Math.min(fx, tx);
        float maxX = cx + Math.max(fx, tx);
        float minY = baseY + (e.fromY / 16f) * scaled;
        float maxY = baseY + (e.toY / 16f) * scaled;
        float minZ = cz + Math.min(fz, tz);
        float maxZ = cz + Math.max(fz, tz);
        Matrix4f mat = pose.pose();
        ChessPieceModelData.FaceUv f;
        if ((f = e.down) != null) {
            quadTex(consumer, mat, pose, light, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, 0, -1, 0, f.u0(), f.v1(),
                    f.u1(), f.v1(), f.u1(), f.v0(), f.u0(), f.v0());
        }
        if ((f = e.up) != null) {
            quadTex(consumer, mat, pose, light, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, 0, 1, 0, f.u0(), f.v1(),
                    f.u0(), f.v0(), f.u1(), f.v0(), f.u1(), f.v1());
        }
        if ((f = e.north) != null) {
            quadTex(consumer, mat, pose, light, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, 0, 0, -1, f.u0(), f.v1(),
                    f.u1(), f.v1(), f.u1(), f.v0(), f.u0(), f.v0());
        }
        if ((f = e.south) != null) {
            quadTex(consumer, mat, pose, light, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, 0, 0, 1, f.u0(), f.v1(),
                    f.u0(), f.v0(), f.u1(), f.v0(), f.u1(), f.v1());
        }
        if ((f = e.west) != null) {
            quadTex(consumer, mat, pose, light, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ, -1, 0, 0, f.u0(), f.v1(),
                    f.u0(), f.v0(), f.u1(), f.v0(), f.u1(), f.v1());
        }
        if ((f = e.east) != null) {
            quadTex(consumer, mat, pose, light, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, 1, 0, 0, f.u0(), f.v1(),
                    f.u0(), f.v0(), f.u1(), f.v0(), f.u1(), f.v1());
        }
    }

    private static void quadTex(VertexConsumer consumer, Matrix4f mat, PoseStack.Pose pose, int light, float x0, float y0, float z0, float x1,
            float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float nx, float ny, float nz, float u0, float v0,
            float u1, float v1, float u2, float v2, float u3, float v3) {
        int cr = 255;
        int cg = 255;
        int cb = 255;
        int ca = 255;
        vertexTex(consumer, mat, pose, light, cr, cg, cb, ca, x0, y0, z0, nx, ny, nz, u0, v0);
        vertexTex(consumer, mat, pose, light, cr, cg, cb, ca, x1, y1, z1, nx, ny, nz, u1, v1);
        vertexTex(consumer, mat, pose, light, cr, cg, cb, ca, x2, y2, z2, nx, ny, nz, u2, v2);
        vertexTex(consumer, mat, pose, light, cr, cg, cb, ca, x3, y3, z3, nx, ny, nz, u3, v3);
    }

    private static void drawValidCellOverlay(PoseStack.Pose pose, VertexConsumer consumer, int row, int col, int light) {
        float minX = col * CELL + CELL_INSET;
        float maxX = (col + 1) * CELL - CELL_INSET;
        float minZ = row * CELL + CELL_INSET;
        float maxZ = (row + 1) * CELL - CELL_INSET;
        Matrix4f mat = pose.pose();
        float y = OVERLAY_Y;
        vertexT(consumer, mat, pose, light, 90, 170, 255, 90, minX, y, minZ, 0, 1, 0);
        vertexT(consumer, mat, pose, light, 90, 170, 255, 90, maxX, y, minZ, 0, 1, 0);
        vertexT(consumer, mat, pose, light, 90, 170, 255, 90, maxX, y, maxZ, 0, 1, 0);
        vertexT(consumer, mat, pose, light, 90, 170, 255, 90, minX, y, maxZ, 0, 1, 0);
    }

    private static void drawBox(PoseStack.Pose pose, VertexConsumer consumer, float cx, float cy, float cz, float halfW, float halfH, int cr,
            int cg, int cb, int ca, int light) {
        float minX = cx - halfW;
        float maxX = cx + halfW;
        float minY = cy - halfH;
        float maxY = cy + halfH;
        float minZ = cz - halfW;
        float maxZ = cz + halfW;
        drawBoxFull(pose, consumer, minX, minY, minZ, maxX, maxY, maxZ, cr, cg, cb, ca, light);
    }

    private static void drawBoxFull(PoseStack.Pose pose, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY,
            float maxZ, int cr, int cg, int cb, int ca, int light) {
        Matrix4f mat = pose.pose();
        quad(consumer, mat, pose, light, cr, cg, cb, ca, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, 0, -1, 0);
        quad(consumer, mat, pose, light, cr, cg, cb, ca, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, 0, 1, 0);
        quad(consumer, mat, pose, light, cr, cg, cb, ca, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, 0, 0, -1);
        quad(consumer, mat, pose, light, cr, cg, cb, ca, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, 1, 0, 0);
        quad(consumer, mat, pose, light, cr, cg, cb, ca, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ, -1, 0, 0);
        quad(consumer, mat, pose, light, cr, cg, cb, ca, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, minX, minY, maxZ, 0, 0, 1);
    }

    private static void quad(VertexConsumer consumer, Matrix4f mat, PoseStack.Pose pose, int light, int cr, int cg, int cb, int ca, float x0,
            float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float nx, float ny,
            float nz) {
        vertexSolid(consumer, mat, pose, light, cr, cg, cb, ca, x0, y0, z0, nx, ny, nz);
        vertexSolid(consumer, mat, pose, light, cr, cg, cb, ca, x1, y1, z1, nx, ny, nz);
        vertexSolid(consumer, mat, pose, light, cr, cg, cb, ca, x2, y2, z2, nx, ny, nz);
        vertexSolid(consumer, mat, pose, light, cr, cg, cb, ca, x3, y3, z3, nx, ny, nz);
    }

    private static void vertexSolid(VertexConsumer consumer, Matrix4f mat, PoseStack.Pose pose, int light, int cr, int cg, int cb, int ca, float px,
            float py, float pz, float nx, float ny, float nz) {
        consumer.addVertex(mat, px, py, pz).setColor(cr, cg, cb, ca).setUv(0.5f, 0.5f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private static void vertexTex(VertexConsumer consumer, Matrix4f mat, PoseStack.Pose pose, int light, int cr, int cg, int cb, int ca, float px,
            float py, float pz, float nx, float ny, float nz, float u, float v) {
        consumer.addVertex(mat, px, py, pz).setColor(cr, cg, cb, ca).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private static void vertexT(VertexConsumer consumer, Matrix4f mat, PoseStack.Pose pose, int light, int cr, int cg, int cb, int ca, float px,
            float py, float pz, float nx, float ny, float nz) {
        vertexSolid(consumer, mat, pose, light, cr, cg, cb, ca, px, py, pz, nx, ny, nz);
    }
}
