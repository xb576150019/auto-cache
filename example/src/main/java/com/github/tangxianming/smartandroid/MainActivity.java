package com.github.tangxianming.smartandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.txm.topcodes.smartcache.CallBack;
import com.txm.topcodes.smartcache.SmartCache;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SmartCache.MediaCache.getInstance(this).asyncCache("http://lcell.bnu.edu.cn/v2/public/photo/2016/9/120_145b0f67-23da-45ea-8371-e6251c7c14f5.JPEG", new CallBack() {
			@Override
			public void onStart() {
				//Run in UI thread
			}

			@Override
			public void save2DataBase(String cachePath) {
				//Run in io thread
				//you can sava the ca cachPath to database or sharepreference…….
			}

			@Override
			public void onSuccess(byte[] bytes) {
				//Run in UI thread
				Log.d("bytes", String.valueOf(bytes.length));
			}

			@Override
			public void onErro(Throwable e) {
				//Run in UI thread
			}
		}, SmartCache.DIRECTORY_PICTURES, "filename");

	}
}
