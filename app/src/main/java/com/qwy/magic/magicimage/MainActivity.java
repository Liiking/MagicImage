package com.qwy.magic.magicimage;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.qwy.magic.magicimage.entity.IdentifyResult;
import com.qwy.magic.magicimage.util.BitMapUtils;
import com.qwy.magic.magicimage.util.TecentHttpUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageView, iv_after;
    private TextView textView;
    private TextView tv_color;
    private TextView tv_trans;
    private TextView tv_identify;
    private final static int REQUEST_IMAGE = 100;
    private String p = null;
    private Bitmap bitmap = null, copy = null;
    private int[] before;
    private int[] after;
    private int width, height;
    private int pixelsR, pixelsG, pixelsB, pixelsA;
    private AlertDialog dialog;// 提示分享货保存图片弹窗
    private AlertDialog dialogInfo;// 识别身份证信息弹窗

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        dialog = builder.setTitle("提示")
                .setMessage("分享或保存图片？")
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // save picture
                        saveImageToGallery(MainActivity.this, copy);
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("分享", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shareSingleImage();
                    }
                })
                .setNegativeButton("不了", null)
                .create();

        imageView = (ImageView) findViewById(R.id.image);
        iv_after = (ImageView) findViewById(R.id.iv_after);
        iv_after.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialog.show();
                return false;
            }
        });
        imageView.setOnClickListener(this);
        tv_color = (TextView) findViewById(R.id.tv_color);
        tv_color.setOnClickListener(this);
        tv_trans = (TextView) findViewById(R.id.tv_trans);
        tv_trans.setOnClickListener(this);
        tv_identify = (TextView) findViewById(R.id.tv_identify);
        tv_identify.setOnClickListener(this);
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
        }
        textView = (TextView) findViewById(R.id.tv_change);
        textView.setOnClickListener(this);

    }

    // 分享单张图片
    public void shareSingleImage() {
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
            startActivity(Intent.createChooser(shareIntent, "分享到"));
        }else {
            Toast.makeText(MainActivity.this, "请先从图库选择一张图片", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 保存图片到本地图库
     * @param context
     * @param bmp
     */
    public void saveImageToGallery(Context context, Bitmap bmp) {
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
        Toast.makeText(MainActivity.this, "图片已保存至" + path, Toast.LENGTH_SHORT).show();
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
    }

    /**
     * select picture
     */
    private void selectImage(){
        MultiImageSelector.create(MainActivity.this)
                .showCamera(true) // 是否显示相机. 默认为显示
//                .count(1) // 最大选择图片数量, 默认为9. 只有在选择模式为多选时有效
                .single() // 单选模式
//                .multi() // 多选模式, 默认模式;
//                .origin(ArrayList<String>) // 默认已选择图片. 只有在选择模式为多选时有效
                .start(MainActivity.this, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE){
            if(resultCode == RESULT_OK){
                // 获取返回的图片列表
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                // 处理你自己的逻辑 ....
                if(path != null && path.size() > 0){
                    p = path.get(0);
//                    onSelected();
                    bitmap = getImage(p);
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    /**
     * 选择图片后压缩展示
     */
    private void onSelected(){
        bitmap = BitmapFactory.decodeFile(p);
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
        imageView.setImageBitmap(bitmap);
    }

    /**
     * 获取压缩后的图片
     * @param srcPath
     * @return
     */
    private Bitmap getImage(String srcPath) {
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

    private Bitmap compressImage(Bitmap image) {
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
    private void handlePicture(){
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        copy = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        before = new int[width * height];
        after = new int[width * height];
        bitmap.getPixels(before, 0, width, 0, 0, width, height);
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
        iv_after.setImageBitmap(copy);
    }


    /**
     * 处理指定的图片为黑白照片
     */
    private void colored(){
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        copy = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        before = new int[width * height];
        after = new int[width * height];
        bitmap.getPixels(before, 0, width, 0, 0, width, height);
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
        iv_after.setImageBitmap(copy);
    }

    /**
     * 处理指定的图片为半透明照片
     */
    private void halfTrans(){
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        copy = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        before = new int[width * height];
        after = new int[width * height];
        bitmap.getPixels(before, 0, width, 0, 0, width, height);
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
        iv_after.setImageBitmap(copy);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_color:// 黑白照片
                colored();
                break;
            case R.id.tv_trans:// 半透明
                halfTrans();
                break;
            case R.id.image:
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // 应用没有读取手机外部存储的权限
                    // 申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            100);
                }else {
                    selectImage();
                }
                break;
            case R.id.tv_identify:// 识别身份证信息
                TecentHttpUtil.uploadIdCard(BitMapUtils.bitmapToBase64(bitmap), "0", new TecentHttpUtil.SimpleCallBack() {
                    @Override
                    public void Succ(String res) {
                        IdentifyResult result = new Gson().fromJson(res, IdentifyResult.class);
                        if(result != null){
                            if(result.getErrorcode() == 0){
                                // 识别成功
                                showDialogInfo(result);
                            }else {
                                Toast.makeText(MainActivity.this, result.getErrormsg(), Toast.LENGTH_SHORT).show();
                                /*switch (result.getErrorcode()){
                                    case -7001:
                                        Toast.makeText(MainActivity.this, "未检测到身份证，请对准边框(请避免拍摄时倾角和旋转角过大、摄像头)", Toast.LENGTH_SHORT).show();
                                        break;
                                    case -7002:
                                        Toast.makeText(MainActivity.this, "请使用第二代身份证件进行扫描", Toast.LENGTH_SHORT).show();
                                        break;
                                    case -7003:
                                        Toast.makeText(MainActivity.this, "不是身份证正面照片(请使用带证件照的一面进行扫描)", Toast.LENGTH_SHORT).show();
                                        break;
                                    case -7004:
                                        Toast.makeText(MainActivity.this, "不是身份证反面照片(请使用身份证反面进行扫描)", Toast.LENGTH_SHORT).show();
                                        break;
                                    case -7005:
                                        Toast.makeText(MainActivity.this, "确保扫描证件图像清晰", Toast.LENGTH_SHORT).show();
                                        break;
                                    case -7006:
                                        Toast.makeText(MainActivity.this, "请避开灯光直射在证件表面", Toast.LENGTH_SHORT).show();
                                        break;
                                    default:
                                        Toast.makeText(MainActivity.this, "识别失败，请稍后重试", Toast.LENGTH_SHORT).show();
                                        break;
                                }*/
                            }
                        }
                    }

                    @Override
                    public void error() {

                    }
                });
                break;
            case R.id.tv_change:
                if(bitmap != null){
                    // 处理图片
                    handlePicture();
                }else {
                    Toast.makeText(MainActivity.this, "please select a picture first", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 显示对话框
     * @param result
     */
    private void showDialogInfo(final IdentifyResult result){
        StringBuilder sb = new StringBuilder();
        sb.append("姓名：" + result.getName() + "\n");
        sb.append("性别：" + result.getSex() + "\t" + "民族：" + result.getNation() + "\n");
        sb.append("出生：" + result.getBirth() + "\n");
        sb.append("住址：" + result.getAddress() + "\n" + "\n");
        sb.append("公民身份号码：" + result.getId() + "\n");
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        dialogInfo = builder.setTitle("识别成功")
                .setMessage(sb.toString())
                .setPositiveButton("复制号码", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("text", result.getId());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(MainActivity.this, "身份证号已复制到粘贴板", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        dialogInfo.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if(grantResults[0] ==  PackageManager.PERMISSION_GRANTED){
                // 申请到权限
                selectImage();
            }else {
                Toast.makeText(getApplicationContext(), "没有读取外部存储权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
