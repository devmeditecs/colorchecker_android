#ifndef CLR_COMMON_H
#define CLR_COMMON_H

#pragma pack(push, 16) // for compatibility reasons

typedef struct {
    int width;
    int height;
    int stride;
    unsigned char *pixels;
    int channels;
    int pf; // reserved
} CLR_ColorImage;

typedef struct {
    float r;
    float g;
    float b;
} CLR_RGB;

typedef struct {
    float l;
    float a;
    float b;
} CLR_LAB;

typedef enum {
    CLR_OK = 0,
    CLR_FAIL,
    CLR_INVALID_PARAM,
    CLR_OUT_OF_MEMORY,
    CLR_DATA_FILE_MISSING,
    CLR_OPERATION_ABORTED,
    CLR_GENERIC_FAILURE,
    CLR_LIB_NOT_INITIALIZED
} CLR_ResultCode;

typedef struct {
    float blur;
    float contrast;
    float shadows;
} CLR_Features;


#pragma pack (pop)

#endif
