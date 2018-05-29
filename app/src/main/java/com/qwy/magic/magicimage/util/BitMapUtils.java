package com.qwy.magic.magicimage.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import com.qwy.magic.magicimage.MainActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by qwy on 2016/10/19.
 * bitmap编解码工具类
 */
public class BitMapUtils {


    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            return "";
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                return "";
            }
        }
        return result;
    }


    /**
     * 从SD卡上获取图片。如果不存在则返回null</br>
     * @return 代表图片的Bitmap对象
     */
    public static Bitmap getBitmapFromSDCard(String url) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(url));
            if (inputStream != null && inputStream.available() > 0) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 分享单张图片
    public static void shareSingleImage(Context context, Bitmap copy) {
        if(copy != null){
            File appDir = new File(Environment.getExternalStorageDirectory(), "demo");
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            String fileName = System.currentTimeMillis() + ".jpg";
            File file = new File(appDir, fileName);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                copy.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String path = file.getAbsolutePath();

            // 由文件得到uri
            Uri imageUri = Uri.fromFile(new File(path));
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/*");
            context.startActivity(Intent.createChooser(shareIntent, "分享到"));
        }else {
            Toast.makeText(context, "请先从图库选择一张图片", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 保存图片到本地图库
     *
     * @param context       上下文
     * @param bmp           要保存的bitmap
     */
    public static void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "demo");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String path = file.getAbsolutePath();

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Toast.makeText(context, "图片已保存至" + path, Toast.LENGTH_SHORT).show();
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
    }

    /**
     * 选择图片后压缩展示
     */
    public static Bitmap onSelected(String local_path) {
        Bitmap bitmap = BitmapFactory.decodeFile(local_path);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
        int size = bitmap.getWidth() * bitmap.getHeight();
        float zoom = (float)Math.sqrt(size * 1024 / (float)out.toByteArray().length);

        Matrix matrix = new Matrix();
        matrix.setScale(zoom, zoom);

        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        out.reset();
        result.compress(Bitmap.CompressFormat.JPEG, 85, out);
        while(out.toByteArray().length > size * 1024){
            System.out.println(out.toByteArray().length);
            matrix.setScale(0.9f, 0.9f);
            result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
            out.reset();
            result.compress(Bitmap.CompressFormat.JPEG, 85, out);
        }
        bitmap = result;
        return bitmap;
    }

    /**
     * 获取压缩后的图片
     *
     * @param srcPath       要压缩的图片路径
     * @return              压缩后的bitmap
     */
    public static Bitmap getImage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);// 此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
    }

    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 处理指定的图片
     */
    public static Bitmap handlePicture(Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap copy = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] before = new int[width * height];
        int[] after = new int[width * height];
        bitmap.getPixels(before, 0, width, 0, 0, width, height);
        int pixelsA, pixelsR, pixelsG, pixelsB;
        for(int i = 0;i < width * height;i++){
            int color = before[i];
            // ∂获取RGB分量
            pixelsA = Color.alpha(color);
            pixelsR = Color.red(color);
            pixelsG = Color.green(color);
            pixelsB = Color.blue(color);

            if(pixelsA >= 1) {
                // 转换
                pixelsR = (255 - pixelsR);
                pixelsG = (255 - pixelsG);
                pixelsB = (255 - pixelsB);
                // 均小于等于255大于等于0
                if (pixelsR > 255) {
                    pixelsR = 255;
                } else if (pixelsR < 0) {
                    pixelsR = 0;
                }
                if (pixelsG > 255) {
                    pixelsG = 255;
                } else if (pixelsG < 0) {
                    pixelsG = 0;
                }
                if (pixelsB > 255) {
                    pixelsB = 255;
                } else if (pixelsB < 0) {
                    pixelsB = 0;
                }
            }
            // 根据新的RGB生成新像素
            after[i] = Color.argb(pixelsA, pixelsR, pixelsG, pixelsB);
        }
        copy.setPixels(after, 0, width, 0, 0, width, height);
        return copy;
    }


    /**
     * 处理指定的图片为黑白照片
     */
    public static Bitmap colored(Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap copy = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] before = new int[width * height];
        int[] after = new int[width * height];
        bitmap.getPixels(before, 0, width, 0, 0, width, height);
        int pixelsA, pixelsR, pixelsG, pixelsB;
        for(int i = 0;i < width * height;i++){
            int color = before[i];
            // 获取RGB分量
            pixelsA = Color.alpha(color);
            pixelsR = Color.red(color);
            pixelsG = Color.green(color);
            pixelsB = Color.blue(color);

            if(pixelsA >= 1) {
                pixelsR = pixelsG = pixelsB = (pixelsR + pixelsG + pixelsB) / 3;
            }

            // 根据新的RGB生成新像素
            after[i] = Color.argb(pixelsA, pixelsR, pixelsG, pixelsB);
        }
        copy.setPixels(after, 0, width, 0, 0, width, height);
        return copy;
    }

    /**
     * 处理指定的图片为半透明照片
     */
    public static Bitmap halfTrans(Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap copy = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] before = new int[width * height];
        int[] after = new int[width * height];
        bitmap.getPixels(before, 0, width, 0, 0, width, height);
        int pixelsA, pixelsR, pixelsG, pixelsB;
        for(int i = 0;i < width * height;i++){
            int color = before[i];
            // 获取RGB分量
            pixelsA = Color.alpha(color);
            pixelsR = Color.red(color);
            pixelsG = Color.green(color);
            pixelsB = Color.blue(color);

            // 根据新的RGB生成新像素
            after[i] = Color.argb(pixelsA / 2, pixelsR, pixelsG, pixelsB);
        }
        copy.setPixels(after, 0, width, 0, 0, width, height);
        return copy;
    }

}
