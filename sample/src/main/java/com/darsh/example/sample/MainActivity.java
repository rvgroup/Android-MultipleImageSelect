package com.darsh.example.sample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;

import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final String DIR_SD = "MultiImageSelector-b8b16f7cf85d";
    final String FILENAME_SD = "2f522aac-152c-4731-95d8-b8b16f7cf85d.sv";
    final String FILE_WITH_CUSTOM_PATH = "custompath.txt";

    private boolean inClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    protected void onStart() {
        super.onStart();

        if (!this.inClose) {
            Intent intent = new Intent(MainActivity.this, AlbumSelectActivity.class);

            File file = this.GetFile(FILE_WITH_CUSTOM_PATH);
            if (file.exists()) {
                intent.putExtra("customPath", DIR_SD);
            }

            startActivityForResult(intent, Constants.REQUEST_CODE);
        }

        this.inClose = false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try
        {
            if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
                ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);

                String xml = this.getImagesPathXml(images);
                this.saveTextToFile(xml);
            }
        }
        catch (Exception ex)
        {
        }
        finally
        {
            this.closeApp();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        this.closeApp();
    }

    private void closeApp() {
        this.inClose = true;

        this.finish();
        //this.moveTaskToBack(true);

        //System.exit(0);
    }

    private String getImagesPathXml(ArrayList<Image> images) {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "files");
            serializer.attribute("", "number", String.valueOf(images.size()));

            for (Image img: images){
                String path = img.path;

                serializer.startTag("", "file");
                serializer.text(path);
                serializer.endTag("", "file");
            }

            serializer.endTag("", "files");
            serializer.endDocument();

            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void saveTextToFile(String text) throws  Exception {
        if (this.isExternalStorageWritable()) {
            File sdFile = this.GetFile(FILENAME_SD);
            sdFile.delete();

            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
                bw.write(text);
                bw.close();

                //Log.d(LOG_TAG, "Файл записан на SD: " + sdFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File GetFile(String localPath) {
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();

        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);

        // создаем каталог
        sdPath.mkdirs();

        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, localPath);

        return  sdFile;
    }
}
