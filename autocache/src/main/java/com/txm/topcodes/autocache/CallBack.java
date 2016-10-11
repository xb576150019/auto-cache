package com.txm.topcodes.autocache;

/**
 * CallBack
 *
 * @author TXM  2016/9/27.
 *         回调函数接口
 */
public interface CallBack {
	void onStart();

	void save2DataBase(String cachePath);

	void onSuccess(byte[] bytes);

	void onErro(Throwable e);
}
