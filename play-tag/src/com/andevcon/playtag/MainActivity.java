package com.andevcon.playtag;

import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.CheckBox;

import com.andevcon.playtag.util.HexUtil;

public class MainActivity extends Activity {

	private static final String DEBUG_MAIN_ACTIVITY = "MainActivity";
	private NfcAdapter nfcAdapter;
	private PendingIntent nfcPendingIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Grab the NFC Adapter found on the phone.
		// TODO: If returns null, NFC is disabled or not available
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);

		// Setup a PendingIntent to trigger when an NFC scan occurs
		Intent intent = new Intent(this, getClass());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String tagId = HexUtil.bytesToHex(tag.getId());
			Log.i(DEBUG_MAIN_ACTIVITY,
					String.format("Found NFC Tag with serial number %s", tagId));
			onNfcTagDiscovered(tag, intent);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (null != nfcAdapter) {
			nfcAdapter.disableForegroundDispatch(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (null != nfcAdapter) {
			nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, null,
					null);
		}
	}

	private void onNfcTagDiscovered(Tag tag, Intent intent) {
		CheckBox cbox = (CheckBox) findViewById(R.id.checkBoxWrite);
		if (cbox.isChecked()) {
			writeDataToTag(tag);
		}
	}

	private void writeDataToTag(Tag tag) {
		String msg = "Hello Tag";
		NdefRecord r = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_TEXT, null, msg.getBytes());
		NdefMessage m = new NdefMessage(new NdefRecord[] { r });

		Ndef ndef = Ndef.get(tag);
		try {
			if (null != ndef) {
				ndef.connect();
				ndef.writeNdefMessage(m);
				ndef.close();
			} else {
				NdefFormatable ndefFormat = NdefFormatable.get(tag);
				ndefFormat.connect();
				ndefFormat.format(m);
				ndefFormat.close();
			}

			Log.i(DEBUG_MAIN_ACTIVITY, "Text successfully written to tag");

		} catch (IOException e) {
			Log.e(DEBUG_MAIN_ACTIVITY, e.getMessage());
		} catch (FormatException e) {
			Log.e(DEBUG_MAIN_ACTIVITY, "Invalid Format");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
