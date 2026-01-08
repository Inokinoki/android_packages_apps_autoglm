#pragma once

#include <string>
#include <vector>

enum class EngineType {
    NCNN,
    ONNX
};

class ModelEngine {
public:
    virtual ~ModelEngine() = default;
    
    virtual bool loadModel(const std::string& modelPath) = 0;
    virtual std::string runInference(const std::string& prompt) = 0;
    virtual std::string runMultiModalInference(const std::vector<uint8_t>& image, const std::string& prompt) = 0;
    
    static ModelEngine* create(EngineType type);
};
