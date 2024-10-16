#include <jni.h>
#include <stdbool.h>
#include <string.h>
#include <android/log.h>

#define LOG_TAG "NDK"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static bool is_ru = false;


JNIEXPORT void JNICALL
Java_com_espresso_app_articles_RegisterActivity_switchLanguageNative(JNIEnv *env, jobject obj, jboolean ru) {
    is_ru = (bool) ru;
    LOGE("Language switched to: %s", is_ru ? "RU" : "EN");
}

JNIEXPORT jstring JNICALL
Java_com_espresso_app_articles_RegisterActivity_getLocalizedStringNative(JNIEnv *env, jobject obj, jint key) {
    const char* result;

    switch (key) {
        case 0:  // Email hint
            result = is_ru ? "Введите email:" : "Enter email:";
            break;
        case 1:  // Password hint
            result = is_ru ? "Введите пароль:" : "Enter password:";
            break;
        case 2:  // Username hint
            result = is_ru ? "Введите имя пользователя:" : "Enter username:";
            break;
        case 3:  // Info hint
            result = is_ru ? "Введите информацию о себе:" : "Enter info about yourself:";
            break;
        case 4:  // Register button text
            result = is_ru ? "ЗАРЕГИСТРИРОВАТЬСЯ" : "SIGN UP";
            break;
        default:
            result = is_ru ? "Неизвестный ключ" : "Unknown key";
            break;
    }

    return (*env)->NewStringUTF(env, result);
}
bool validate_email(const char *email) {
    const char *pattern = "@";
    return strstr(email, pattern) != NULL;
}

bool validate_password(const char *password) {
    return strlen(password) >= 6;
}

bool validate_non_empty(const char *input) {
    return strlen(input) > 0;
}

JNIEXPORT jboolean JNICALL
Java_com_espresso_app_articles_RegisterActivity_validateInputsNative(JNIEnv *env, jobject obj,
                                                                     jstring email, jstring password,
                                                                     jstring username, jstring info) {
    const char *native_email = (*env)->GetStringUTFChars(env, email, 0);
    const char *native_password = (*env)->GetStringUTFChars(env, password, 0);
    const char *native_username = (*env)->GetStringUTFChars(env, username, 0);
    const char *native_info = (*env)->GetStringUTFChars(env, info, 0);

    bool valid = true;

    if (!validate_email(native_email)) {
        LOGE("Invalid email");
        valid = false;
    }

    if (!validate_password(native_password)) {
        LOGE("Password too short");
        valid = false;
    }

    if (!validate_non_empty(native_username)) {
        LOGE("Username is empty");
        valid = false;
    }

    if (!validate_non_empty(native_info)) {
        LOGE("Info is empty");
        valid = false;
    }

    (*env)->ReleaseStringUTFChars(env, email, native_email);
    (*env)->ReleaseStringUTFChars(env, password, native_password);
    (*env)->ReleaseStringUTFChars(env, username, native_username);
    (*env)->ReleaseStringUTFChars(env, info, native_info);

    return valid ? JNI_TRUE : JNI_FALSE;
}
