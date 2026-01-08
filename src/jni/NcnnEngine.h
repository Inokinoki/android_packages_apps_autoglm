#pragma once
#include "ModelEngine.h"

// Stub NCNN include
// #include <ncnn/net.h>

class NcnnEngine : public ModelEngine {
public:
    NcnnEngine();
    ~NcnnEngine() override;
    
    bool loadModel(const std::string& modelPath) override;
    std::string runInference(const std::string& prompt) override;
    std::string runMultiModalInference(const std::vector<uint8_t>& image, const std::string& prompt) override;

private:
    // ncnn::Net mNet;
};
