package com.jbro129.terrariastub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
	
	private static final String statusText = "Terraria Environment Status: ";
	private static final String TAG = "JbroLog";
	private File externalFilesDir;
	private TextView status;
	private TextView info;
	private Button wipe;
	private Button recreate;
	private Button importFile;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		status = findViewById(R.id.envstatus);
		info = findViewById(R.id.info);
		wipe = findViewById(R.id.wipe);
		recreate = findViewById(R.id.recreate);
		importFile = findViewById(R.id.importFile);
		
		wipe.setOnClickListener(v -> {
			Log.d(TAG, "Wiping environment");
			wipe();
		});
		recreate.setOnClickListener(v -> {
			Log.d(TAG, "ReCreating environment");
			wipe();
			initEnvironment();
		});
		importFile.setOnClickListener(v -> {
			File downloads = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
					Environment.DIRECTORY_DOWNLOADS + "/");
			
			Log.d(TAG, "Importing File from " + downloads.getAbsolutePath());
			
			ScrollView scrollView = new ScrollView(MainActivity.this);
			scrollView.setScrollBarSize(5);
			
			LinearLayout files = new LinearLayout(MainActivity.this);
			files.setOrientation(LinearLayout.VERTICAL);
			files.setGravity(Gravity.CENTER);
			
			scrollView.addView(files);
			
			AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
			final AlertDialog alert = build.create();
			alert.setView(scrollView);
			
			alert.show();
			
			ArrayList<File> filesList = getWorldsAndPlayers(downloads.getAbsolutePath());
			
			if (filesList.isEmpty())
			{
				Log.d(TAG, "No players or world found");
			}
			
			for (File file : filesList)
			{
				boolean world = file.getAbsolutePath().contains(".wld");
				
				addButtonLayout(MainActivity.this, files, file, v1 -> {
					File dest = (world)
							?
							new File(externalFilesDir.getAbsolutePath() + "/Worlds/" + file.getName())
							:
							new File(externalFilesDir.getAbsolutePath() + "/Players/" + file.getName());
					
					if (copyFile(file, dest))
					{
						Log.d(TAG, "Imported to " + dest.getAbsolutePath());
						Toast.makeText(MainActivity.this, "Imported " + file.getName(), Toast.LENGTH_SHORT).show();
					}
					else
					{
						Log.e(TAG, "Failed to import " + file.getAbsolutePath() + " to " + dest.getAbsolutePath());
						Toast.makeText(MainActivity.this, "Failed... Check logs", Toast.LENGTH_SHORT).show();
					}
					alert.dismiss();
				});
			}
		});
		
		externalFilesDir = this.getExternalFilesDir(null).getParentFile();
		Log.d(TAG, "external files dir: " + externalFilesDir.getAbsolutePath());
		
		info.setText("Env Info: " + BuildConfig.APPLICATION_ID + " " + BuildConfig.VERSION_NAME + " " + BuildConfig.VERSION_CODE);
		
		if (ContextCompat.checkSelfPermission(MainActivity.this,
				Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			Log.d(TAG, "read write access not granted or not requested");
			ActivityCompat.requestPermissions(MainActivity.this,
					new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
					123);
		} else {
			Log.d(TAG, "read write access granted");
		}
		
		initEnvironment();
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		
		if (requestCode == 123) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Log.d(TAG, "storage permission granted");
			} else {
				Log.d(TAG, "storage permission not granted");
			}
		}
	}
	
	private void wipe()
	{
		Log.d(TAG, "Data path to wipe: " + externalFilesDir.getAbsolutePath());
		
		deleteDir(externalFilesDir);
	}
	
	private void initEnvironment()
	{
		boolean anyerrors = false;
		
		status.setText(statusText + "Preparing...");
		
		File controllerProfilesDir = new File(externalFilesDir.getAbsolutePath() + "/ControllerProfiles");
		File filesDir = new File(externalFilesDir.getAbsolutePath() + "/files");
		File oldSavesDir = new File(externalFilesDir.getAbsolutePath() + "/OldSaves");
		File oldSavesPlayersDir = new File(externalFilesDir.getAbsolutePath() + "/OldSaves/Players");
		File oldSavesWorldsDir = new File(externalFilesDir.getAbsolutePath() + "/OldSaves/Worlds");
		File playersDir = new File(externalFilesDir.getAbsolutePath() + "/Players");
		File worldsDir = new File(externalFilesDir.getAbsolutePath() + "/Worlds");
		
		File achievementFile = new File(externalFilesDir.getAbsolutePath() + "/achievements.dat");
		File configFile = new File(externalFilesDir.getAbsolutePath() + "/config.json");
		File favoritesFile = new File(externalFilesDir.getAbsolutePath() + "/favorites.json");
		File virtualControlsFile = new File(externalFilesDir.getAbsolutePath() + "/VirtualControls.json");
		File dummyWorldFile = new File(externalFilesDir.getAbsolutePath() + "/Worlds/dummy.wld");
		File dummyPlayerFile = new File(externalFilesDir.getAbsolutePath() + "/Players/dummy.plr");
		
		ArrayList<File> folders = new ArrayList<>();
		ArrayList<File> files = new ArrayList<>();
		
		folders.add(controllerProfilesDir);
		folders.add(filesDir);
		folders.add(oldSavesDir);
		folders.add(oldSavesPlayersDir);
		folders.add(oldSavesWorldsDir);
		folders.add(playersDir);
		folders.add(worldsDir);
		
		files.add(achievementFile);
		files.add(configFile);
		files.add(favoritesFile);
		files.add(virtualControlsFile);
		// files.add(dummyWorldFile);
		// files.add(dummyPlayerFile);
		
		for (File folder : folders)
		{
			if (!folder.exists())
			{
				if (folder.mkdirs())
				{
					Log.d(TAG, "Created folder " + folder.getAbsolutePath());
				}
				else
				{
					anyerrors = true;
					Log.d(TAG, "Unable to create folder " + folder.getAbsolutePath());
				}
			}
			else
			{
				Log.d(TAG, "Already created folder " + folder.getAbsolutePath());
			}
		}
		
		for (File file : files)
		{
			if (!file.exists())
			{
				if (createDummyFile(file))
				{
					Log.d(TAG, "Created file " + file.getAbsolutePath());
				}
				else
				{
					anyerrors = true;
					Log.d(TAG, "Unable to create file " + file.getAbsolutePath());
				}
			}
			else
			{
				Log.d(TAG, "Already created file " + file.getAbsolutePath());
			}
		}
		
		if (anyerrors)
		{
			status.setText(statusText + "Failed... Check logs");
			Log.d(TAG, "Create env failed");
		}
		else
		{
			status.setText(statusText + "Created!");
			Log.d(TAG, "Create env pass");
		}
		
	}
	
	public ArrayList<File> getWorldsAndPlayers(String path)
	{
		ArrayList<File> list = new ArrayList<>();
		
		File[] files = new File(path).listFiles((dir, name) -> name.contains(".wld") || name.contains(".plr"));
		
		if (files != null && files.length != 0)
		{
			Collections.addAll(list, files);
		}
		
		return list;
	}
	
	@SuppressLint("NewApi")
	public static void addButtonLayout(Context ctx, LinearLayout layout, File file, View.OnClickListener listener)
	{
		Button button = new Button(ctx);
		button.setText(file.getName());
		button.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
		button.setAllCaps(false);
		button.setTextColor(Color.BLACK);
		//button.setTextSize(16.0F);
		button.setGravity(Gravity.CENTER_HORIZONTAL);
		button.setOnClickListener(listener);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_VERTICAL;
		
		params.setMargins(10,15,10,15);
		
		button.setLayoutParams(params);
		
		layout.addView(button);
	}
	
	public static boolean copyFile(File source, File dest) {
		try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(dest)) {
			
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			
			return false;
		}
		return dest.exists();
	}
	
	public boolean createDummyFile(File path)
	{
		File root = path.getParentFile();
		
		if (!root.exists()) root.mkdirs();
		
		try {
			FileWriter writer = new FileWriter(path, true);
			writer.append("Dummy file");
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			return path.exists();
		}
	}
	
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
}