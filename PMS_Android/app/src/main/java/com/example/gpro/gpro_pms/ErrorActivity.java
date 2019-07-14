package com.example.gpro.gpro_pms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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

public class ErrorActivity extends AppCompatActivity {

    int total = 0;
    TextView lbResult;
    String urlPath = "",
            cspId = "0",
            equipCode = "11",
            clusterId = "16",
            appId = "11",
            IPAddress = "";
    String[] titleArr = {"MÃ LỖI", "TÊN LỖI", "LOẠI LỖI", "CÔNG ĐOẠN", "SẢN LƯỢNG"};
    JsonArrayRequest jsonArrayRequest;
    TableLayout table;
    TableRow tableRow;
    EditText[] editTexts;
    TextView label;
    ImageButton imageButton;

    JSONObject jsonObject;
    RequestQueue mRequestQueue;

    ImageButton btnReload, btnBack, btnConfig;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hide thanh action bar to fullscreen
        getSupportActionBar().hide();

        setContentView(R.layout.activity_error);

        sharedPreferences = getSharedPreferences("PMS_SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Boolean isFirst = sharedPreferences.getBoolean("IS_FIRTS_LAUNCHER", true);
        if (isFirst) {
            Intent intent = new Intent(ErrorActivity.this, ConfigActivity.class);
            startActivity(intent);
        } else {
            IPAddress = (sharedPreferences.getString("IP", "0.0.0.0"));
            appId = (sharedPreferences.getString("AppId", "0"));
            equipCode = (sharedPreferences.getString("EquipCode", "0"));
            clusterId = (sharedPreferences.getString("ClusterId", "0"));
        }

        // Instantiate the cache
        final Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        final Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        Intent intent = getIntent();
        cspId = intent.getStringExtra("cspId");
        IPAddress = (String) intent.getStringExtra("rootUrl");
        equipCode = (String) intent.getStringExtra("equipId");
        clusterId = (String) intent.getStringExtra("clusId");
        appId = (String) intent.getStringExtra("appId");

        lbResult = (TextView) findViewById(R.id.lbResult);

        btnReload = (ImageButton) findViewById(R.id.imageButtonReload);
        btnReload.setOnClickListener(new View.OnClickListener() {
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

                total = 0;
                //endregion
                GenerateButton();
            }
        });

        btnBack = (ImageButton) findViewById(R.id.imageButtonBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ErrorActivity.this, MainActivity.class);
                startActivity(intent1);
            }
        });

        btnConfig = (ImageButton) findViewById(R.id.imageButtonSetting);
        btnConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ErrorActivity.this, ConfigActivity.class);
                startActivity(intent1);
            }
        });

        GenerateButton();
    }

    public void GenerateButton() {
        //region tao buttons
        urlPath = (IPAddress + "/api/serviceapi/geterrors?cspid=" + cspId);
        jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, urlPath, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        table = (TableLayout) findViewById(R.id.table);
                        //region draw title
                        tableRow = new TableRow(ErrorActivity.this);
                        tableRow.setLayoutParams(new TableRow.LayoutParams(
                                TableLayout.LayoutParams.WRAP_CONTENT,
                                TableLayout.LayoutParams.MATCH_PARENT,
                                1.0f));

                        for (int ii = 0; ii < 5; ii++) {
                            label = new TextView(ErrorActivity.this);
                            label.setLayoutParams(new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    1.0f
                            ));
                            label.setText(titleArr[ii]);
                            label.setTextSize(25);
                            label.setPadding(0, 20, 0, 20);
                            label.setTypeface(label.getTypeface(), Typeface.BOLD);
                            label.setBackgroundResource(R.drawable.cell_shape);
                            label.setGravity(Gravity.CENTER);
                            label.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                            if (ii == 4) {
                                TableRow.LayoutParams rowSpanLayout = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                                rowSpanLayout.span = 2;
                                tableRow.addView(label, rowSpanLayout);
                            } else
                                tableRow.addView(label);
                        }
                        table.addView(tableRow);
                        //endregion
                        if (response != null && response.length() > 0) {
                            editTexts = new EditText[response.length()];
                            int index = 0;
                            //region draw data row
                            for (int i = 0; i < response.length(); i++) {
                                tableRow = new TableRow(ErrorActivity.this);
                                tableRow.setLayoutParams(new TableRow.LayoutParams(
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
                                        if (ii < 5) {
                                            //region
                                            label = new TextView(ErrorActivity.this);
                                            label.setLayoutParams(new TableRow.LayoutParams(
                                                    TableRow.LayoutParams.MATCH_PARENT,
                                                    TableRow.LayoutParams.MATCH_PARENT,
                                                    1.0f
                                            ));

                                            label.setTextSize(20);
                                            if (i % 2 == 0)
                                                label.setBackgroundResource(R.drawable.cell_data);
                                            else
                                                label.setBackgroundResource(R.drawable.cell_data_1);
                                            label.setGravity(Gravity.CENTER);
                                            String lbtext = "";
                                            switch (ii) {
                                                case 0:
                                                    lbtext = (jsonObject.optString("Code"));
                                                    break;
                                                case 1:
                                                    lbtext = (jsonObject.optString("Name"));
                                                    break;
                                                case 2:
                                                    lbtext = (jsonObject.optString("GroupName"));
                                                    break;
                                                case 3:
                                                    lbtext = (jsonObject.optString("PhaseName"));
                                                    break;
                                                case 4:
                                                    lbtext = (jsonObject.optString("Quantity"));
                                                    total += jsonObject.optInt("Quantity");
                                                    break;
                                            }
                                            label.setText((lbtext == "null" ? "" : lbtext));
                                            tableRow.addView(label);
                                            //endregion
                                        } else {
                                            LinearLayout linearLayout = new LinearLayout(ErrorActivity.this);

                                            //region textbox
                                            editTexts[index] = new EditText(ErrorActivity.this);
                                            editTexts[index].setLayoutParams(new TableRow.LayoutParams(
                                                    100,65,
                                                    // TableRow.LayoutParams.MATCH_PARENT,
                                                   // TableRow.LayoutParams.MATCH_PARENT,
                                                    1.0f
                                            ));
                                            editTexts[index].setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                                            editTexts[index].setText("1");
                                            editTexts[index].setTextSize(17);
                                            editTexts[index].setGravity(Gravity.CENTER);
                                            editTexts[index].setTextColor(Color.RED);
                                            editTexts[index].setTypeface(editTexts[index].getTypeface(), Typeface.BOLD);
                                            linearLayout.addView(editTexts[index]);
                                            //endregion

                                            //region plus button
                                            imageButton = new ImageButton(ErrorActivity.this);
                                            imageButton.setLayoutParams(new TableRow.LayoutParams(
                                                    65, 65,
                                                    1.0f
                                            ));
                                            imageButton.setPadding(0, 0, 0, 0);
                                            imageButton.getBackground().setAlpha(0);
                                            imageButton.setImageResource(R.drawable.plus_button_128);
                                            imageButton.setScaleType(ImageButton.ScaleType.FIT_START);
                                            imageButton.setAdjustViewBounds(true);
                                            final int finalIndex = index;
                                            final String errCode = jsonObject.optString("Code");
                                            imageButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Button_Click(true, editTexts[finalIndex].getText().toString(), 4, errCode );
                                                }
                                            });
                                            linearLayout.addView(imageButton);
                                            //endregion

                                            //region minus button
                                            imageButton = new ImageButton(ErrorActivity.this);
                                            imageButton.setLayoutParams(new TableRow.LayoutParams(
                                                    65, 65,
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
                                                    Button_Click(false, editTexts[finalIndex].getText().toString(), 4, errCode );
                                                }
                                            });
                                            linearLayout.addView(imageButton);
                                            //endregion
                                            if (i % 2 != 0)
                                                linearLayout.setBackgroundResource(R.drawable.cell_data_1);
                                            linearLayout.setPadding(5, 10, 5, 10);
                                            linearLayout.setGravity(Gravity.RIGHT);
                                            tableRow.addView(linearLayout);
                                            index++;
                                        }
                                    }
                                }
                                table.addView(tableRow);
                            }
                            //endregion
                        }
                        //region total row
                        tableRow = new TableRow(ErrorActivity.this);
                        tableRow.setLayoutParams(new TableRow.LayoutParams(
                                TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.MATCH_PARENT,
                                1.0f));
                        label = new TextView(ErrorActivity.this);
                        label.setLayoutParams(new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.MATCH_PARENT,
                                1.0f
                        ));

                        label.setTextSize(30);
                        label.setText("Tổng cộng ");
                        label.setTypeface(label.getTypeface(), Typeface.BOLD);
                        label.setGravity(Gravity.CENTER);
                        label.setBackgroundResource(R.drawable.cell_data);
                        TableRow.LayoutParams rowSpanLayout1 = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                        rowSpanLayout1.span = 4;
                        tableRow.addView(label, rowSpanLayout1);

                        label = new TextView(ErrorActivity.this);
                        label.setLayoutParams(new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.MATCH_PARENT,
                                1.0f
                        ));
                        label.setTextSize(30);
                        label.setTypeface(label.getTypeface(), Typeface.BOLD);
                        label.setGravity(Gravity.CENTER);
                        label.setText((total + ""));
                        label.setBackgroundResource(R.drawable.cell_data);
                        tableRow.addView(label);

                        label = new TextView(ErrorActivity.this);
                        label.setLayoutParams(new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.MATCH_PARENT,
                                1.0f
                        ));
                        label.setBackgroundResource(R.drawable.cell_data);
                        tableRow.addView(label);
                        table.addView(tableRow);
                        //endregion
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

    private void Button_Click(boolean isPlus, String quantities, int type, String errId ) {
        String str = (IPAddress + "/api/serviceapi/NhapSL?cspId=" + cspId + "&proType=" + type + "&isPlus=" + (isPlus ? "true" : "false") + "&sl=" + quantities + "&equipCode=" + equipCode + "&errId=" + errId + "&clusId=" + clusterId + "&appId=" + appId);
        RequestQueue rqQue = Volley.newRequestQueue(ErrorActivity.this);
        JsonObjectRequest jRequest = new JsonObjectRequest(
                Request.Method.GET, str, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Boolean rs = response.optBoolean("IsSuccess");
                        if (rs) {
                            lbResult.setText("Kết quả : nhập sản lượng thành công.");
                        }
                        else
                            lbResult.setText("Kết quả : "+response.optString("DataSendKeyPad") );
                        btnReload.performClick();
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
