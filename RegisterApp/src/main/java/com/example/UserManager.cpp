#include <jni.h>
#include "com_example_UserManager.h"
#include <windows.h>
#include <lm.h>

#pragma comment(lib, "netapi32.lib")

JNIEXPORT jboolean JNICALL Java_UserManager_createUser
  (JNIEnv *env, jobject obj, jstring jusername, jstring jpassword) {
    const char* username = env->GetStringUTFChars(jusername, NULL);
    const char* password = env->GetStringUTFChars(jpassword, NULL);
    wchar_t wusername[256];
    wchar_t wpassword[256];
    MultiByteToWideChar(CP_UTF8, 0, username, -1, wusername, 256);
    MultiByteToWideChar(CP_UTF8, 0, password, -1, wpassword, 256);
    USER_INFO_1 userInfo;
    NET_API_STATUS nStatus;
    DWORD dwLevel = 1;
    ZeroMemory(&userInfo, sizeof(USER_INFO_1));
    userInfo.usri1_name = wusername;
    userInfo.usri1_password = wpassword;
    userInfo.usri1_priv = USER_PRIV_USER;
    userInfo.usri1_flags = UF_SCRIPT | UF_NORMAL_ACCOUNT;
    nStatus = NetUserAdd(NULL, dwLevel, (LPBYTE)&userInfo, NULL);
    if (nStatus != NERR_Success) {
        env->ReleaseStringUTFChars(jusername, username);
        env->ReleaseStringUTFChars(jpassword, password);
        return JNI_FALSE;  
    }
    USER_INFO_1008 userFlags;
    userFlags.usri1008_flags = UF_SCRIPT | UF_NORMAL_ACCOUNT;
    nStatus = NetUserSetInfo(NULL, wusername, 1008, (LPBYTE)&userFlags, NULL);
    env->ReleaseStringUTFChars(jusername, username);
    env->ReleaseStringUTFChars(jpassword, password);
    return (nStatus == NERR_Success) ? JNI_TRUE : JNI_FALSE;
}
