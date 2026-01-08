#include <jni.h>
#include <string>
#include <vector>
#include <memory>
#include "ModelEngine.h"
#include <android/log.h>

#define TAG "LLMJNI"

// Global handle
std::unique_ptr<ModelEngine> g_engine;

extern "C" JNIEXPORT jboolean JNICALL
Java_org_lineageos_setupwizard_agent_llm_LocalLLMClient_loadModel(JNIEnv* env, jobject /* this */, jstring path, jint type) {
    const char* nativePath = env->GetStringUTFChars(path, 0);
    
    // Type 0: NCNN, 1: ONNX
    EngineType engineType = (type == 0) ? EngineType::NCNN : EngineType::ONNX;
    
    g_engine.reset(ModelEngine::create(engineType));
    
    bool result = false;
    if (g_engine) {
        result = g_engine->loadModel(nativePath);
    }
    
    env->ReleaseStringUTFChars(path, nativePath);
    return result;
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_lineageos_setupwizard_agent_llm_LocalLLMClient_runInference(JNIEnv* env, jobject /* this */, jstring prompt) {
    if (!g_engine) return env->NewStringUTF("Error: Model not loaded");

    const char* nativePrompt = env->GetStringUTFChars(prompt, 0);
    std::string response = g_engine->runInference(nativePrompt);
    env->ReleaseStringUTFChars(prompt, nativePrompt);
    
    return env->NewStringUTF(response.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_lineageos_setupwizard_agent_llm_LocalLLMClient_runImageInference(JNIEnv* env, jobject /* this */, jbyteArray imageData, jstring prompt) {
    if (!g_engine) return env->NewStringUTF("Error: Model not loaded");

    const char* nativePrompt = env->GetStringUTFChars(prompt, 0);
    
    jsize len = env->GetArrayLength(imageData);
    jbyte* bytes = env->GetByteArrayElements(imageData, 0);
    std::vector<uint8_t> vec(bytes, bytes + len);
    
    std::string response = g_engine->runMultiModalInference(vec, nativePrompt);
    
    env->ReleaseByteArrayElements(imageData, bytes, 0);
    env->ReleaseStringUTFChars(prompt, nativePrompt);
    
    return env->NewStringUTF(response.c_str());
}

// JNI_OnLoad is called when the library is loaded
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    __android_log_print(ANDROID_LOG_INFO, TAG, "JNI Loaded");
    return JNI_VERSION_1_6;
}
