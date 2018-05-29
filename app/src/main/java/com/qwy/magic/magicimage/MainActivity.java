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
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
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
    private TextView tv_ocr;
    private final static int REQUEST_IMAGE = 100;
    private String p = null;
    private Bitmap bitmap = null, copy = null;
    private AlertDialog dialog;// 提示分享货保存图片弹窗
    private AlertDialog dialogInfo;// 识别身份证信息弹窗
    private CheckBox checkbox;

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
                        BitMapUtils.saveImageToGallery(MainActivity.this, copy);
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("分享", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BitMapUtils.shareSingleImage(MainActivity.this, copy);
                    }
                })
                .setNegativeButton("不了", null)
                .create();

        imageView = (ImageView) findViewById(R.id.image);
        iv_after = (ImageView) findViewById(R.id.iv_after);
        iv_after.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (copy != null) {
                    dialog.show();
                }
                return false;
            }
        });
        imageView.setOnClickListener(this);
        checkbox = (CheckBox) findViewById(R.id.checkbox);
        tv_color = (TextView) findViewById(R.id.tv_color);
        tv_color.setOnClickListener(this);
        tv_trans = (TextView) findViewById(R.id.tv_trans);
        tv_trans.setOnClickListener(this);
        tv_identify = (TextView) findViewById(R.id.tv_identify);
        tv_identify.setOnClickListener(this);
        tv_ocr = (TextView) findViewById(R.id.tv_ocr);
        tv_ocr.setOnClickListener(this);
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
        }
        textView = (TextView) findViewById(R.id.tv_change);
        textView.setOnClickListener(this);

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
                    bitmap = BitMapUtils.getImage(p);
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_color:// 黑白照片
                copy = BitMapUtils.colored(bitmap);
                iv_after.setImageBitmap(copy);
                break;
            case R.id.tv_trans:// 半透明
                copy = BitMapUtils.halfTrans(bitmap);
                iv_after.setImageBitmap(copy);
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
            case R.id.tv_ocr:// ocr识别身份证信息
                String type = checkbox.isChecked() ? "0" : "1";
                TecentHttpUtil.ocrIdCard(p, type, new TecentHttpUtil.SimpleCallBack() {
                    @Override
                    public void Succ(String res) {
                        Log.e("===========success", res);
                    }

                    @Override
                    public void error() {

                    }
                });
                break;
            case R.id.tv_identify:// 识别身份证信息
                String t = checkbox.isChecked() ? "0" : "1";
                TecentHttpUtil.uploadIdCard(BitMapUtils.bitmapToBase64(bitmap), t, new TecentHttpUtil.SimpleCallBack() {
                    @Override
                    public void Succ(String res) {
                        Log.e("===========success", res);
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
                    copy = BitMapUtils.handlePicture(bitmap);
                    iv_after.setImageBitmap(copy);
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
