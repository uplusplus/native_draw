#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/bitmap.h>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,"NATIVE", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,"NATIVE", __VA_ARGS__)

#define RGB565_R(p) ((((p) & 0xF800) >> 11) << 3)
#define RGB565_G(p) ((((p) & 0x7E0 ) >> 5)  << 2)
#define RGB565_B(p) ( ((p) & 0x1F  )        << 3)
#define MAKE_RGB565(r,g,b) ((((r) >> 3) << 11) | (((g) >> 2) << 5) | ((b) >> 3))

#define RGBA_A(p) (((p) & 0xFF000000) >> 24)
#define RGBA_R(p) (((p) & 0x00FF0000) >> 16)
#define RGBA_G(p) (((p) & 0x0000FF00) >>  8)
#define RGBA_B(p)  ((p) & 0x000000FF)
#define MAKE_RGBA(r,g,b,a) (((a) << 24) | ((r) << 16) | ((g) << 8) | (b))

extern "C"
jstring
Java_vdi_oe_com_myapplication_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C"
jint
Java_vdi_oe_com_myapplication_DrawCanvas_setBitmap(
        JNIEnv *env,
        jobject  /*this*/,
        jobject zBitmap) {

    if (zBitmap == NULL) {
        LOGE("bitmap is null\n");
        return -1;
    }

    // Get bitmap info
    AndroidBitmapInfo info;
    memset(&info, 0, sizeof(info));
    AndroidBitmap_getInfo(env, zBitmap, &info);
    // Check format, only RGB565 & RGBA are supported
    if (info.width <= 0 || info.height <= 0 ||
        (info.format != ANDROID_BITMAP_FORMAT_RGB_565 && info.format != ANDROID_BITMAP_FORMAT_RGBA_8888)) {
        LOGE("invalid bitmap\n");
        env->ThrowNew(env->FindClass("java/io/IOException"), "invalid bitmap");
        return -1;
    }

    // Lock the bitmap to get the buffer
    void * pixels = NULL;
    int res = AndroidBitmap_lockPixels(env, zBitmap, &pixels);
    if (pixels == NULL) {
        LOGE("fail to lock bitmap: %d\n", res);
        env->ThrowNew(env->FindClass("java/io/IOException"), "fail to open bitmap");
        return -1;
    }

    LOGD("Effect: %dx%d, %d pixels:%p\n", info.width, info.height, info.format, pixels);


    int x = 0, y = 0;
    // From top to bottom
    for (y = 0; y < info.height; ++y) {
        // From left to right
        for (x = 0; x < info.width; ++x) {
            int a = 255, r = 0, g = 255, b = 0;
            void *pixel = NULL;
            // Get each pixel by format
            if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
                pixel = ((uint16_t *)pixels) + y * info.width + x;
//                uint16_t v = *(uint16_t *)pixel;
//                r = RGB565_R(v);
//                g = RGB565_G(v);
//                b = RGB565_B(v);
            } else {// RGBA
                pixel = ((uint32_t *)pixels) + y * info.width + x;
//                uint32_t v = *(uint32_t *)pixel;
//                a = RGBA_A(v);
//                r = RGBA_R(v);
//                g = RGBA_G(v);
//                b = RGBA_B(v);
            }

            // Grayscale
//            int gray = (r * 38 + g * 75 + b * 15) >> 7;

            // Write the pixel back
            if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
                *((uint16_t *)pixel) = MAKE_RGB565(b, g, r);
            } else {// RGBA
                *((uint32_t *)pixel) = MAKE_RGBA(b, g, r, a);
            }
        }
    }

    AndroidBitmap_unlockPixels(env, zBitmap);
    return 1;
}
