#include "OnnxEngine.h"
#include <android/log.h>

#define TAG "OnnxEngine"

OnnxEngine::OnnxEngine() { // : mEnv(ORT_LOGGING_LEVEL_WARNING, "OnnxEngine") 
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "OnnxEngine created");
}

OnnxEngine::~OnnxEngine() {
}

bool OnnxEngine::loadModel(const std::string& modelPath) {
    __android_log_print(ANDROID_LOG_INFO, TAG, "Loading ONNX model from %s", modelPath.c_str());
    // Ort::SessionOptions sessionOptions;
    // mSession = Ort::Session(mEnv, modelPath.c_str(), sessionOptions);
    return true;
}

std::string OnnxEngine::runInference(const std::string& prompt) {
    // Run ONNX session
    return "ONNX Response: " + prompt;
}

std::string OnnxEngine::runMultiModalInference(const std::vector<uint8_t>& image, const std::string& prompt) {
    return "ONNX Vision Response";
}
