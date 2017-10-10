package com.cit.ids.utilities;

import java.util.HashMap;
import java.util.Locale;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;

@SuppressWarnings("deprecation")
public class TTSHandler extends Service implements 
			OnInitListener, OnUtteranceCompletedListener {
    
    /*Global private variables declaration & initialization*/
	private TextToSpeech vTextToSpeech = null;
    private Boolean vIsStopTTS = false;
    private Boolean vIsLocaleDataAvailable = false;
    private Boolean vIsConnected = false;
    private Boolean vIsStopOnConnected = false;
    
    private String vInitialMessageQueue = null;
    private String vFinalMessageQueue = null;
    
    private HashMap<String, String> vHashMap = null;
    
    /*without initial & final message. can set separately if required*/
    public TTSHandler(Context vContext) {
    	Log.d("TTS", "Constructer with no intial & final message");
    	vIsStopOnConnected = false;
    	initializeTTSEngine(vContext, "", "");
	}
    
    /*with only one message. stop service after speaking this single message*/
    public TTSHandler(Context vContext, String vMessage) {
    	Log.d("TTS", "Constructer with single message");
    	vIsStopOnConnected = true;
    	initializeTTSEngine(vContext, "", vMessage);
	}
    
    /*with initial & final message*/
    public TTSHandler(Context vContext, 
    		String vInitialMessage, String vFinalMessage) {
    	
    	Log.d("TTS", "Constructer with intial & final message");
    	vIsStopOnConnected = false;
    	initializeTTSEngine(vContext, vInitialMessage, vFinalMessage);
	}
    
    /*initializing TTS engine, initial and final message queue*/
    private void initializeTTSEngine(Context vContext,
    			String vInitialMessage, String vFinalMessage) {
    	/*initializing TTS engine and Hash Map*/
    	vTextToSpeech = new TextToSpeech(vContext, this);
    	vHashMap = new HashMap<String, String>();
    	vHashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "LastSpeech");
    	
    	/*initializing initial and final message queue*/
    	vInitialMessageQueue = vInitialMessage;
    	vFinalMessageQueue = vFinalMessage;
    }
    
    /*adding the message to TTS engine queue to be speak*/
    public void speakIt(String vTextToSpeak) {
		Log.d("TTS", "Speaking");
   		if(vIsConnected) {
   			if(vIsStopTTS) {
    			vTextToSpeech.speak(vTextToSpeak, TextToSpeech.QUEUE_FLUSH, vHashMap);
    		} else {
   				if(vTextToSpeech.isSpeaking()) {
   					vTextToSpeech.speak(vTextToSpeak, TextToSpeech.QUEUE_ADD, null);
   				} else {
    				vTextToSpeech.speak(vTextToSpeak, TextToSpeech.QUEUE_FLUSH, null);
    			}
    		}
    	} else {
    		vInitialMessageQueue += vTextToSpeak;
    	}
    }
    
    /*setting initial message to speak just after connecting to TTS engine*/
    public void setInitialMessage(String vInitialMessage) {
    	vInitialMessageQueue = vInitialMessage;
    }
    
    /*setting final message to speak just before disconnecting from TTS engine*/
    public void setFinalMessage(String vFinalMessage) {
    	vFinalMessageQueue = vFinalMessage;
    }
    
    /*return true if TTS data available for user default Locale*/
    public boolean isLocaleDataAvailable() {
    	return vIsLocaleDataAvailable;
    }
    
    /*return true if TTS service successfully acquired*/
    public boolean isConnected() {
    	return vIsConnected;
    }
    
    /*explicit call to stop TTS engine*/
    public void stopTTSHandler() {
    	if(vIsConnected) {
    		Log.d("TTS", "stoping TTS");
    		vIsStopTTS = true;
    		/*sending final message to TTS engine*/
    		speakIt(vFinalMessageQueue);
    	}
    }
    
    /*called after getting the request result from TTS engine*/
    @Override
    public void onInit(int status) {
    	if (status == TextToSpeech.SUCCESS) {
        	Log.d("TTS", "TTS SUCCESS");
        	vIsConnected = true;
        	if (vTextToSpeech.isLanguageAvailable(Locale.getDefault()) >= 0) {
        		Log.d("TTS", "Default locale data available");
        		vIsLocaleDataAvailable = true;
	        	vTextToSpeech.setLanguage(Locale.getDefault());
	        	
	        	/*adding listener*/
	        	vTextToSpeech.setOnUtteranceCompletedListener(this);
	        	
	        	/*sending initial message to TTS engine*/
	        	speakIt(vInitialMessageQueue);
	        	
	        	/*implicitly terminating TTS service if vIsStopOnConnected is true*/
	        	if(vIsStopOnConnected) {
	        		this.stopTTSHandler();
	        	}
        	} else {
	        	Log.d("TTS", "Locale data not available");
	        	vIsLocaleDataAvailable = false;
	        }
	    } else if (status == TextToSpeech.ERROR) {
	    	Log.d("TTS", "TTS ERROR");
	    	vIsConnected = false;
	    }
    }
    
    /*called when TTS engine stop speaking*/
    @Override
	public void onUtteranceCompleted(String utteranceId) {
		Log.d("TTS", "Speaking Done");
		/*matching if completed speech was final message or not*/
		if(utteranceId.equals("LastSpeech")) {
			Log.d("TTS", "HashMap Matched");
			this.onDestroy();
		}
	}
    
    /*called when destroying service*/
    @Override
    public void onDestroy() {
        Log.d("TTS", "OnDestroy");
        if (vTextToSpeech != null) {
            vTextToSpeech.stop();
            vTextToSpeech.shutdown();
            vIsConnected = false;
        }
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent vIntent) {
        return null;
    }
}