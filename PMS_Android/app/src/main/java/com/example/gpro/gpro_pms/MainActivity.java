package com.example.gpro.gpro_pms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.view.View.TEXT_ALIGNMENT_CENTER;

public class MainActivity extends AppCompatActivity {

    LinearLayout linearLayout, childLinearLayout;
    TableLayout table, childTable;
    TableRow tbRow, childRow;
    EditText[] editText;
    ImageButton imageButton, imageButtonReload, btnConfig;
    Button button;
    TextView txt, lbResult;
    String[] titleArr = {"MÃ HÀNG", "SIZE", "MÀU", "THOÁT CHUYỀN", "  KIỂM ĐẠT  ", "LỖI"};
    String[] dataArr = {"DECATHLON ddfd", "M", "WHITE", "999/999/99999", "999/999/99999", "99"};

    String IPAddress = "http://192.168.1.8:1000",
            urlPath = "",
            equipCode = "11",
            clusterId = "16",
            appId = "11",
            lineId = "0",
            data = "";
    JsonArrayRequest jsonArrayRequest;
    JSONObject jsonObject;
    RequestQueue mRequestQueue;
    SharedPreferences sharedPreferences;

    boolean isTablet = true;
    int labelZise = 25,
            buttonWidth = 65,
            textSize = 20,
            buttonSize = 19;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hide thanh action bar to fullscreen
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);
        lbResult = (TextView) findViewById(R.id.lbResult);

        sharedPreferences = getSharedPreferences("PMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
            startActivity(intent);
        } else {
            IPAddress = "http://" + (sharedPreferences.getString("IP", "0.0.0.0"));
            appId = (sharedPreferences.getString("AppId", "0"));
            equipCode = (sharedPreferences.getString("EquipCode", "0"));
            clusterId = (sharedPreferences.getString("ClusterId", "0"));
            lineId = (sharedPreferences.getString("LineId", "0"));
           String str = (sharedPreferences.getString("IsTablet", "0")).toString();
           if(str.equals("0")){
               isTablet =   false  ;
           }
        }
        if (!isTablet) {
            labelZise = 18;
            buttonWidth = 100;
            textSize = 16;
            buttonSize=12;
        }

        // Instantiate the cache
        final Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        final Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        GenerateButton();

        imageButtonReload = (ImageButton) findViewById(R.id.imageButtonReload);
        imageButtonReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //region remove old row
                if (table != null) {
                    int childCount = table.getChildCount();
                    // Remove all rows except the first one
                    if (childCount > 1) {
                        table.removeViews(1, childCount - 1);
                    }
                }
                //endregion

                GenerateButton();
            }
        });

        btnConfig = (ImageButton) findViewById(R.id.imageButtonSetting);
        btnConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(MainActivity.this, ConfigActivity.class);
                startActivity(intent1);
            }
        });

    }

    public void GenerateButton() {
        //region tao buttons
        urlPath = (IPAddress + "/api/serviceapi/getdayinfo?lineid=" + lineId);
        jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, urlPath, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        table = (TableLayout) findViewById(R.id.table);

                        //region draw title
                        tbRow = new TableRow(MainActivity.this);
                        tbRow.setLayoutParams(new TableRow.LayoutParams(
                                TableLayout.LayoutParams.WRAP_CONTENT,
                                TableLayout.LayoutParams.MATCH_PARENT,
                                1.0f));

                        for (int ii = 0; ii < 6; ii++) {
                            txt = new TextView(MainActivity.this);
                            txt.setLayoutParams(new TableRow.LayoutParams(
                                    //    10,10,
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    1.0f
                            ));
                            txt.setText(titleArr[ii]);
                            txt.setTextSize(labelZise);
                            txt.setPadding(0, 20, 0, 20);
                            txt.setTypeface(txt.getTypeface(), Typeface.BOLD);
                            txt.setBackgroundResource(R.drawable.cell_shape);
                            txt.setGravity(Gravity.CENTER);
                            txt.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                            if (ii == 1) {
                                //  txt.getLayoutParams().
                            }
                            tbRow.addView(txt);
                        }
                        table.addView(tbRow);
                        //endregion

                        if (response != null && response.length() > 0) {
                            editText = new EditText[(response.length() * 3)];
                            int index = 0;
                            //region draw data row
                            for (int i = 0; i < response.length(); i++) {
                                tbRow = new TableRow(MainActivity.this);
                                tbRow.setLayoutParams(new TableRow.LayoutParams(
                                        TableLayout.LayoutParams.MATCH_PARENT,
                                        TableLayout.LayoutParams.MATCH_PARENT,
                                        1.0f));
                                jsonObject = null;
                                try {
                                    jsonObject = response.getJSONObject(i);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (jsonObject != null) {
                                    final Integer cspId = jsonObject.optInt("cspId");
                                    for (int ii = 0; ii < 6; ii++) {
                                        switch (ii) {
                                            case 0:
                                            case 1:
                                            case 2:
                                                //region
                                                txt = new TextView(MainActivity.this);
                                                txt.setLayoutParams(new TableRow.LayoutParams(
                                                        TableRow.LayoutParams.MATCH_PARENT,
                                                        TableRow.LayoutParams.MATCH_PARENT,
                                                        1.0f
                                                ));

                                                txt.setTextSize(labelZise);
                                                if (i % 2 == 0)
                                                    txt.setBackgroundResource(R.drawable.cell_data);
                                                else
                                                    txt.setBackgroundResource(R.drawable.cell_data_1);
                                                txt.setGravity(Gravity.CENTER);
                                                String lbtext = "";
                                                switch (ii) {
                                                    case 0:
                                                        lbtext = (jsonObject.optString("ProName"));
                                                        break;
                                                    case 1:
                                                        lbtext = (jsonObject.optString("SizeName"));
                                                        break;
                                                    case 2:
                                                        lbtext = (jsonObject.optString("ColorName"));
                                                        break;
                                                }
                                                txt.setText((lbtext == "null" ? "" : lbtext));
                                                tbRow.addView(txt);
                                                //endregion
                                                break;
                                            case 3:
                                            case 4:
                                            case 5:
                                                linearLayout = new LinearLayout(MainActivity.this);
                                                linearLayout.setOrientation(LinearLayout.VERTICAL);
                                                linearLayout.setLayoutParams(new TableRow.LayoutParams(
                                                        TableRow.LayoutParams.MATCH_PARENT,
                                                        TableRow.LayoutParams.MATCH_PARENT,
                                                        1.0f
                                                ));

                                              //  LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                                              //  params.setMargins(3, 30, 3, 3);
                                             //   linearLayout.setLayoutParams(params);

                                                if (i % 2 == 0)
                                                    linearLayout.setBackgroundResource(R.drawable.cell_data);
                                                else
                                                    linearLayout.setBackgroundResource(R.drawable.cell_data);

                                                //region   sl label
                                                txt = new TextView(MainActivity.this);
                                                txt.setLayoutParams(new LinearLayout.LayoutParams(
                                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                                        1.0f
                                                ));
                                                if (i % 2 != 0)
                                                    txt.setBackgroundResource(R.drawable.cell_data_1);
                                                txt.setTextColor(Color.BLUE);
                                                if (ii == 3) {
                                                    //thoat chuyen
                                                    data = jsonObject.optString("TC");
                                                    data += "/" + jsonObject.optInt("DinhMuc");
                                                    data += "/" + jsonObject.optString("LK_TC");
                                                } else if (ii == 4) {
                                                    //kiem dat
                                                    data = jsonObject.optString("KCS");
                                                    data += "/" + jsonObject.optInt("DinhMuc");
                                                    data += "/" + jsonObject.optString("LK_KCS");
                                                } else if (ii == 5) {
                                                    //loi
                                                    data = jsonObject.optString("ERR");
                                                }
                                                txt.setText(data);
                                                txt.setTextSize(labelZise);
                                                // txt.setBackgroundResource(R.drawable.cell_shape);
                                                txt.setGravity(Gravity.CENTER);
                                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                                                params.setMargins(0, 0, 0, 10);

                                                linearLayout.addView(txt,params);
                                                //endregion

                                                if (ii < 5) {
                                                    // thoat chuyen & kcs
                                                    //region textbox & button
                                                    LinearLayout linearLayout1 = new LinearLayout(MainActivity.this);
                                                    linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
                                                    linearLayout1.setLayoutParams(new LinearLayout.LayoutParams(
                                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                                            1.0f));

                                                    editText[index] = new EditText(MainActivity.this);
                                                    editText[index].setLayoutParams(new LinearLayout.LayoutParams(
                                                               buttonWidth,buttonWidth,
                                                            //TableRow.LayoutParams.MATCH_PARENT,
                                                           // LinearLayout.LayoutParams.MATCH_PARENT,
                                                            1.0f
                                                    ));
                                                    editText[index].setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                                                    editText[index].setText("1");
                                                    editText[index].setGravity(Gravity.CENTER);
                                                    editText[index].setTextSize(textSize);
                                                    editText[index].setTextColor(Color.RED);
                                                    editText[index].setPadding(0,0,0,0);
                                                    editText[index].setTypeface(editText[index].getTypeface(), Typeface.BOLD);
                                                    linearLayout1.addView(editText[index]);

                                                    imageButton = new ImageButton(MainActivity.this);
                                                    imageButton.setLayoutParams(new LinearLayout.LayoutParams(
                                                            buttonWidth, buttonWidth,
                                                            1.0f
                                                    ));
                                                    imageButton.setPadding(0, 0, 0, 0);
                                                    imageButton.getBackground().setAlpha(0);
                                                    imageButton.setImageResource(R.drawable.plus_button_128);
                                                    imageButton.setScaleType(ImageButton.ScaleType.FIT_START);
                                                    imageButton.setAdjustViewBounds(true);
                                                    imageButton.setPadding(0,0,0,0);
                                                    final int finalIndex = index;
                                                    int proType = 0;
                                                    switch (ii) {
                                                        case 3:
                                                            proType = 0;
                                                            break;
                                                        case 4:
                                                            proType = 1;
                                                            break;
                                                        case 5:
                                                            proType = 4;
                                                            break;
                                                    }
                                                    final int fproType = proType;
                                                    imageButton.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Button_Click(cspId, true, editText[finalIndex].getText().toString(), fproType);
                                                        }
                                                    });
                                                    linearLayout1.addView(imageButton);

                                                    imageButton = new ImageButton(MainActivity.this);
                                                    imageButton.setLayoutParams(new LinearLayout.LayoutParams(
                                                            buttonWidth, buttonWidth,
                                                            1.0f
                                                    ));
                                                    imageButton.setImageResource(R.drawable.minus_button_128);
                                                    imageButton.getBackground().setAlpha(0);
                                                    imageButton.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
                                                    imageButton.setAdjustViewBounds(true);
                                                    imageButton.setPadding(0, 0, 0, 0);
                                                    imageButton.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Button_Click(cspId, false, editText[finalIndex].getText().toString(), fproType);
                                                        }
                                                    });
                                                     linearLayout1.addView(imageButton);
                                                    linearLayout1.setPadding(0, 0, 0, 0);

                                                    linearLayout.addView(linearLayout1);
                                                    index++;
                                                    //endregion
                                                } else {
                                                    //region error
                                                    LinearLayout linearLayout1 = new LinearLayout(MainActivity.this);
                                                    linearLayout1.setLayoutParams(new LinearLayout.LayoutParams(
                                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                                            1.0f));

                                                    button = new Button(MainActivity.this);
                                                    button.setLayoutParams(new TableRow.LayoutParams(
                                                            170, buttonWidth,
                                                            1.0f
                                                    ));
                                                    // imageButton.setImageResource(R.drawable.minus_button_128);
                                                    // imageButton.getBackground().setAlpha(0);
                                                    //  imageButton.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
                                                    //  imageButton.setAdjustViewBounds(true);
                                                    button.setPadding(0, 0, 0, 0);
                                                    button.setText("Nhập sản lượng");
                                                    button.setTextSize(buttonSize);
                                                   // button.setTypeface(null, Typeface.BOLD);
                                                    button.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Intent intent = new Intent(MainActivity.this, ErrorActivity.class);
                                                            intent.putExtra("cspId", cspId.toString());
                                                            intent.putExtra("rootUrl", IPAddress.toString());
                                                            intent.putExtra("equipId", equipCode.toString());
                                                            intent.putExtra("clusId", clusterId.toString());
                                                            intent.putExtra("appId", appId.toString());
                                                            startActivity(intent);
                                                        }
                                                    });
                                                    linearLayout1.addView(button);
                                                    linearLayout1.setPadding(5, 10, 5, 10);
                                                    linearLayout.addView(linearLayout1);
                                                    //endregion
                                                }
                                                tbRow.addView(linearLayout);
                                                break;
                                        }
                                    }
                                }
                                table.addView(tbRow);
                            }
                            //endregion
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //  lbNumber.setText(  "ERR");
            }
        });

        if (mRequestQueue != null)
            mRequestQueue.add(jsonArrayRequest);
        //endregion
    }

    private void Button_Click(Integer cspId, boolean isPlus, String quantities, int type) {
        String str = (IPAddress + "/api/serviceapi/NhapSL?cspId=" + cspId + "&proType=" + type + "&isPlus=" + (isPlus ? "true" : "false") + "&sl=" + quantities + "&equipCode=" + equipCode + "&errId=0&clusId=" + clusterId + "&appId=" + appId);
        RequestQueue rqQue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jRequest = new JsonObjectRequest(
                Request.Method.GET, str, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Boolean rs = response.optBoolean("IsSuccess");
                        if (rs) {
                            lbResult.setText("Kết quả : nhập sản lượng thành công.");
                        } else
                            lbResult.setText("Kết quả : " + response.optString("DataSendKeyPad"));

                        imageButtonReload.performClick();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        lbResult.setText("Kết quả : Không kết nối được với máy chủ.");
                    }
                }
        );
        jRequest.setShouldCache(false);
        jRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(jRequest);
    }

}
