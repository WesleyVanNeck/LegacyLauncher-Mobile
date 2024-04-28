package net.kdt.pojavlaunch.utils;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Utilities for working with matrices and rectangles.
 */
public class MatrixUtils {

    /**
     * Transforms the coordinates of the input rectangle using the supplied matrix, and writes the result
     * into the output rectangle.
     *
     * @param inRect the input rectangle for this operation
     * @param outRect the output rectangle for this operation
     * @param transformMatrix the matrix for transforming the rectangle
     */
    public static void transformRect(Rect inRect, Rect outRect, Matrix transformMatrix) {
        transformRectImpl(inRect, outRect, transformMatrix, FlipType.NONE);
    }

    /**
     * Transforms the coordinates of the input rectangle using the supplied matrix, and writes the result
     * into the output rectangle.
     *
     * @param inRect the input rectangle for this operation
     * @param outRect the output rectangle for this operation
     * @param transformMatrix the matrix for transforming the rectangle
     * @param flipType the type of flipping to apply to the rectangle
     */
    public static void transformRect(Rect inRect, Rect outRect, Matrix transformMatrix, FlipType flipType) {
        transformRectImpl(inRect, outRect, transformMatrix, flipType);
    }

    /**
     * Transforms the coordinates of the input rectangle using the supplied matrix, and writes the result
     * into the output rectangle.
     *
     * @param inRect the input rectangle for this operation
     * @param outRect the output rectangle for this operation
     * @param transformMatrix the matrix for transforming the rectangle
     */
    public static void transformRect(RectF inRect, RectF outRect, Matrix transformMatrix) {
        transformRectImpl(inRect, outRect, transformMatrix, FlipType.NONE);
    }

    /**
     * Transforms the coordinates of the input rectangle using the supplied matrix, and writes the result
     * into the output rectangle.
     *
     * @param inRect the input rectangle for this operation
     * @param outRect the output rectangle for this operation
     * @param transformMatrix the matrix for transforming the rectangle
     * @param flipType the type of flipping to apply to the rectangle
     */
    public static void transformRect(RectF inRect, RectF outRect, Matrix transformMatrix, FlipType flipType) {
        transformRectImpl(inRect, outRect, transformMatrix, flipType);
    }

    /**
     * Transforms the coordinates of the input rectangle using the supplied matrix, and writes the result
     * into the output rectangle.
     *
     * @param inRect the input rectangle for this operation
     * @param outRect the output rectangle for this operation
     * @param transformMatrix the matrix for transforming the rectangle
     * @param flipType the type of flipping to apply to the rectangle
     */
    private static void transformRectImpl(Rect inRect, Rect outRect, Matrix transformMatrix, FlipType flipType) {
        if (inRect == null || outRect == null || transformMatrix == null) {
            throw new IllegalArgumentException("Input parameters cannot be null");
        }

        float[] inOutDecodeRect = createInOutDecodeRect(transformMatrix);
        if (inOutDecodeRect == null) {
            return;
        }

        writeInputRect(inOutDecodeRect, inRect);
        flipPoints(inOutDecodeRect, flipType);
        transformPoints(inOutDecodeRect, transformMatrix);
        readOutputRect(inOutDecodeRect, outRect);
    }

    /**
     * Transforms the coordinates of the input rectangle using the supplied matrix, and writes the result
     * into the output rectangle.
     *
     * @param inRect the input rectangle for this operation
     * @param outRect the output rectangle for this operation
     * @param transformMatrix the matrix for transforming the rectangle
     * @param flipType the type of flipping to apply to the rectangle
     */
    private static void transformRectImpl(RectF inRect, RectF outRect, Matrix transformMatrix, FlipType flipType) {
        if (inRect == null || outRect == null || transformMatrix == null) {
            throw new IllegalArgumentException("Input parameters cannot be null");
        }

        float[] inOutDecodeRect = createInOutDecodeRect(transformMatrix);
        if (inOutDecodeRect == null) {
            return;
        }

        writeInputRect(inOutDecodeRect, inRect);
        flipPoints(inOutDecodeRect, flipType);
        transformPoints(inOutDecodeRect, transformMatrix);
        readOutputRect(inOutDecodeRect, outRect);
    }

    /**
     * Transforms the coordinates of the input rectangle using the supplied matrix, and writes the result
     * into the output rectangle.
     *
     * @param inRect the input rectangle for this operation
     * @param outRect the output rectangle for this operation
     * @param transformMatrix the matrix for transforming the rectangle
     * @param flipType the type of flipping to apply to the rectangle
     */
    private static void transformRectImpl(Rect inRect, RectF outRect, Matrix transformMatrix, FlipType flipType) {
        if (inRect == null || outRect == null || transformMatrix == null) {
            throw new IllegalArgumentException("Input parameters cannot be null");
        }

        float[] inOutDecodeRect = createInOutDecodeRect(transformMatrix);
        if (inOutDecodeRect == null) {
            return;
        }

        writeInputRect(inOutDecodeRect, inRect);
        flipPoints(inOutDecodeRect, flipType);
        transformPoints(inOutDecodeRect, transformMatrix);
        readOutputRect(inOutDecodeRect, outRect);
    }

    /**
     * Transforms the coordinates of the input rectangle using the supplied matrix, and writes the result
     * into the output rectangle.
     *
     * @param inRect the input rectangle for this operation
     * @param outRect the output rectangle for this operation
     * @param transformMatrix the matrix for transforming the rectangle
     * @param flipType the type of flipping to apply to the rectangle
     */
    private static void transformRectImpl(RectF inRect, Rect outRect, Matrix transformMatrix, FlipType flipType) {
        if (inRect == null || outRect == null || transformMatrix == null) {
            throw new IllegalArgumentException("Input parameters cannot be null");
        }

        float[] inOutDecodeRect = createInOutDecodeRect(transformMatrix);
        if (inOutDecodeRect == null) {
            return;
        }

        writeInputRect(inOutDecodeRect, inRect);
        flipPoints(inOutDecodeRect, flipType);
        transformPoints(inOutDecodeRect, transformMatrix);
        readOutputRect(inOutDecodeRect, outRect);
    }

    /**
     * Transforms the coordinates of the input rectangle using the supplied matrix, and writes the result
     * into the output rectangle.
     *
     * @param inRect the input rectangle for this operation
     * @param outRect the output rectangle for this operation
     * @param transformMatrix the matrix for transforming the rectangle
     */
    private static void transformRectImpl(Rect inRect, Rect outRect, Matrix transformMatrix) {
        transformRectImpl(inRect, outRect, transformMatrix, FlipType.NONE);
    }

    /**
     * Transforms the coordinates of the input rectangle using the supplied matrix, and writes the result
     * into the output rectangle.
     *
     * @param inRect the input rectangle for this operation
     * @param outRect the output rectangle for this operation
     * @param transformMatrix the matrix for transforming the rectangle
     */
    private static void transformRectImpl(RectF inRect, RectF outRect, Matrix transformMatrix) {
        transformRectImpl(inRect, outRect, transformMatrix, FlipType.NONE);
    }

    /**
     * Flips the points in the input array based on the flip type.
     *
     * @param inOutDecodeRect the array of points to flip
     * @param flipType the type of flipping to apply
     */
    private static void flipPoints(float[] inOutDecodeRect, FlipType flipType) {
        if (flipType == FlipType.HORIZONTAL) {
            float temp = inOutDecodeRect[2];
            inOutDecodeRect[2] = inOutDecodeRect[0];
            inOutDecodeRect[0] = temp;
        } else if (flipType == FlipType.VERTICAL) {
            float temp = inOutDecodeRect[1];
            inOutDecodeRect[1] = inOutDecodeRect[3];
            inOutDecodeRect[3] = temp;
        }
    }

    /**
     * Creates an array of 8 floats to be used as input and output for transforming rectangles.
     *
     * @param transformMatrix the matrix for transforming the rectangle
     * @return the array of 8 floats
     */
    private static float[] createInOutDecodeRect(Matrix transformMatrix) {
        if (transformMatrix.isIdentity()) {
            return null;
        }

        float[] inOutDecodeRect = new float[8];
        inOutDecodeRect[0] = 0;
        inOutDecodeRect[1] = 0;
        inOutDecodeRect[2] = 1;
        inOutDecodeRect[3] = 1;
        return inOutDecodeRect;
    }

    /**
     * Writes the input rectangle into the input and output array.
     *
     * @param inOutDecodeRect the array of 8 floats
     * @param inRect the input rectangle
     */
    private static void writeInputRect(float[] inOutDecodeRect, Rect inRect) {
        inOutDecodeRect[0] = inRect.left;
        inOutDecodeRect[1] = inRect.top;
        inOutDecodeRect[2] = inRect.right;
        inOutDecodeRect[3] = inRect.bottom;
    }

    /**
     * Writes the input rectangle into the input and output array.
     *
     * @param inOutDecodeRect the array of 8 floats
     * @param inRect the input rectangle
     */
    private static void writeInputRect(float[] inOutDecodeRect, RectF inRect) {
        inOutDecodeRect[0] = inRect.left;
        inOutDecodeRect[1] = inRect.top;
        inOutDecodeRect[2] = inRect.right;
        inOutDecodeRect[3] = inRect.bottom;
    }

    /**
     * Reads the output rectangle from the input and output array.
     *
     * @param inOutDecodeRect the array of 8 floats
     * @param outRect the output rectangle
     */
    private static void readOutputRect(float[] inOutDecodeRect, Rect outRect) {
        outRect.set((int) inOutDecodeRect[0], (int) inOutDecodeRect[1], (int) inOutDecodeRect[2], (int) inOutDecodeRect[3]);
    }

    /**
     * Reads the output rectangle from the input and output array.
     *
     * @param inOutDecodeRect the array of 8 floats
     * @param outRect the output rectangle
     */
    private static void readOutputRect(float[] inOutDecodeRect, RectF outRect) {
        outRect.set(inOutDecodeRect[0], inOutDecodeRect[1], inOutDecodeRect[2], inOutDecodeRect[3]);
    }

    /**
     * Transforms the points in the input array using the supplied matrix.
     *
     * @param inOutDecodeRect the array of points to transform
     * @param transformMatrix the matrix for transforming the points
     */
    private static void transformPoints(float[] inOutDecodeRect, Matrix transformMatrix) {
        transformMatrix.mapPoints(inOutDecodeRect, 0, 2);
    }

    /**
     * Inverts the source matrix, and writes the result into the destination matrix.
     *
     * @param source the source matrix
     * @param destination the destination matrix
     * @throws IllegalArgumentException when the matrix is not invertible
     */
    public static void inverse(Matrix source, Matrix destination) throws IllegalArgumentException {
        if (!source.invert(destination)) {
            float[] matrix = new float[9];
            source.getValues(matrix);
            inverseMatrix(matrix);
            destination.setValues(matrix);
        }
    }

    /**
     * Inverts an affine matrix.
     *
     * @param matrix the matrix to invert
     */
    private static void inverseMatrix(float[] matrix) {
        float determinant = matrix[0] * (matrix[4] * matrix[8] - matrix[5] * matrix[7])
                - matrix[1] * (matrix[3] * matrix[8] - matrix[5] * matrix[6])
                + matrix[2] * (matrix[3] * matrix[7] - matrix[4] * matrix[6]);

        if (determinant == 0) {
            throw new IllegalArgumentException("Matrix is not invertible");
        }

        float invDet = 1 / determinant;

        float temp0 = (matrix[4] * matrix[8] - matrix[5] * matrix[7]);
        float temp1 = (matrix[2] * matrix[7] - matrix[1] * matrix[8]);
        float temp2 = (matrix[1] * matrix
