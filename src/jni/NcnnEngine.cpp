#include "NcnnEngine.h"
#include <android/log.h>

#define TAG "NcnnEngine"

NcnnEngine::NcnnEngine() {
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "NcnnEngine created");
}

NcnnEngine::~NcnnEngine() {
    // mNet.clear();
}

bool NcnnEngine::loadModel(const std::string& modelPath) {
    __android_log_print(ANDROID_LOG_INFO, TAG, "Loading NCNN model from %s", modelPath.c_str());
    // int ret = mNet.load_param((modelPath + ".param").c_str());
    // ret = mNet.load_model((modelPath + ".bin").c_str());
    return true; 
}

std::string NcnnEngine::runInference(const std::string& prompt) {
    // 1. Tokenize prompt
    // 2. Run inference
    // 3. Detokenize output
    return "NCNN Response: " + prompt;
}

std::string NcnnEngine::runMultiModalInference(const std::vector<uint8_t>& image, const std::string& prompt) {
    // 1. Process Image (Resize, Normalize)
    // ncnn::Mat in = ncnn::Mat::from_pixels(image.data(), ncnn::Mat::PIXEL_RGBA, w, h);
    
    // 2. Encode Image
    // 3. Concat with text embeddings
    // 4. Decode
    return "NCNN Vision Response";
}
