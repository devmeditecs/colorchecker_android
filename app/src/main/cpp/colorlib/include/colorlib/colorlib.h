#ifndef CLR_COLORLIB_H
#define CLR_COLORLIB_H

#include <stdint.h>

#include <colorlib/export.h>
#include <colorlib/common.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief Get vector of features of image.
 * 
 * @param image Input image. Image must be 24 bit RGB.
 * @param features Pointer to variable, where the features will be placed.
 * 
 * @return Result code.
 */
COLORLIB_EXPORT CLR_ResultCode CLR_GetFeatures(const CLR_ColorImage *image, CLR_Features *features);


/**
 * Rotate card and get qr code of test.
 *
 * Detect card markers, rotate card and put result to *card.
 * Detect qr code, decode it and put result to *qr.
 *
 * @param image Input image. Image must be 24 bit RGB.
 * @param card Pointer to the image, where the card will be placed. Image will be 24 bit RGB.
 * @param qr Pointer to variable, where the qr code will be placed.
 *
 * @return Result code.
 */
COLORLIB_EXPORT CLR_ResultCode CLR_GetCard(const CLR_ColorImage *image, CLR_ColorImage *card, int *qr);


/**
 * @brief Extract colors of indicators after calibration.
 * 
 * @param image Result of CLR_GetCard. Image must be 24 bit RGB.
 * @param colors Array where colors will be placed. Warning: this function does not control size of array.
 * @param clen Pointer to variable, where the count of indicators will be placed.
 * 
 * @return Result code.
 */
COLORLIB_EXPORT CLR_ResultCode CLR_ExtractColors(const CLR_ColorImage *image, CLR_RGB colors[], size_t *clen);

struct CLR_Classifier;
typedef struct CLR_Classifier *CLR_CLASSIFIER_HANDLE;

/**
 * @brief Creates classifier object.
 * 
 * @return Handle to classifier or null pointer on failure. 
 */
COLORLIB_EXPORT CLR_CLASSIFIER_HANDLE CLR_Classifier_Create();

/**
 * @brief Fit classifier on train data.
 * 
 * @param h Classifier handler.
 * @param colors flattened train set for classifier.
 * 
 * @return Result code. 
 */
COLORLIB_EXPORT CLR_ResultCode CLR_Classifier_Fit(CLR_CLASSIFIER_HANDLE h, CLR_LAB colors[], size_t n_rows, size_t n_cols);

/**
 * @brief Predict class of color.
 * 
 * @param h Classifier handler.
 * @param color Color to predict.
 * @param result Pointer to variable, where the result will be placed.
 * 
 * @return Result code.
 */
COLORLIB_EXPORT CLR_ResultCode CLR_Classifier_MatchColor(CLR_CLASSIFIER_HANDLE h, CLR_RGB color, int *result);

/**
 * @brief Destroy classifier object.
 * 
 * @param h Classifier handler.
 * 
 * @return Result code.
 */
COLORLIB_EXPORT CLR_ResultCode CLR_Classifier_Destroy(CLR_CLASSIFIER_HANDLE h);

#ifdef __cplusplus
}
#endif

#endif
