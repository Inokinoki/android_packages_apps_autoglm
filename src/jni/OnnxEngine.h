#pragma once
#include "ModelEngine.h"

// Stub ONNX include
// #include <onnxruntime_cxx_api.h>

class OnnxEngine : public ModelEngine {
public:
    OnnxEngine();
    ~OnnxEngine() override;
    
    bool loadModel(const std::string& modelPath) override;
    std::string runInference(const std::string& prompt) override;
    std::string runMultiModalInference(const std::vector<uint8_t>& image, const std::string& prompt) override;
    
private:
    // Ort::Env mEnv;
    // Ort::Session mSession;
};
