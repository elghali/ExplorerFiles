package com.example.explore.explorerfiles;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ListFilesActivity extends ListActivity {

    private static final String AUDIO_CONVERTER_APP = "Switch Audio Converter Free";
    private String path;
    private String filename;
    private List values;
    private ArrayAdapter adapterSingle;//, adapterMultiple;
    private boolean multiple = false;

    private ArrayList<String> selected = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_files);
        //Use the current directory as the title
//        path = "/";

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        path= Environment.getExternalStorageDirectory().getAbsolutePath();
        if(getIntent().hasExtra("path")){
            path = getIntent().getStringExtra("path");
        }
        setTitle(path);

        // Read all files sorted into  the values-array
        values = new ArrayList();
        File dir = new File(path);
        if (!dir.canRead()){
            setTitle(getTitle()+ " (inaccessible)");
        }
        String[] list = dir.list();
        if(list != null){
            for(String file: list){
                if(!file.startsWith(".")){
                    values.add(file);
                }
            }
        }
        Collections.sort(values);

        //put the data into the list
        adapterSingle = new ArrayAdapter(this, android.R.layout.simple_selectable_list_item,
                android.R.id.text1,values);
        //adapterMultiple = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice,
               // android.R.id.text1,values);
        setListAdapter(adapterSingle);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String filename = (String)getListAdapter().getItem(position);
                if(path.endsWith(File.separator)){
                    filename = path + filename;
                }else{
                    filename = path + File.separator + filename;
                }
                if (new File(filename).isDirectory()) {
                    Toast.makeText(getApplicationContext(),"Cannot select a directory!",Toast.LENGTH_SHORT).show();
                    return false;
                }
                //if (!selected.isEmpty()) return false;
                //setListAdapter(adapterMultiple);

                if (/*getListView().isItemChecked(position)*/ selected.contains(filename)){
                    selected.remove(filename);
                    getListView().setItemChecked(position, false);
                    try {
                        view.setBackgroundColor(ContextCompat.getColor(getApplication(), R.color.colorPrimaryLight));
                    } catch (Exception e){}
                    if (selected.isEmpty()){
                        multiple = false;
                        findViewById(R.id.button_done).setVisibility(View.GONE);
                        findViewById(R.id.button_clear).setVisibility(View.GONE);
                    }
                } else {
                    multiple = true;
                    findViewById(R.id.button_done).setVisibility(View.VISIBLE);
                    findViewById(R.id.button_clear).setVisibility(View.VISIBLE);
                    selected.add(filename);
                    getListView().setItemChecked(position, true);
                    try {
                        view.setBackgroundColor(ContextCompat.getColor(getApplication(), R.color.colorText));
                    } catch (Exception e){}
                }
                //Toast.makeText(getApplicationContext(),"Item long clicked",Toast.LENGTH_SHORT).show();
                System.out.println(selected.toString());
                return true;
            }
        });
    }

    public void done (View v) {
        send();
    }

    public void clear (View v) {
        selected.clear();
        getListView().clearChoices();
        findViewById(R.id.button_done).setVisibility(View.GONE);
        findViewById(R.id.button_clear).setVisibility(View.GONE);
        int n = getListView().getChildCount();
        for (int i = 0; i < n; i ++) {
            //getListView().getChildAt(i);
            getListView().getChildAt(i).setBackgroundColor(ContextCompat.getColor(getApplication(), R.color.colorPrimaryLight));
        }
    }

    public void send() {
        new AlertDialog.Builder(ListFilesActivity.this)
                .setTitle("Choose Action")
                .setMessage("Send these audios or convert them")
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new AlertDialog.Builder(ListFilesActivity.this)
                                .setTitle("Choose Way")
                                .setMessage("How do you want to send your files?")
                                .setPositiveButton("Bluetooth", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        ArrayList<Uri> uris = new ArrayList<Uri>();
                                        Intent bluetoothIntent = new Intent();
                                        bluetoothIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                                        bluetoothIntent.setType("audio/*");
                                        bluetoothIntent.setPackage("com.android.bluetooth");
                                        for (String fileStr : selected) {
                                            File fileAudio = new File(fileStr);
                                            uris.add(Uri.fromFile(fileAudio));
                                        }
                                        bluetoothIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                                        try {
                                            startActivity(bluetoothIntent);
                                        } catch (ActivityNotFoundException e) {
                                            e.printStackTrace();
                                            Toast.makeText(getApplicationContext(), "No Bluetooth on this device",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .setNegativeButton("Send Via...", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        ArrayList<Uri> uris = new ArrayList<>();
                                        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                                        emailIntent.setType("audio/*");
                                        for (String fileStr : selected) {
                                            File fileAudio = new File(fileStr);
                                            uris.add(Uri.fromFile(fileAudio));
                                        }
                                        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                                        try {
                                            startActivity(emailIntent);
//                                            startActivity(Intent.createChooser(emailIntent,"Send To..."));
                                        } catch (ActivityNotFoundException e) {
                                            e.printStackTrace();
                                            Toast.makeText(getApplicationContext(), "No email client found",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .setIcon(R.mipmap.fmexplorermine)
                                .show();
                    }
                })
                .setNegativeButton("Convert", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isAppInstalled("com.nchsoftware.switchand_free")) {
                            new AlertDialog.Builder(ListFilesActivity.this)
                                    .setTitle("App missing")
                                    .setMessage("You need to download Switch Free App to continue with this action")
                                    .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                                            marketLaunch.setData(Uri.parse("market://search?q=nch software " + AUDIO_CONVERTER_APP));
                                            startActivity(marketLaunch);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //get me out of here
                                        }
                                    })
                                    .setIcon(R.mipmap.fmexplorermine)
                                    .show();
                        } else {
                            try {
                                Intent convertAudio = getPackageManager().getLaunchIntentForPackage("com.nchsoftware.switchand_free");
                                startActivity(convertAudio);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .setIcon(R.mipmap.fmexplorermine)
                .show();
    }

    private boolean isAppInstalled(String packageName){
        PackageManager pm = getPackageManager();
        boolean installed;
        try{
            pm.getPackageInfo(packageName,PackageManager.GET_ACTIVITIES);
            installed = true;
        }catch(PackageManager.NameNotFoundException e){
            installed = false;
        }
        return installed;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
        if (multiple){
            Toast.makeText(getApplicationContext(),"Hold item to add to add to selection.",Toast.LENGTH_SHORT).show();
        } else {
            filename = (String) getListAdapter().getItem(position);
            if (path.endsWith(File.separator)) {
                filename = path + filename;
            } else {
                filename = path + File.separator + filename;
            }
            if (new File(filename).isDirectory()) {
                Intent intent = new Intent(this, ListFilesActivity.class);
                intent.putExtra("path", filename);
                startActivity(intent);

            } else {
                new AlertDialog.Builder(ListFilesActivity.this)
                        .setTitle("Choose Action")
                        .setMessage("Send this audio or convert it")
                        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new AlertDialog.Builder(ListFilesActivity.this)
                                        .setTitle("Choose Way")
                                        .setMessage("How do you want to send your files?")
                                        .setPositiveButton("Bluetooth", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent bluetoothIntent = new Intent();
                                                bluetoothIntent.setAction(Intent.ACTION_SEND);
                                                bluetoothIntent.setType("audio/*");
                                                bluetoothIntent.setPackage("com.android.bluetooth");
                                                File fileAudio = new File(filename);
                                                bluetoothIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileAudio));
                                                try {
                                                    startActivity(bluetoothIntent);
                                                } catch (ActivityNotFoundException e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(getApplicationContext(), "No Bluetooth on this device",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        })
                                        .setNegativeButton("Email", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
//                                                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
                                                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                                emailIntent.setType("audio/*");
                                                File fileAudio = new File(filename);
                                                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileAudio));
                                                try {
                                                    startActivity(emailIntent);
//                                                    startActivity(Intent.createChooser(emailIntent,"Send To..."));
                                                } catch (ActivityNotFoundException e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(getApplicationContext(), "No email client found",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        })
                                        .setIcon(R.mipmap.fmexplorermine)
                                        .show();
                            }
                        })
                        .setNegativeButton("Convert", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!isAppInstalled("com.nchsoftware.switchand_free")) {
                                    new AlertDialog.Builder(ListFilesActivity.this)
                                            .setTitle("App missing")
                                            .setMessage("You need to download Switch Free App to continue with this action")
                                            .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                                                    marketLaunch.setData(Uri.parse("market://search?q=nch software " + AUDIO_CONVERTER_APP));
                                                    startActivity(marketLaunch);
                                                }
                                            })
                                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //get me out of here
                                                }
                                            })
                                            .setIcon(R.mipmap.fmexplorermine)
                                            .show();
                                } else {
                                    try {
                                        Intent convertAudio = getPackageManager().getLaunchIntentForPackage("com.nchsoftware.switchand_free");
                                        startActivity(convertAudio);
                                    } catch (ActivityNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        })
                        .setIcon(R.mipmap.fmexplorermine)
                        .show();
            }
        }
    }
}
