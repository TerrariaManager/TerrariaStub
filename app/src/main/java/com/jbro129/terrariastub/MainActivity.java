package com.jbro129.terrariastub;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
	
	private static final String statusText = "Terraria Environment Status: ";
	private static final String TAG = "JbroLog";
	private File externalFilesDir;
	private TextView status;
	private TextView info;
	private Button wipe;
	private Button recreate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		status = findViewById(R.id.envstatus);
		info = findViewById(R.id.info);
		wipe = findViewById(R.id.wipe);
		recreate = findViewById(R.id.recreate);
		
		wipe.setOnClickListener(v -> {
			Log.d(TAG, "Wiping environment");
			wipe();
		});
		recreate.setOnClickListener(v -> {
			Log.d(TAG, "ReCreating environment");
			wipe();
			initEnvironment();
		});
		
		externalFilesDir = this.getExternalFilesDir(null).getParentFile();
		
		info.setText("Env Info: " + BuildConfig.APPLICATION_ID + " " + BuildConfig.VERSION_NAME + " " + BuildConfig.VERSION_CODE);
		
		initEnvironment();
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
		
		Log.d(TAG, "external files dir: " + externalFilesDir.getAbsolutePath());
		
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
		files.add(dummyWorldFile);
		files.add(dummyPlayerFile);
		
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