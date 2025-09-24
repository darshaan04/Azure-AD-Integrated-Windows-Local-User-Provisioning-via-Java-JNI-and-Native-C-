#include <jni.h>
#include <windows.h>
#include <lm.h>
#include <iostream>

#pragma comment(lib, "netapi32.lib")

// Utility to convert jstring to wchar_t*
wchar_t* jstringToWchar(JNIEnv* env, jstring jStr) {
    if (jStr == NULL) return NULL;
    const jchar* raw = env->GetStringChars(jStr, NULL);
    if (raw == NULL) return NULL;
    jsize len = env->GetStringLength(jStr);
    wchar_t* buf = new wchar_t[len + 1];
    for (jsize i = 0; i < len; i++) {
        buf[i] = static_cast<wchar_t>(raw[i]);
    }
    buf[len] = L'\0';
    env->ReleaseStringChars(jStr, raw);
    return buf;
}

// JNI function implementation: adjust function name to match your package/class/method
// Assuming Java package: com.example, class: UserManager, method: createLocalUser(String, String)
extern "C" JNIEXPORT jboolean JNICALL Java_com_example_UserManager_createLocalUser
  (JNIEnv* env, jobject obj, jstring jUsername, jstring jPassword) {
    
    wchar_t* username = jstringToWchar(env, jUsername);
    wchar_t* password = jstringToWchar(env, jPassword);

    USER_INFO_1 userInfo = {0};
    userInfo.usri1_name = username;
    userInfo.usri1_password = password;
    userInfo.usri1_priv = USER_PRIV_USER;
    userInfo.usri1_flags = UF_SCRIPT | UF_DONT_EXPIRE_PASSWD | UF_NORMAL_ACCOUNT;

    NET_API_STATUS status = NetUserAdd(NULL, 1, (LPBYTE)&userInfo, NULL);

    delete[] username;
    delete[] password;

    if (status == NERR_Success) {
        return JNI_TRUE;
    } else {
        std::wcerr << L"Failed to create user. Error code: " << status << std::endl;
        return JNI_FALSE;
    }
}

