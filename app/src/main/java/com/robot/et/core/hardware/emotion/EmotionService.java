package com.robot.et.core.hardware.emotion;

import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.common.EmotionConfig;
import com.robot.et.util.ScriptManager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class EmotionService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		int flag = Emotion.init();
		if (flag != 0) {
			Log.i("Emotion", "表情初始化失败");
			return;
		}
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(BroadcastAction.ACTION_CONTROL_ROBOT_EMOTION);
		registerReceiver(receiver, filter);
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {

		private int emotion;

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BroadcastAction.ACTION_CONTROL_ROBOT_EMOTION)) {
				emotion = intent.getIntExtra("emotion",EmotionConfig.ROBOT_EMOTION_NORMAL);// 获取表情
				Log.i("Emotion", "Emotion:"+emotion);
				doEmotion(emotion);
				if(DataConfig.isPlayScript){
					ScriptManager.setNewScriptInfos(ScriptManager.getScriptActionInfos(),true,2000);
				}
			}
		}
	};

	private void doEmotion(int emotion) {
		switch (emotion) {
		case EmotionConfig.ROBOT_EMOTION_NORMAL:
			Emotion.setEmotion(0x6001);
			break;
		case EmotionConfig.ROBOT_EMOTION_PLEASURE:
			Emotion.setEmotion(0x6002);
			break;
		case EmotionConfig.ROBOT_EMOTION_ANGER:
			Emotion.setEmotion(0x6003);
			break;
		case EmotionConfig.ROBOT_EMOTION_SAD:
			Emotion.setEmotion(0x6004);
			break;
		case EmotionConfig.ROBOT_EMOTION_HAPPY:
			Emotion.setEmotion(0x6005);
			break;
		case EmotionConfig.ROBOT_EMOTION_SEE_LEFT:
				Emotion.setEmotion(0x6006);
				break;
		case EmotionConfig.ROBOT_EMOTION_SEE_RIGHT:
				Emotion.setEmotion(0x6007);
				break;
		case EmotionConfig.ROBOT_EMOTION_SLEEP:
			Emotion.setEmotion(0x6008);
			break;
		default:
			break;
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(receiver != null){
			unregisterReceiver(receiver);
		}
	}
}
