package com.example.artem.myjpgapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.IDN;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends Activity {

    private EditText et;
    private String sIinput;
    private ImageView img;

    Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = (Button) findViewById(R.id.btn);
        et = (EditText) findViewById(R.id.et);
        img = (ImageView) findViewById(R.id.img);

        OnClickListener ocl = new OnClickListener() {
            @Override
            public void onClick(View v) {
                sIinput = et.getText().toString();
                new ResponseTask().execute(sIinput);
            }
        };
        btn.setOnClickListener(ocl);
        registerForContextMenu(img);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 0, 0, "Save");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        try {
            File file = new File(getFilesDir(), "img");
            Log.d("myLogs", getFilesDir().toString());
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();

//            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            File f = new File(file.getAbsolutePath());
//            Uri uri = Uri.fromFile(f);
//            intent.setData(uri);
//            this.sendBroadcast(intent);
//
            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, file.getName(), file.getName());

            Log.d("myLogs", file.getAbsolutePath());
            Log.d("myLogs", file.getPath());
            Log.d("myLogs", file.getName());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Save", Toast.LENGTH_LONG).show();
        return super.onContextItemSelected(item);
    }

    class ResponseTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                String sPunycode = params[0].replace(" ", "");
                sPunycode = IDN.toASCII(sPunycode);

                URL url = new URL("http://" + sPunycode + ".jpg.to" );
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream is = httpURLConnection.getInputStream();
                BufferedReader bf = new BufferedReader(new InputStreamReader(is));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bf.readLine()) != null){
                    sb.append(line);
                }

                String sUrl = parseUrlTag(sb.toString());
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(sUrl).getContent());

                httpURLConnection.disconnect();
                is.close();
                bf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            img.setImageBitmap(bitmap);
        }

        private String parseUrlTag(String sParse) {
            Pattern pattern = Pattern.compile("src=[^']*\"");
            Matcher matcher = pattern.matcher(sParse);
            if (matcher.find()){
                sParse = sParse.substring(matcher.start() + 5, matcher.end() - 1);
            }
            return sParse;
        }
    }
}
//сохранить картинку, история последних 10 запросов, возможность подбора случайных слов-картинок
//Запрос sdfs, большие картинки, перенос