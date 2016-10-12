package com.txm.topcodes.autocache;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;


import com.txm.topcodes.autocache.util.SDCardUtils;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * AutoCache
 *
 * @author txm 2016/10/8.
 *         This is a entry class,contains Media and MediaCache two important internal class.
 */
public class AutoCache {
	/**
	 * Define cache file type.
	 */
	public static String DIRECTORY_PODCASTS = "Podcasts";
	public static String DIRECTORY_PICTURES = "Pictures";
	public static String DIRECTORY_MOVIES = "Movies";
	public static String DIRECTORY_DOWNLOADS = "Download";
	public static String DIRECTORY_MUSIC = "Music";
	public static String DIRECTORY_DOCUMENTS = "Documents";

	/**
	 * Contains some operations on the local stream file.
	 */
	public static class Media {

		public static byte[] getFile(String fileDir) {
			return SDCardUtils.loadFileFromSDCard(fileDir);
		}

		public static Bitmap getSoftBmp(String fileDir) {
			Bitmap bmp = SDCardUtils.loadBitmapFromSDCard(fileDir);
			SoftReference<Bitmap> bitmapSoftReference = new SoftReference<Bitmap>(bmp);
			return bitmapSoftReference.get();
		}

		public static Bitmap getBmp(String fileDir) {
			Bitmap bmp = SDCardUtils.loadBitmapFromSDCard(fileDir);
			SoftReference<Bitmap> bitmapSoftReference = new SoftReference<Bitmap>(bmp);
			return bitmapSoftReference.get();
		}

		public static Bitmap getWeakBmp(String fileDir) {
			Bitmap bmp = SDCardUtils.loadBitmapFromSDCard(fileDir);
			WeakReference<Bitmap> bitmapWeakReference = new WeakReference<Bitmap>(bmp);
			return bitmapWeakReference.get();
		}
	}

	/**
	 * This is an important class,you can async-access and async-cache network files through it.
	 */
	public static class MediaCache implements Cache {

		private static MediaCache cacheInstance;

		private Context context;

		private String cachePath;

		private List<Subscription> subscriptions = new ArrayList<>();

		public MediaCache(Context context) {
			this.context = context;
		}

		public synchronized static MediaCache getInstance(Context context) {
			if (null == cacheInstance) {
				cacheInstance = new MediaCache(context);
			}
			return cacheInstance;
		}


		/**
		 * Async-access network files and automatic save it to local SDCard.
		 * @param url
		 * @param callBack
		 * @param dataType
		 * @param fileName
		 */
		public void asyncCache(final String url, final CallBack callBack, final String dataType, final String fileName) {
			cachePath = SDCardUtils.getSDCardPrivateFileDir(context, dataType, fileName);
			Subscription subscription = Observable.create(new Observable.OnSubscribe<byte[]>() {
				@Override
				public void call(final Subscriber<? super byte[]> subscriber) {
					OkHttpClient client = new OkHttpClient();
					final Request request = new Request.Builder().url(url).build();
					client.newCall(request).enqueue(new Callback() {
						@Override
						public void onFailure(Call call, IOException e) {
							subscriber.onError(e);
						}

						@Override
						public void onResponse(Call call, Response response) throws IOException {
							if (response.code() == HttpURLConnection.HTTP_OK) {
								byte[] bytes = response.body().bytes();
								subscriber.onNext(bytes);
								subscriber.onCompleted();
							} else {
								subscriber.onError(new Exception(String.valueOf(response.code())));
							}
						}
					});
				}
			})
					.subscribeOn(Schedulers.io())
					.doOnSubscribe(new Action0() {
						@Override
						public void call() {
							callBack.onStart();
						}
					})
					.subscribeOn(AndroidSchedulers.mainThread())
					.doOnNext(new Action1<byte[]>() {
						@Override
						public void call(final byte[] bytes) {
							Schedulers.io().createWorker().schedule(new Action0() {
								@Override
								public void call() {
									Boolean isSaved = SDCardUtils.saveFileToSDCardPrivateFilesDir(bytes, dataType, fileName, context);
								}
							});
						}
					})
					.doOnNext(new Action1<byte[]>() {
						@Override
						public void call(byte[] bytes) {
							Schedulers.io().createWorker().schedule(new Action0() {
								@Override
								public void call() {
									callBack.save2DataBase(cachePath);
								}
							});
						}
					})
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(new Subscriber<byte[]>() {
						@Override
						public void onCompleted() {
							unsubscribe();
						}

						@Override
						public void onError(Throwable e) {
							Log.e("autoCacheError", e.getMessage());
							callBack.onErro(e);
						}

						@Override
						public void onNext(byte[] bytes) {
							callBack.onSuccess(bytes);
						}
					});
			subscriptions.add(subscription);
		}

		/**
		 * Get cache path based on file name and file type.
		 * @param context
		 * @param dataType
		 * @param fileName
		 * @return
		 */
		public String findCachePath(Context context, String dataType, String fileName) {
			return SDCardUtils.getSDCardPrivateFileDir(context, dataType, fileName);
		}

		/**
		 * Cache a known media file.
		 * @param bytes
		 * @param dataType
		 * @param fileName
		 * @return
		 */
		public boolean cache(byte[] bytes, String dataType, String fileName) {
			return SDCardUtils.saveFileToSDCardPrivateFilesDir(bytes, dataType, fileName, context);
		}

		/**
		 * It is not necessary to use this function.Because RxJava can Auto unsubscribe your subscriptions when program executes onCompleted function.
		 */
		@Override
		public void close() {
			for (Subscription subscription : subscriptions) {
				if (!subscription.isUnsubscribed()) {
					subscription.unsubscribe();
				}
			}
			subscriptions.clear();
		}
	}
}