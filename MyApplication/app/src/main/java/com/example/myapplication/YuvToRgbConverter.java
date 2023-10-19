package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

import java.nio.ByteBuffer;

public class YuvToRgbConverter {
    private final RenderScript rs;
    private final ScriptIntrinsicYuvToRGB scriptYuvToRgb;

    private int pixelCount = -1;
    private byte[] yuvBuffer;
    private Allocation inputAllocation;
    private Allocation outputAllocation;

    public YuvToRgbConverter(Context context) {
        rs = RenderScript.create(context);
        scriptYuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
    }

    public synchronized void yuvToRgb(Image image, Bitmap output) {
        if (!yuvBufferInitialized()) {
            pixelCount = image.getCropRect().width() * image.getCropRect().height();
            int pixelSizeBits = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888);
            yuvBuffer = new byte[pixelCount * pixelSizeBits / 8];
        }

        imageToByteArray(image, yuvBuffer);

        if (!allocationsInitialized(output)) {
            Type elemType = new Type.Builder(rs, Element.YUV(rs)).setYuvFormat(ImageFormat.NV21).create();
            inputAllocation = Allocation.createSized(rs, elemType.getElement(), yuvBuffer.length);
            outputAllocation = Allocation.createFromBitmap(rs, output);
        }

        inputAllocation.copyFrom(yuvBuffer);
        scriptYuvToRgb.setInput(inputAllocation);
        scriptYuvToRgb.forEach(outputAllocation);
        outputAllocation.copyTo(output);
    }

    private boolean yuvBufferInitialized() {
        return yuvBuffer != null;
    }

    private boolean allocationsInitialized(Bitmap output) {
        return inputAllocation != null && outputAllocation != null && outputAllocation.getType().getX() == output.getWidth() && outputAllocation.getType().getY() == output.getHeight();
    }

    private void imageToByteArray(Image image, byte[] outputBuffer) {
        assert image.getFormat() == ImageFormat.YUV_420_888;

        Rect imageCrop = image.getCropRect();
        Image.Plane[] imagePlanes = image.getPlanes();

        for (int planeIndex = 0; planeIndex < imagePlanes.length; planeIndex++) {
            int outputStride;
            int outputOffset;

            switch (planeIndex) {
                case 0:
                    outputStride = 1;
                    outputOffset = 0;
                    break;
                case 1:
                    outputStride = 2;
                    outputOffset = pixelCount + 1;
                    break;
                case 2:
                    outputStride = 2;
                    outputOffset = pixelCount;
                    break;
                default:
                    return;
            }

            Image.Plane plane = imagePlanes[planeIndex];
            ByteBuffer planeBuffer = plane.getBuffer();
            int rowStride = plane.getRowStride();
            int pixelStride = plane.getPixelStride();
            Rect planeCrop = (planeIndex == 0) ? imageCrop : new Rect(imageCrop.left / 2, imageCrop.top / 2, imageCrop.right / 2, imageCrop.bottom / 2);
            int planeWidth = planeCrop.width();
            int planeHeight = planeCrop.height();
            byte[] rowBuffer = new byte[rowStride];

            for (int row = 0; row < planeHeight; row++) {
                planeBuffer.position((row + planeCrop.top) * rowStride + planeCrop.left * pixelStride);

                if (pixelStride == 1 && outputStride == 1) {
                    planeBuffer.get(outputBuffer, outputOffset, planeWidth);
                    outputOffset += planeWidth;
                } else {
                    planeBuffer.get(rowBuffer, 0, planeWidth);
                    for (int col = 0; col < planeWidth; col++) {
                        outputBuffer[outputOffset] = rowBuffer[col * pixelStride];
                        outputOffset += outputStride;
                    }
                }
            }
        }
    }
}
