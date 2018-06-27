package nl.tudelft.cs4160.trustchain_android.funds.qr;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.common.BitMatrix;

public class QRGenerator {

    public static Bitmap GenerateQRCode(int size, BitMatrix matrix) {
        final Bitmap image = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < size; i++) {//width
            for (int j = 0; j < size; j++) {//height
                image.setPixel(i, j, matrix.get(i, j) ? Color.BLACK : Color.WHITE);
            }
        }
        return image;
    }
}
