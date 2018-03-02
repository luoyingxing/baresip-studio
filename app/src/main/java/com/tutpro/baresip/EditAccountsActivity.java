package com.tutpro.baresip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class EditAccountsActivity extends AppCompatActivity {

    private EditText editText;
    private String path;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_accounts);

        editText = (EditText)findViewById(R.id.editAccounts);
        path = getApplicationContext().getFilesDir().getAbsolutePath() + "/accounts";
        file = new File(path);
        String content;

        if (!file.exists()) {
            Log.e("Baresip", "Failed to find accounts file");
            content = "No accounts";
        } else {
            Log.e("Baresip", "Found accounts file");
            int length = (int)file.length();
            byte[] bytes = new byte[length];
            try {
                FileInputStream in = new FileInputStream(file);
                try {
                    in.read(bytes);
                } finally {
                    in.close();
                }
                content = new String(bytes);
            } catch (java.io.IOException e) {
                Log.e("Baresip", "Failed to read accounts file: " + e.toString());
                content = "Failed to read account file";
            }
        }

        Log.d("Baresip", "Content length is: " + content.length());

        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.textsize));
        editText.setText(content, TextView.BufferType.EDITABLE);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent();
        switch (item.getItemId()) {
        case R.id.save:
            try {
                FileOutputStream fOut =
                    new FileOutputStream(file.getAbsoluteFile(), false);
                OutputStreamWriter fWriter = new OutputStreamWriter(fOut);
                String res = editText.getText().toString();
                try {
                    fWriter.write(res);
                    fWriter.close();
                    fOut.close();
                } catch (java.io.IOException e) {
                    Log.e("Baresip", "Failed to write accounts file: " +
                          e.toString());
                }
            } catch (java.io.FileNotFoundException e) {
                Log.e("Baresip", "Failed to find accounts file: " +
                      e.toString());
            }
            Log.d("Baresip", "Updated accounts file");
            setResult(RESULT_OK, i);
            finish();
            return true;
        case R.id.cancel:
            setResult(RESULT_CANCELED, i);
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}

