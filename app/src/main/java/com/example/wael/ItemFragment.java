package com.example.wael;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

public class ItemFragment extends Fragment {

    private TextToSpeech tts;
    private boolean ttsReady = false;
    private String pendingSpeak = null;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable retrySpeakRunnable = new Runnable() {
        @Override
        public void run() {
            if (ttsReady && tts != null && pendingSpeak != null) {
                doSpeak(pendingSpeak);
                pendingSpeak = null;
            }
        }
    };

    private static final String[][] NIKUD_DATA = {
            {"\u05B7", "פַּתַח"},   // Patach
            {"\u05B8", "קָמָץ"},   // Kamatz
            {"\u05B6", "סֶגוֹל"},   // Segol
            {"\u05B5", "צֵירֵי"},   // Tsere
            {"\u05B4", "חִירִיק"},  // Hiriq
            {"\u05B9", "חוֹלָם"},   // Holam
            {"\u05BB", "קֻבּוּץ"},   // Kubutz
            {"\u05B0", "שְׁוָא"},    // Shva
            {"\u05B2", "חָטָף פַּתַח"}, // Hataf Patach
            {"\u05B1", "חָטָף סֶגוֹל"}, // Hataf Segol
            {"\u05B3", "חָטָף קָמָץ"},  // Hataf Kamatz
    };

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_item, container, false);

        initTts();
        setupLetterCardsClick(view);

        return view;
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacks(retrySpeakRunnable);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        ttsReady = false;
        pendingSpeak = null;
        super.onDestroyView();
    }

    private void initTts() {
        Activity activity = getActivity();
        if (activity == null) return;
        tts = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.SUCCESS || tts == null) return;
                Locale hebrew = new Locale("he");
                int result = tts.setLanguage(hebrew);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.getDefault());
                    if (getContext() != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "لتثبيت صوت العبرية: الإعدادات ← اللغة ← TTS", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                ttsReady = true;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (pendingSpeak != null) {
                            doSpeak(pendingSpeak);
                            pendingSpeak = null;
                        }
                    }
                }, 150);
            }
        });
    }

    private void doSpeak(String text) {
        if (text == null || text.isEmpty() || tts == null) return;
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void speakHebrew(String text) {
        if (text == null || text.isEmpty()) return;
        if (tts != null && ttsReady) {
            doSpeak(text);
        } else {
            pendingSpeak = text;
            handler.removeCallbacks(retrySpeakRunnable);
            handler.postDelayed(retrySpeakRunnable, 600);
        }
    }

    private void setupLetterCardsClick(View root) {
        if (!(root instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) root;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof MaterialCardView) {
                setLetterCardClick((MaterialCardView) child);
            } else if (child instanceof ViewGroup) {
                setupLetterCardsClick(child);
            }
        }
    }

    private void setLetterCardClick(MaterialCardView card) {
        TextView letterView = null;
        if (card.getChildCount() > 0 && card.getChildAt(0) instanceof TextView) {
            letterView = (TextView) card.getChildAt(0);
        }
        if (letterView == null) return;

        final String letter = letterView.getText().toString().trim();
        if (letter.isEmpty()) return;

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakHebrew(letter);
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                Point p = new Point();
                p.x = location[0];
                p.y = location[1];
                showNikudPopup(getActivity(), p, letter);
            }
        });
    }

    private void showNikudPopup(Activity activity, Point anchorPoint, String letter) {
        if (activity == null) return;

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_nikud, null);

        int width = (int) (320 * activity.getResources().getDisplayMetrics().density);
        PopupWindow popup = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popup.setOutsideTouchable(true);

        TextView title = popupView.findViewById(R.id.popup_title);
        title.setText("הניקוד ל־" + letter);

        LinearLayout list = popupView.findViewById(R.id.nikud_list);
        list.removeAllViews();

        for (String[] nikud : NIKUD_DATA) {
            String combiningChar = nikud[0];
            String nameHebrew = nikud[1];
            final String letterWithNikud = letter + combiningChar;

            View row = inflater.inflate(R.layout.item_nikud_row, list, false);
            TextView letterText = row.findViewById(R.id.nikud_letter);
            TextView nameText = row.findViewById(R.id.nikud_name);

            letterText.setText(letterWithNikud);
            nameText.setText(nameHebrew);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    speakHebrew(letterWithNikud);
                }
            });

            list.addView(row);
        }

        Button closeBtn = popupView.findViewById(R.id.popup_close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });

        View anchor = activity.getWindow().getDecorView();
        int screenW = activity.getResources().getDisplayMetrics().widthPixels;
        int x = (screenW - width) / 2;
        int y = (int) (100 * activity.getResources().getDisplayMetrics().density);

        popup.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y);
    }
}
