package dev.splat.colorchecker;

import static dev.splat.colorchecker.Utils.netIsAvailable;
import static dev.splat.colorchecker.Utils.rgbToLab;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.GetUpdatesResponse;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.IntStream;


import dev.splat.colorchecker.adapters.ResultsViewAdapter;
import dev.splat.colorchecker.databinding.ActivityMainBinding;
import dev.splat.colorchecker.models.Result;

public class MainActivity extends CameraActivity {
    
    static {
        System.loadLibrary("colorchecker");
    }
    private static final String LOGTAG = "[OPENCV] ";
    private int current_page_idx = 0;
    private ActivityMainBinding binding;
    private JavaCameraView mOpenCvCameraView;
    private boolean camera_stop = true;
    private boolean is_flashlight = false;
    private TelegramBot bot;

    private final double[][] train_data = new double[][] {
            {1, 1, 1, 10, 10, 10, 20, 20, 20, 30, 30, 30},
            {100, 100, 100, 200, 200, 200, 300, 300, 300, 400, 400, 400}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bot = new TelegramBot(getString(R.string.BOT_TOKEN));

        if (!OpenCVLoader.initDebug()) {
            Log.d(LOGTAG, "OpenCV inited");
        }

        mOpenCvCameraView = binding.opencvCameraView;
        mOpenCvCameraView.setVisibility(View.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(cvCameraViewListener);

        binding.buttonFlashlight.setOnClickListener(v -> {
            is_flashlight = !is_flashlight;
            mOpenCvCameraView.set_flashlight(is_flashlight);
        });

        int flatten[] = new int[train_data.length * train_data[0].length];
        for (int i = 0; i < train_data.length; i++) {
            for (int j = 0; j < train_data[i].length; j++) {
                flatten[i * train_data[0].length + j] = (int) train_data[i][j];
            }
        }
        fit(flatten, train_data.length, train_data[0].length);

        binding.actionButton.setOnClickListener(v -> {
            if (current_page_idx == 0) {
                binding.layoutInstructions.setVisibility(View.GONE);
                mOpenCvCameraView.setVisibility(View.VISIBLE);
                binding.layoutCamera.setVisibility(View.VISIBLE);

                binding.actionButton.setVisibility(View.INVISIBLE);
                camera_stop = false;
                current_page_idx = 1;
            } else if (current_page_idx == 1) {
                mOpenCvCameraView.setVisibility(View.INVISIBLE);
                binding.layoutCamera.setVisibility(View.GONE);
                binding.layoutResults.setVisibility(View.VISIBLE);

                binding.actionButton.setVisibility(View.VISIBLE);
                binding.actionButton.setText("Пройти тест заново");
                current_page_idx = 2;
            }
            else if (current_page_idx == 2) {
                binding.layoutResults.setVisibility(View.GONE);
                binding.layoutInstructions.setVisibility(View.VISIBLE);

                current_page_idx = 0;
                binding.actionButton.setText("Начать тестирование");
            }
        });
    }

    @Override
    protected List<?extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }
    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.v(LOGTAG, "OpenCV loaded");
                mOpenCvCameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };
    private void process_frame(Mat image) {
        if (current_page_idx != 1 || camera_stop)
            return;
        double features[] = rateimage(image.getNativeObjAddr());
        if (features == null) {
            binding.cameraMessage.setText("Ошибка");
            return;
        }

        if (features[0] < 0.5 || features[1] < 0.5 || features[2] < 0.5) {
            // bad image
            binding.cameraMessage.setText("Низкое качество изображения");
            return;
        }
        Mat card = new Mat();
        int qr = gettest(image.getNativeObjAddr(), card.getNativeObjAddr());
        if (qr != 0)
        {
            if (qr == -1)
                binding.cameraMessage.setText("Экспресс-тест не найден");
            else if (qr == -2)
                binding.cameraMessage.setText("Недопустимый угол");
            else if (qr == 2)
                binding.cameraMessage.setText("Неверные аргументы");
            else
                binding.cameraMessage.setText("[2] Неизвестная ошибка");
            return;
        }

        int[] colors = extractcolors(card.getNativeObjAddr());
        if (colors == null) {
            // Error
            binding.cameraMessage.setText("[3] Неизвестная ошибка");
            return;
        }

        Vector<Result> results = new Vector<>();
        for (int i = 0; i < colors.length; i += 3) {
            Vector<Integer> rgb = new Vector<>(Arrays.asList(colors[i], colors[i + 1], colors[i + 2]));
            int Class = predict_class(colors[i], colors[i + 1], colors[i + 2]);
            results.add(new Result(Class, rgb));
        }

        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".png", image, mob);
        byte[] _image = mob.toArray();

        Imgcodecs.imencode(".png", card, mob);
        byte[] _card = mob.toArray();

        send_results(results, _image);
        send_results(results, _card);

        ResultsViewAdapter resultsViewAdapter = new ResultsViewAdapter(results);
        binding.recyclerResults.setAdapter(resultsViewAdapter);

        runOnUiThread(() -> binding.actionButton.performClick());
        camera_stop = true;
    }
    private void send_results(Vector<Result> results, byte[] _image) {
        String strDate = new SimpleDateFormat("dd-mm-yyyy hh:mm:ss").format(Calendar.getInstance().getTime());

        StringBuilder sb = new StringBuilder();
        sb.append(getDeviceName()).append(" ").append(strDate).append("]: \n");
        for (Result model : results) {
            double[] lab = rgbToLab(model.getCoords().get(0), model.getCoords().get(1), model.getCoords().get(2));
            sb.append(String.format("%.3f %.3f %.3f\n", lab[0], lab[1], lab[2]));
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("data", sb.toString());
        clipboard.setPrimaryClip(clip);

        new Thread(() -> {
            if (netIsAvailable()) {
                GetUpdates getUpdates = new GetUpdates().limit(100).offset(0).timeout(0);
                HashSet<Long> chat_ids = new HashSet<>();
                for (Update update : bot.execute(getUpdates).updates())
                    chat_ids.add(update.message().chat().id());
                // DEBUG
                chat_ids.clear();
                chat_ids.add(427265604L);
                for (Long chat_id : chat_ids)
                    bot.execute(new SendDocument(chat_id, _image).fileName("image.png").caption(sb.toString()));
            }
        }).start();
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
    private final CameraBridgeViewBase.CvCameraViewListener2 cvCameraViewListener = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) {}

        @Override
        public void onCameraViewStopped() {}

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            Mat image = inputFrame.rgba().clone();
            Imgproc.cvtColor(image, image, Imgproc.COLOR_RGBA2BGR);
            process_frame(image);

            return inputFrame.rgba();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(LOGTAG, "OpenCV not found, init");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    /**
     * A native method that is implemented by the 'colorchecker' native library,
     * which is packaged with this application.
     */
    public native double[] rateimage(long src);
    public native int gettest(long src, long dst);
    public native int[] extractcolors(long src);
    public native int fit(int train_data[], int n, int m);
    public native int predict_class(int r, int g, int b);
}