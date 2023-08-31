#include <jni.h>
#include <opencv2/opencv.hpp>

// #include "dcolorlib.hpp"

#define COLORLIB_STATIC_DEFINE
#include "colorlib/colorlib.h"
#include "colorlib/common.h"

CLR_CLASSIFIER_HANDLE ch = nullptr;

extern "C" JNIEXPORT jdoubleArray JNICALL
Java_dev_splat_colorchecker_MainActivity_rateimage(JNIEnv *env, jobject p_this, jlong src) {
    cv::Mat* image = (cv::Mat*) src;
    CLR_ColorImage img = { image->cols, image->rows, int(image->step1()), image->data, image->channels(), 0 };
    CLR_Features features;
    CLR_ResultCode rc = CLR_GetFeatures(&img, &features);
    if (rc != CLR_OK)
        return nullptr;

    std::vector<jdouble> result = {features.blur, features.shadows, features.contrast};
    jdoubleArray arr = env->NewDoubleArray(result.size());
    if (arr == nullptr)
        return nullptr;
    env->SetDoubleArrayRegion(arr, 0, result.size(), result.data());
    return arr;
}

extern "C" JNIEXPORT jint JNICALL
Java_dev_splat_colorchecker_MainActivity_gettest(JNIEnv *env, jobject p_this, jlong src, jlong dst) {
    cv::Mat* image = (cv::Mat*) src;
    cv::Mat* out = (cv::Mat*) dst;

    CLR_ColorImage img = { image->cols, image->rows, int(image->step1()), image->data, image->channels(), 0 };
    CLR_ColorImage card = { out->cols, out->rows, int(out->step1()), out->data, out->channels(), 0 };
    int qr;
    CLR_ResultCode rc = CLR_GetCard(&img, &card, &qr);
    if (rc != CLR_OK)
        return -1;

    (*out) = cv::Mat(card.height, card.width, CV_8UC3, card.pixels, card.stride);
    return rc;
}

extern "C" JNIEXPORT jintArray JNICALL
Java_dev_splat_colorchecker_MainActivity_extractcolors(JNIEnv *env, jobject p_this, jlong src) {
    cv::Mat* card = (cv::Mat *) src;

    CLR_ColorImage img = { card->cols, card->rows, int(card->step1()), card->data, card->channels(), 0 };
    CLR_RGB result[11];
    size_t rlen = 1;

    CLR_ResultCode rc = CLR_ExtractColors(&img, result, &rlen);
    // test_draw_debug(*card);
    if (rc != CLR_OK)
        return nullptr;

    size_t alen = rlen * 3;
    jintArray arr = env->NewIntArray(alen);
    if (arr == nullptr)
        return nullptr;
    jint fill[alen];
    for (size_t i = 0; i < alen; i += 3) {
        fill[i] = result[i / 3].r;
        fill[i + 1] = result[i / 3].g;
        fill[i + 2] = result[i / 3].b;
    }
    env->SetIntArrayRegion(arr, 0, alen, fill);
    return arr;
}

extern "C" JNIEXPORT jint JNICALL
Java_dev_splat_colorchecker_MainActivity_fit(JNIEnv *env, jobject thiz, jintArray train_data, jint n, jint m) {
    jsize clen = env->GetArrayLength(train_data);
    CLR_LAB data[n * m / 3];
    jint *body = env->GetIntArrayElements( train_data, 0);
    for (size_t i = 0; i < clen; i++)
        data[i / 3] = {float(body[i]), float(body[i + 1]), float(body[i + 2])};
    if (ch == nullptr)
        ch = CLR_Classifier_Create();
    CLR_ResultCode rc = CLR_Classifier_Fit(ch, data, n, m);
    if (rc != CLR_OK)
        return 0;
    return 1;
}
extern "C" JNIEXPORT jint JNICALL
Java_dev_splat_colorchecker_MainActivity_predict_1class(JNIEnv *env, jobject thiz, jint r, jint g, jint b) {
    int result;
    CLR_ResultCode rc = CLR_Classifier_MatchColor(ch, {float(r), float(g), float(b)}, &result);
    if (rc != CLR_OK)
        return -1;
    return result;
}