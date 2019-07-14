package com.example.gpro.gpro_pms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

public class ConfigActivity extends AppCompatActivity {
Switch swTB;
    ImageButton ibtnBack, ibtnSave;
    EditText txtIp, txtAppId,txtEquipCode,txtClusterId, txtLineId;
    Intent intent;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_config);

        txtAppId = (EditText)findViewById(R.id.txtAppId);
        txtIp = (EditText)findViewById(R.id.txtIp);
        txtEquipCode = (EditText)findViewById(R.id.txtEquipCode);
        txtClusterId = (EditText)findViewById(R.id.txtClusterId);
        txtLineId = (EditText)findViewById(R.id.txtLineId);
        swTB = (Switch) findViewById(R.id.swTB);

        sharedPreferences = getSharedPreferences("PMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        txtIp.setText(sharedPreferences.getString("IP", "0.0.0.0"));
        txtAppId.setText(sharedPreferences.getString("AppId", "0"));
        txtEquipCode.setText(sharedPreferences.getString("EquipCode", "0"));
        txtClusterId.setText(sharedPreferences.getString("ClusterId", "0"));
        txtLineId.setText(sharedPreferences.getString("LineId", "0"));
        swTB.setChecked( (sharedPreferences.getString("IsTablet", "0").equals("0")?false:true));

        ibtnBack = (ImageButton) findViewById(R.id.btnBack);
        ibtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(ConfigActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        //region save button
        ibtnSave = (ImageButton)findViewById(R.id.btnSave);
        ibtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtIp.getText().toString() == "")
                    Toast.makeText(ConfigActivity.this, "Vui lòng nhập địa chỉ máy chủ.", Toast.LENGTH_LONG).show();
                else if (txtAppId.getText().toString() == "")
                    Toast.makeText(ConfigActivity.this, "Vui lòng nhập mã ứng dụng.", Toast.LENGTH_LONG).show();
                else if (txtEquipCode.getText().toString() == "")
                    Toast.makeText(ConfigActivity.this, "Vui lòng nhập mã thiết bị.", Toast.LENGTH_LONG).show();
                else if (txtLineId.getText().toString() == "")
                    Toast.makeText(ConfigActivity.this, "Vui lòng nhập mã chuyền.", Toast.LENGTH_LONG).show();
                else if (txtClusterId.getText().toString() == "")
                    Toast.makeText(ConfigActivity.this, "Vui lòng nhập mã cụm.", Toast.LENGTH_LONG).show();
                else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("IS_FIRTS_LAUNCHER", false);
                    editor.putString("IP", txtIp.getText().toString());
                    editor.putString("AppId", txtAppId.getText().toString());
                    editor.putString("EquipCode", txtEquipCode.getText().toString());
                    editor.putString("ClusterId", txtClusterId.getText().toString());
                    editor.putString("LineId", txtLineId.getText().toString());
                    editor.putString("IsTablet",  (swTB.isChecked()?"1":"0") );
                    editor.apply();
                    intent = new Intent(ConfigActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
        //endregion
    }
}
